package com.uno_restart.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.hash.Hashing;
import com.uno_restart.exception.PlayerAbnormalException;
import com.uno_restart.mapper.PlayerInfoMapper;
import com.uno_restart.types.player.PlayerContact;
import com.uno_restart.types.player.PlayerHistory;
import com.uno_restart.types.player.PlayerInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.UUID;


@Service
public class PlayerService
        extends ServiceImpl<PlayerInfoMapper, PlayerInfo> {
    private static final int MIN_PLAYER_NAME_LEN = 2;
    private static final int MAX_PLAYER_NAME_LEN = 8;

    @Autowired
    Base64.Decoder decoder;
    @Autowired
    ObjectMapper jsonParser;

    private String getPasswordByPlayerName(String playerName) {
        LambdaQueryWrapper<PlayerInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PlayerInfo::getPlayerName, playerName).select(PlayerInfo::getPassword);
        return this.getOne(wrapper).getPassword();
    }

    private String getSalt(String playerName) {
        LambdaQueryWrapper<PlayerInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PlayerInfo::getPlayerName, playerName).select(PlayerInfo::getSalt);
        return this.getOne(wrapper).getSalt();
    }

    private void updatePassword(String newPassword, String salt, String playerName) {
        LambdaUpdateWrapper<PlayerInfo> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(PlayerInfo::getPlayerName, playerName)
                .set(PlayerInfo::getPassword, newPassword)
                .set(PlayerInfo::getSalt, salt);
        this.update(wrapper);
    }

    private void updatePlayerName(String newPlayerName, String playerName) {
        LambdaUpdateWrapper<PlayerInfo> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(PlayerInfo::getPlayerName, playerName).set(PlayerInfo::getPlayerName, newPlayerName);
        this.update(wrapper);
    }

    private void updateAvatarpath(String newAvatarpath, String playerName) {
        LambdaUpdateWrapper<PlayerInfo> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(PlayerInfo::getPlayerName, playerName).set(PlayerInfo::getAvatarPath, newAvatarpath);
        this.update(wrapper);
    }

    private void updateContact(PlayerContact newContact, String playerName) throws JsonProcessingException {
        LambdaUpdateWrapper<PlayerInfo> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(PlayerInfo::getPlayerName, playerName).set(PlayerInfo::getContact,
                jsonParser.writeValueAsString(newContact));
        this.update(wrapper);
    }

    private void updateHistory(PlayerHistory newHistory, String playerName) throws JsonProcessingException {
        LambdaUpdateWrapper<PlayerInfo> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(PlayerInfo::getPlayerName, playerName).set(PlayerInfo::getHistory,
                jsonParser.writeValueAsString(newHistory));
        this.update(wrapper);
    }

    public void modifyOnlineState(boolean isOnline, String playerName) {
        LambdaUpdateWrapper<PlayerInfo> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(PlayerInfo::getPlayerName, playerName).set(PlayerInfo::getIsOnline, isOnline);
        this.update(wrapper);
    }

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

    public void playerRegister(String playerName, String password)
            throws DuplicateKeyException {
        String salt = UUID.randomUUID().toString();
        String encodePassword = encodeWithSalt(password, salt);
        PlayerInfo player = new PlayerInfo(playerName, encodePassword, salt);
        save(player);
    }

    public void modifyPlayerName(String playerName, String newPlayerName) {
        updatePlayerName(newPlayerName, playerName);
    }

    public void modifyContact(String playerName, PlayerContact contact)
            throws JsonProcessingException {
        updateContact(contact, playerName);
    }

    public void modifyAvatarPath(String playerName, String savePath) {
        updateAvatarpath(savePath, playerName);
    }

    public void modifyPassword(String playerName, String newPassword){
        String salt = UUID.randomUUID().toString();
        updatePassword(encodeWithSalt(newPassword, salt), salt, playerName);
    }

    // 在玩家名称格式错误的情况抛出异常
    public void checkPlayerName(String playerName) throws PlayerAbnormalException {
        if (playerName.length() < MIN_PLAYER_NAME_LEN || playerName.length() > MAX_PLAYER_NAME_LEN)
            throw new PlayerAbnormalException("用户名格式错误");
    }

    // 在邮箱格式错误的情况抛出异常
    public void checkEmail(String email) throws PlayerAbnormalException {
        // 邮箱为空代表清除邮箱
        if (email != null && !email.matches("^[a-z0-9A-Z]+[-|a-z0-9A-Z._]+@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-z]{2,}$"))
            throw new PlayerAbnormalException("邮箱格式错误");
    }

    // 在手机号码格式错误的情况抛出异常
    public void checkPhone(String phone) throws PlayerAbnormalException {
        // 同上
        if (phone != null && !phone.matches("^1[3456789]\\d{9}$"))
            throw new PlayerAbnormalException("手机号码格式错误");
    }

    // 使用盐加密密码
    private String encodeWithSalt(String password, String salt) {
        return Hashing.sha256()
                .hashString(password + salt, StandardCharsets.UTF_8)
                .toString();
    }

    // 比较输入密码与用户密码是否一致
    public boolean comparePassword(String playerName, String input) {
        return getPasswordByPlayerName(playerName)
                .equals(encodeWithSalt(input, getSalt(playerName)));
    }
}
