package com.uno_restart.dataFetcher;

import cn.dev33.satoken.stp.StpUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsMutation;
import com.netflix.graphql.dgs.DgsQuery;
import com.uno_restart.exception.PlayerAbnormalException;
import com.uno_restart.service.PlayerService;
import com.uno_restart.service.RoomService;
import com.uno_restart.types.player.PlayerAvatarFeedback;
import com.uno_restart.types.player.PlayerContact;
import com.uno_restart.types.player.PlayerInfo;
import com.uno_restart.types.player.PlayerInfoFeedback;
import graphql.relay.*;
import graphql.schema.DataFetchingEnvironment;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

// TODO 修改成员变量可能需要线程安全
@Slf4j
@DgsComponent
public class PlayerDataFetcher {

    @Autowired
    PlayerService playerService;
    @Autowired
    RoomService roomService;
    @Autowired
    Base64.Encoder encoder;

    @Value("${global-setting.avatar-upload-path: ./uploads/}")
    private String UPLOAD_PATH;
    @Value("${global-setting.allowed-avatar-types: image/jpeg, image/png, image/gif}")
    private List<String> ALLOWED_AVATAR_TYPES;
    @Value("${global-setting.max-avatar-size: 3145728}")
    private long MAX_AVATAR_SIZE;

    @DgsQuery
    public PlayerInfo me() {
        log.info(String.valueOf(ALLOWED_AVATAR_TYPES));
        log.info(String.valueOf(UPLOAD_PATH));
        log.info(String.valueOf(MAX_AVATAR_SIZE));
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

    // 退出前先退出房间
    @DgsMutation
    public PlayerInfoFeedback playerQuit() {
        StpUtil.checkLogin();

        String playerName = StpUtil.getLoginIdAsString();
        roomService.quit(playerName);
        playerService.modifyOnlineState(false, playerName);

        return new PlayerInfoFeedback(true)
                .setMessage("退出成功");
    }

    @DgsMutation
    public PlayerInfoFeedback playerLogout() {
        StpUtil.checkLogin();

        String playerName = StpUtil.getLoginIdAsString();
        roomService.quit(playerName);
        playerService.modifyOnlineState(false, playerName);
        StpUtil.logout();

        return new PlayerInfoFeedback(true)
                .setMessage("注销成功");
    }

    @DgsMutation
    public PlayerInfoFeedback playerLogin(@NotNull String playerName, @NotNull String password)
            throws PlayerAbnormalException {
        playerService.checkPlayerName(playerName);

        PlayerInfoFeedback feedback = new PlayerInfoFeedback(false);
        if (playerService.comparePassword(playerName, password)) {
            feedback.setSuccess(true)
                    .setMessage("登陆成功");
            playerService.modifyOnlineState(true, playerName);
            StpUtil.login(playerName);
        } else {
            feedback.setMessage("用户名或密码错误");
        }

        return feedback;
    }

    @DgsMutation
    public PlayerInfoFeedback playerRegister(@NotNull String playerName, @NotNull String password)
            throws PlayerAbnormalException {
        playerService.checkPlayerName(playerName);

        PlayerInfoFeedback feedback = new PlayerInfoFeedback(false);
        try {
            playerService.playerRegister(playerName, password);
            feedback.setSuccess(true)
                    .setMessage("注册成功");
        } catch (DuplicateKeyException e) {
            feedback.setMessage("注册失败, 用户名已被占用");
            return feedback;
        }

        return feedback;
    }

    @DgsMutation
    public PlayerInfoFeedback playerPasswordModify(String oldPassword, String newPassword) {
        StpUtil.checkLogin();

        PlayerInfoFeedback feedback = new PlayerInfoFeedback(false);
        String playerName = StpUtil.getLoginIdAsString();
        if (playerService.comparePassword(playerName, oldPassword)) {
            playerService.modifyPassword(playerName, newPassword);
            feedback.setSuccess(true)
                    .setMessage("密码修改成功, 请重新登录");
            StpUtil.logout();
        } else {
            feedback.setMessage("原密码错误, 请重新输入");
        }

        return feedback;
    }

    @DgsMutation
    public PlayerInfoFeedback playerNameModify(String newPlayerName)
            throws PlayerAbnormalException {
        StpUtil.checkLogin();
        playerService.checkPlayerName(newPlayerName);

        PlayerInfoFeedback feedback = new PlayerInfoFeedback(false);
        try {
            playerService.modifyPlayerName(StpUtil.getLoginIdAsString(), newPlayerName);
            feedback.setSuccess(true)
                    .setMessage("用户名修改成功, 请重新登录");
            StpUtil.logout();
        } catch (DuplicateKeyException e) {
            feedback.setMessage("用户名重复, 请重新输入");
            return feedback;
        }

        return feedback;
    }

    @DgsMutation
    public PlayerAvatarFeedback playerAvatarModify(DataFetchingEnvironment dfe) {
        StpUtil.checkLogin();

        MultipartFile multipartFile = dfe.getArgument("avatar");
        PlayerAvatarFeedback feedback = new PlayerAvatarFeedback(false);

        if (multipartFile.getSize() > MAX_AVATAR_SIZE) {
            feedback.setMessage("修改失败, 头像文件过大");
        } else if (ALLOWED_AVATAR_TYPES.contains(multipartFile.getContentType())) {
            // 使用用户名作为文件名称, 保证唯一性, 可读
            String originalFilename = multipartFile.getOriginalFilename();
            assert originalFilename != null;
            String type = originalFilename.substring(originalFilename.lastIndexOf("."));

            try {
                String playerName = StpUtil.getLoginIdAsString();
                String savePath = UPLOAD_PATH + playerName + type;
                multipartFile.transferTo(new File(savePath));

                playerService.modifyAvatarPath(playerName, savePath);

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
    public PlayerInfoFeedback playerContactModify(String email, String phone)
            throws JsonProcessingException, PlayerAbnormalException {
        StpUtil.checkLogin();
        playerService.checkEmail(email);
        playerService.checkPhone(email);

        PlayerInfoFeedback feedback = new PlayerInfoFeedback(false);
        playerService.modifyContact(StpUtil.getLoginIdAsString(), new PlayerContact(email, phone));

        feedback.setSuccess(true)
                .setMessage("联系方式修改成功");

        return feedback;
    }
}