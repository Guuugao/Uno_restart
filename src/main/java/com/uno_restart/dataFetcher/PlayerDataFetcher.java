package com.uno_restart.dataFetcher;

import cn.dev33.satoken.stp.StpUtil;
import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsMutation;
import com.uno_restart.service.PlayerInfoService;
import com.uno_restart.types.player.PlayerInfo;
import com.uno_restart.types.player.PlayerInfoFeedback;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;

@Slf4j
@DgsComponent
public class PlayerDataFetcher {
    @Autowired
    PlayerInfoService service;

    @DgsMutation
    public PlayerInfoFeedback playerLogout() {
        PlayerInfoFeedback feedback = new PlayerInfoFeedback(false);

        if (!StpUtil.isLogin()) {
            feedback.setMessage("请登录");
            return feedback;
        }

        feedback.setSuccess(true);
        feedback.setMessage("注销成功");
        StpUtil.logout();
        return feedback;
    }

    // TODO 数据库存储密码需要加密
    @DgsMutation
    public PlayerInfoFeedback playerLogin(@NotNull String playerName, @NotNull String password) {
        PlayerInfoFeedback feedback = new PlayerInfoFeedback(false);

        if (checkPlayerName(playerName) && checkPassword(password)) {
            PlayerInfo player = service.getById(playerName);
            if (player.getPassword().equals(password)) {
                feedback.setSuccess(true);
                feedback.setMessage("登陆成功");
                StpUtil.login(playerName);
            } else {
                feedback.setMessage("用户名或密码错误");
            }
        } else {
            feedback.setMessage("登录失败, 请检查用户名或密码格式是否正确");
        }

        return feedback;
    }

    @DgsMutation
    public PlayerInfoFeedback playerRegister(@NotNull String playerName, @NotNull String password) {
        PlayerInfoFeedback feedback = new PlayerInfoFeedback(false);

        if (checkPlayerName(playerName) && checkPassword(password)) {
            PlayerInfo player = new PlayerInfo(playerName, password);
            try {
                service.save(player);
                feedback.setSuccess(true);
                feedback.setMessage("注册成功");
                return feedback;
            } catch (DuplicateKeyException e) {
                feedback.setMessage("注册失败, 用户名已被占用");
                return feedback;
            }
        } else {
            feedback.setMessage("注册失败, 请检查用户名或密码格式是否正确");
        }

        return feedback;
    }

    @DgsMutation
    public PlayerInfoFeedback playerPasswordModify(String oldPassword, String newPassword) {
        PlayerInfoFeedback feedback = new PlayerInfoFeedback(false);

        if (!StpUtil.isLogin()) {
            feedback.setMessage("请登录");
            return feedback;
        }

        if (checkPassword(oldPassword) && checkPassword(newPassword)) {
            String playerName = StpUtil.getLoginIdAsString();
            String password = service.getBaseMapper().getPasswordByPlayerName(playerName);
            if (password.equals(oldPassword)) {
                service.getBaseMapper().updatePassword(newPassword, playerName);
                feedback.setSuccess(true);
                feedback.setMessage("密码修改成功, 请重新登录");
                StpUtil.logout();
            } else {
                feedback.setMessage("原密码错误, 请重新输入");
            }
        } else {
            feedback.setMessage("请检查密码格式是否正确");
        }

        return feedback;
    }

    @DgsMutation
    public PlayerInfoFeedback playerNameModify(String newPlayerName) {
        PlayerInfoFeedback feedback = new PlayerInfoFeedback(false);

        if (!StpUtil.isLogin()) {
            feedback.setMessage("请登录");
            return feedback;
        }

        if (checkPlayerName(newPlayerName)) {
            String playerName = StpUtil.getLoginIdAsString();
            try {
                service.getBaseMapper().updatePlayerName(newPlayerName, playerName);
                feedback.setSuccess(true);
                feedback.setMessage("用户名修改成功, 请重新登录");
                StpUtil.logout();
            } catch (DuplicateKeyException e) {
                feedback.setMessage("用户名重复, 请重新输入");
            }
        } else {
            feedback.setMessage("用户名修改失败, 请检查名称格式");
        }

        return feedback;
    }


    private boolean checkPlayerName(String playerName) {
        return playerName.matches("^.{2,8}$");
    }

    private boolean checkPassword(String password) {
        return password.matches("^(?=.*[a-zA-Z])(?=.*\\d)[a-zA-Z\\d]{6,16}$");
    }
}