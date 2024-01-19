package com.uno_restart.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.uno_restart.types.player.PlayerContact;
import com.uno_restart.types.player.PlayerInfo;

import java.util.List;

public interface IPlayerInfoService extends IService<PlayerInfo> {
    void updateContact(PlayerContact contact, String playerName) throws JsonProcessingException;
    List<PlayerInfo> selectPlayerInfoPage(String playerName, Integer first, String after);
}
