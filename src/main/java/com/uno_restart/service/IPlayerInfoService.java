package com.uno_restart.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.uno_restart.types.player.PlayerInfo;

import java.util.List;

public interface IPlayerInfoService extends IService<PlayerInfo> {
    List<PlayerInfo> selectPlayerInfoPage(String playerName, Integer first, String after);
}
