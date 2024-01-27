package com.uno_restart.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uno_restart.mapper.PlayerInfoMapper;
import com.uno_restart.types.player.PlayerContact;
import com.uno_restart.types.player.PlayerInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.List;


@Service
public class PlayerService
        extends ServiceImpl<PlayerInfoMapper, PlayerInfo>
        implements IPlayerService {
    @Autowired
    Base64.Decoder decoder;
    @Autowired
    ObjectMapper jsonParser;

    @Override
    public String getPasswordByPlayerName(String playerName) {
        LambdaQueryWrapper<PlayerInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PlayerInfo::getPlayerName, playerName).select(PlayerInfo::getPassword);
        return this.getOne(wrapper).getPassword();
    }

    @Override
    public String getSalt(String playerName) {
        LambdaQueryWrapper<PlayerInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PlayerInfo::getPlayerName, playerName).select(PlayerInfo::getSalt);
        return this.getOne(wrapper).getSalt();
    }

    @Override
    public void updatePassword(String newPassword, String salt, String playerName) {
        LambdaUpdateWrapper<PlayerInfo> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(PlayerInfo::getPlayerName, playerName)
                .set(PlayerInfo::getPassword, newPassword)
                .set(PlayerInfo::getSalt, salt);
        this.update(wrapper);
    }

    @Override
    public void updatePlayerName(String newPlayerName, String playerName) {
        LambdaUpdateWrapper<PlayerInfo> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(PlayerInfo::getPlayerName, playerName).set(PlayerInfo::getPlayerName, newPlayerName);
        this.update(wrapper);
    }

    @Override
    public void updateAvatarpath(String newAvatarpath, String playerName) {
        LambdaUpdateWrapper<PlayerInfo> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(PlayerInfo::getPlayerName, playerName).set(PlayerInfo::getAvatarPath, newAvatarpath);
        this.update(wrapper);
    }

    @Override
    public void updateContact(PlayerContact newContact, String playerName) throws JsonProcessingException {
        LambdaUpdateWrapper<PlayerInfo> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(PlayerInfo::getPlayerName, playerName).set(PlayerInfo::getContact,
                jsonParser.writeValueAsString(newContact));
        this.update(wrapper);
    }

    @Override
    public List<PlayerInfo> selectPlayerInfoPage(String playerName, Integer first, String after) {
        LambdaQueryWrapper<PlayerInfo> wrapper = new LambdaQueryWrapper<>();
        String cursor = (after == null) ? null : new String(decoder.decode(after));

        wrapper.likeRight(PlayerInfo::getPlayerName, playerName);
        if (after != null && !after.isEmpty()) {
            wrapper.ge(PlayerInfo::getPlayerName, cursor);
        }
        wrapper.last("limit " + first);

        return this.list(wrapper);
    }
}
