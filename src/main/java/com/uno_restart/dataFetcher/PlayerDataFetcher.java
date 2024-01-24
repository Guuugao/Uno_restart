package com.uno_restart.dataFetcher;

import cn.dev33.satoken.stp.StpUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.hash.Hashing;
import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsMutation;
import com.netflix.graphql.dgs.DgsQuery;
import com.uno_restart.service.PlayerService;
import com.uno_restart.types.player.PlayerAvatarFeedback;
import com.uno_restart.types.player.PlayerContact;
import com.uno_restart.types.player.PlayerInfo;
import com.uno_restart.types.player.PlayerInfoFeedback;
import graphql.relay.*;
import graphql.schema.DataFetchingEnvironment;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

// TODO 修改成员变量可能需要线程安全

@Slf4j
@DgsComponent
public class PlayerDataFetcher {
    @Autowired
    PlayerService playerService;
    @Autowired
    Base64.Encoder encoder;
    private static final String uploadPath = "./uploads/";
    private static final List<String> allowedAvatarTypes = new ArrayList<>(
            Arrays.asList("image/jpeg", "image/png", "image/gif"));
    private static final long allowedAvatarSize = 3 * 1024 * 1024;

    private static final int minPlayerNameLen = 2;
    private static final int maxPlayerNameLen = 8;

    @DgsQuery
    public PlayerInfo me() {
        StpUtil.checkLogin();
        return playerService.getById(StpUtil.getLoginIdAsString());
    }

    @DgsQuery
    public Connection<PlayerInfo> queryPlayerByName(String playerName, Integer first, String after) {
        StpUtil.checkLogin();

        List<PlayerInfo> playerInfos = playerService.selectPlayerInfoPage(playerName, first + 2, after);

        boolean hasPreviousPage = false;
        boolean hasNextPage = false;
        if (!playerInfos.isEmpty()) { // 去除首尾多余节点, 还有数据
            // 若存在前驱页, 则第一条数据的playerName与解码后的after相等
            // 此时设置hasPreviousPage = true, 同时删除该前驱节点
            String previousNodeCursor = encoder.encodeToString(
                    playerInfos.get(0).getPlayerName().getBytes(StandardCharsets.UTF_8));
            if (previousNodeCursor.equals(after)) {
                hasPreviousPage = true;
                playerInfos.remove(0);
            }
            // 若数据条数大于给定大小(first), 代表包含多余节点, 即存在后驱页
            hasNextPage = playerInfos.size() > first;
        }

        List<Edge<PlayerInfo>> edges = playerInfos.stream()
                .limit(first)
                .map(playerInfo -> new DefaultEdge<>(playerInfo,
                        new DefaultConnectionCursor(
                                encoder.encodeToString(
                                        playerInfo.getPlayerName().getBytes(
                                                StandardCharsets.UTF_8)))))
                .collect(Collectors.toList());

        // 没有数据, 则游标为空
        ConnectionCursor startCursor = !edges.isEmpty() ? edges.get(0).getCursor() : new DefaultConnectionCursor("null");
        ConnectionCursor endCursor = !edges.isEmpty() ? edges.get(edges.size() - 1).getCursor() : new DefaultConnectionCursor("null");

        PageInfo pageInfo = new DefaultPageInfo(
                startCursor,
                endCursor,
                hasPreviousPage,
                hasNextPage
        );

        return new DefaultConnection<>(edges, pageInfo);
    }

    @DgsMutation
    public PlayerInfoFeedback playerLogout() {
        PlayerInfoFeedback feedback = new PlayerInfoFeedback(false);

        if (!StpUtil.isLogin()) {
            feedback.setMessage("请登录");
        } else {
            feedback.setSuccess(true)
                    .setMessage("注销成功");
            StpUtil.logout();
        }

        return feedback;
    }

    @DgsMutation
    public PlayerInfoFeedback playerLogin(@NotNull String playerName, @NotNull String password) {
        PlayerInfoFeedback feedback = new PlayerInfoFeedback(false);

        if (checkPlayerName(playerName)) {
            PlayerInfo player = playerService.getById(playerName);
            if (comparePassword(playerName, password)) {
                feedback.setSuccess(true)
                        .setMessage("登陆成功");
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

        if (checkPlayerName(playerName)) {
            String salt = UUID.randomUUID().toString();
            String encodePassword = encodeWithSalt(password, salt);

            PlayerInfo player = new PlayerInfo(playerName, encodePassword, salt);
            try {
                playerService.save(player);
                feedback.setSuccess(true)
                        .setMessage("注册成功");
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

        String playerName = StpUtil.getLoginIdAsString();
        String password = playerService.getPasswordByPlayerName(playerName);
        if (comparePassword(playerName, oldPassword)) {
            String salt = UUID.randomUUID().toString();
            playerService.updatePassword(encodeWithSalt(newPassword, salt), salt, playerName);
            feedback.setSuccess(true)
                    .setMessage("密码修改成功, 请重新登录");
            StpUtil.logout();
        } else {
            feedback.setMessage("原密码错误, 请重新输入");
        }


        return feedback;
    }

    @DgsMutation
    public PlayerInfoFeedback playerNameModify(String newPlayerName) {
        PlayerInfoFeedback feedback = new PlayerInfoFeedback(false);

        if (!StpUtil.isLogin()) {
            feedback.setMessage("请登录");
        } else if (checkPlayerName(newPlayerName)) {
            String playerName = StpUtil.getLoginIdAsString();
            try {
                playerService.updatePlayerName(newPlayerName, playerName);
                feedback.setSuccess(true)
                        .setMessage("用户名修改成功, 请重新登录");
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
        } else if (allowedAvatarTypes.contains(multipartFile.getContentType())) {
            // 使用用户名作为文件名称, 保证唯一性, 可读
            String originalFilename = multipartFile.getOriginalFilename();
            assert originalFilename != null;
            String type = originalFilename.substring(originalFilename.lastIndexOf("."));

            try {
                String playerName = StpUtil.getLoginIdAsString();
                String savePath = uploadPath + playerName + type;
                multipartFile.transferTo(new File(savePath));

                playerService.updateAvatarpath(savePath, playerName);

                feedback.setSuccess(true)
                        .setMessage("头像上传成功");
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

    @DgsMutation
    public PlayerInfoFeedback playerContactModify(String email, String phone) throws JsonProcessingException {
        PlayerInfoFeedback feedback = new PlayerInfoFeedback(false);

        if (!StpUtil.isLogin()) {
            feedback.setMessage("请登录");
        } else if (checkEmail(email) && checkPhone(phone)) {
            String playerName = StpUtil.getLoginIdAsString();
            playerService.updateContact(new PlayerContact(email, phone), playerName);

            feedback.setSuccess(true)
                    .setMessage("联系方式修改成功");
        } else {
            feedback.setMessage("联系方式修改失败, 邮箱或手机号码格式不正确");
        }

        return feedback;
    }

    private boolean checkPlayerName(String playerName) {
        return playerName.length() >= minPlayerNameLen && playerName.length() <= maxPlayerNameLen;
    }

    private boolean checkEmail(String email) {
        // 邮箱为空代表清除邮箱
        return email == null || email.matches("^[a-z0-9A-Z]+[-|a-z0-9A-Z._]+@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-z]{2,}$");
    }

    private boolean checkPhone(String phone) {
        // 同上
        return phone == null || phone.matches("^1[3456789]\\d{9}$");
    }

    private boolean checkPassword(String password) {
        return password.matches("^(?=.*[a-zA-Z])(?=.*\\d)[a-zA-Z\\d]{6,16}$");
    }

    private String encodeWithSalt(String password, String salt) {
        return Hashing.sha256()
                .hashString(password + salt, StandardCharsets.UTF_8)
                .toString();
    }

    private boolean comparePassword(String playerName, String input) {
        return playerService.getPasswordByPlayerName(playerName)
                .equals(encodeWithSalt(input, playerService.getSalt(playerName)));
    }
}