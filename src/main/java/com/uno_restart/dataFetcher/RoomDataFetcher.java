package com.uno_restart.dataFetcher;

import cn.dev33.satoken.stp.StpUtil;
import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsMutation;
import com.uno_restart.service.PlayerInfoService;
import com.uno_restart.types.player.PlayerInfo;
import com.uno_restart.types.room.GameRoomFeedback;
import com.uno_restart.types.room.GameRoomInfo;
import com.uno_restart.types.room.RoomPlayerState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;

// TODO 修改成员变量可能需要线程安全
@Slf4j
@DgsComponent
public class RoomDataFetcher {
    @Autowired
    PlayerInfoService playerService;

    // 记录玩家加入的房间id<playerName, roomID>
    HashMap<String, String> findRoomID;
    // 通过房间id快速找到房间<roomID, GameRoomInfo>
    HashMap<String, GameRoomInfo> publicRooms;
    HashMap<String, GameRoomInfo> privateRooms;

    RoomDataFetcher(){
        findRoomID = new HashMap<>();
        publicRooms = new HashMap<>();
        privateRooms = new HashMap<>();
    }

    /* TODO
    * UNO游戏原则上允许2-10人参与。
    * 但为了保证游戏的趣味性，降低运气因素，一般建议3-4人参与最佳。
    * 如果人数超过5个，可以考虑多加1副牌，使游戏进程更顺利
    * */
    /* TODO
    * 玩家名称和房间名称敏感词过滤, 可以写个checkXXX(), 顺便检查一下长度
    * */
    @DgsMutation
    public GameRoomFeedback roomCreate(String roomName, Integer maxPlayerCount, Boolean isPrivate, String password) {
        GameRoomFeedback feedback = new GameRoomFeedback(false, false);
        String playerName = StpUtil.getLoginIdAsString();

        if (!StpUtil.isLogin()) {
            feedback.setMessage("请登录");
        } else if (findRoomID.containsKey(playerName)) {
            feedback.setMessage("创建失败, 同一玩家禁止创建多个房间");
        } else if (!checkMaxPlayerCount(maxPlayerCount) || !checkRoomName(roomName)) {
            feedback.setMessage("创建房间失败, 请检查房间设置");
        } else {
            PlayerInfo player = playerService.getById(playerName);
            RoomPlayerState state = new RoomPlayerState(false, player);
            GameRoomInfo gameRoomInfo = new GameRoomInfo(roomName, isPrivate, maxPlayerCount, state, password);
            String roomID = gameRoomInfo.getRoomID();

            findRoomID.put(playerName, roomID);
            if (isPrivate) {
                privateRooms.put(roomID, gameRoomInfo);
            } else {
                publicRooms.put(roomID, gameRoomInfo);
            }

            feedback.setSuccess(true)
                    .setMessage("创建房间成功")
                    .setIsInsideRoom(true)
                    .setRoom(gameRoomInfo)
                    .setSelf(state);
        }

        return feedback;
    }

    private boolean checkMaxPlayerCount(Integer maxPlayerCount) {
        return maxPlayerCount >= 2 && maxPlayerCount <= 10;
    }

    private boolean checkRoomName(String roomName) {
        return roomName.length() >= 2 && roomName.length() <= 8;
    }
}
