package com.uno_restart.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uno_restart.mapper.PlayerInfoMapper;
import com.uno_restart.types.player.PlayerContact;
import com.uno_restart.types.player.PlayerInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.List;


// TODO 改为wapper实现
@Service
public class PlayerInfoService
        extends ServiceImpl<PlayerInfoMapper, PlayerInfo>
        implements IPlayerInfoService {
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
    public void updatePassword(String newPassword, String playerName) {
        LambdaUpdateWrapper<PlayerInfo> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(PlayerInfo::getPlayerName, playerName).set(PlayerInfo::getPassword, newPassword);
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
            wrapper.ge(PlayerInfo::getPlayerName, after);
        }
        wrapper.last("limit " + first);

        return this.list(wrapper);
    }
}
