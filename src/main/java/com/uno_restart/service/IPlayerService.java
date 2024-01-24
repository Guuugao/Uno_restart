package com.uno_restart.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.uno_restart.types.player.PlayerContact;
import com.uno_restart.types.player.PlayerInfo;

import java.util.List;

public interface IPlayerService {
//    void updateContact(PlayerContact contact, String playerName) throws JsonProcessingException;
//    List<PlayerInfo> selectPlayerInfoPage(String playerName, Integer first, String after);



    String getPasswordByPlayerName(String playerName);

    String getSalt(String playerName);
    void updatePassword(String newPassword, String salt, String playerName);
    void updatePlayerName(String newPlayerName, String playerName);

    void updateAvatarpath(String newAvatarpath, String playerName);

    void updateContact(PlayerContact contact, String playerName) throws JsonProcessingException;

    List<PlayerInfo> selectPlayerInfoPage(String playerName, Integer first, String after);
}
