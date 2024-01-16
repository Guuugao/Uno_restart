package com.uno_restart.dataFetcher;

import cn.dev33.satoken.stp.StpUtil;
import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsMutation;
import com.uno_restart.service.PlayerInfoService;
import com.uno_restart.types.player.PlayerAvatarFeedback;
import com.uno_restart.types.player.PlayerInfo;
import com.uno_restart.types.player.PlayerInfoFeedback;
import graphql.schema.DataFetchingEnvironment;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@DgsComponent
public class PlayerDataFetcher {
    @Autowired
    PlayerInfoService service;
    private static final String uploadpath = "./uploads/";
    private static final List<String> allowedAvatarTypes = new ArrayList<>(
            Arrays.asList("image/jpeg", "image/png", "image/gif"));
    private static final long allowedAvatarSize = 3 * 1024 * 1024;


    @DgsMutation
    public PlayerInfoFeedback playerLogout() {
        PlayerInfoFeedback feedback = new PlayerInfoFeedback(false);

        if (!StpUtil.isLogin()) {
            feedback.setMessage("请登录");
        } else {
            feedback.setSuccess(true);
            feedback.setMessage("注销成功");
            StpUtil.logout();
        }

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
        }else if (checkPassword(oldPassword) && checkPassword(newPassword)) {
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
        }else if (checkPlayerName(newPlayerName)) {
            String playerName = StpUtil.getLoginIdAsString();
            try {
                service.getBaseMapper().updatePlayerName(newPlayerName, playerName);
                feedback.setSuccess(true);
                feedback.setMessage("用户名修改成功, 请重新登录");
                StpUtil.logout();
                return feedback;
            } catch (DuplicateKeyException e) {
                feedback.setMessage("用户名重复, 请重新输入");
                return feedback;
            }
        } else {
            feedback.setMessage("用户名修改失败, 请检查名称格式");
        }

        return feedback;
    }

    @DgsMutation
    public PlayerAvatarFeedback playerAvatarModify(DataFetchingEnvironment dfe) {
        MultipartFile multipartFile = dfe.getArgument("avatar");
        PlayerAvatarFeedback feedback = new PlayerAvatarFeedback(false);

        if (!StpUtil.isLogin()) {
            feedback.setMessage("请登录");
        } else if (multipartFile.getSize() > allowedAvatarSize) {
            feedback.setMessage("修改失败, 头像文件过大");
        } else if (allowedAvatarTypes.contains(multipartFile.getContentType())){
            // 使用用户名作为文件名称, 保证唯一性, 可读
            String originalFilename = multipartFile.getOriginalFilename();
            assert originalFilename != null;
            String type = originalFilename.substring(originalFilename.lastIndexOf("."));

            try {
                String playerName = StpUtil.getLoginIdAsString();
                String savePath = uploadpath + playerName + type;
                multipartFile.transferTo(new File(savePath));

                service.getBaseMapper().updateAvatarpath(savePath, playerName);

                feedback.setSuccess(true);
                feedback.setMessage("头像上传成功");
                feedback.setAvatarPath(savePath);
            } catch (IOException e) {
                feedback.setMessage("文件上传失败");
                return feedback;
            }
        } else {
            feedback.setMessage("修改失败, 不允许的文件格式");
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