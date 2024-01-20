package com.uno_restart;

import cn.dev33.satoken.fun.SaParamRetFunction;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uno_restart.service.PlayerInfoService;
import com.uno_restart.types.player.PlayerContact;
import com.uno_restart.types.player.PlayerHistory;
import com.uno_restart.types.player.PlayerInfo;
import com.uno_restart.util.RoomIDUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;


@SpringBootTest
class DefaultTests {
    @Autowired
    PlayerInfoService service;

    @Test
    void select() {
        List<PlayerInfo> playerInfos = service.selectPlayerInfoPage("Thh", 5, null);
        playerInfos.forEach(System.out::println);
    }

    @Test
    void update() throws JsonProcessingException {
        LambdaUpdateWrapper<PlayerInfo> wrapper = new LambdaUpdateWrapper<>();
        ObjectMapper mapper = new ObjectMapper();
        PlayerContact contact = new PlayerContact(null, null);
        wrapper.eq(PlayerInfo::getPlayerName, "admin").set(PlayerInfo::getContact,
                mapper.writeValueAsString(contact));
        service.update(wrapper);
    }
    @Test
    void reference(){
        SaParamRetFunction<PlayerInfo, String> getPassword = PlayerInfo::getPassword;
        System.out.println(getPassword.toString());
        System.out.println(getPassword.getClass());
    }

    @Test
    void insert() {
        PlayerInfo playerInfo = new PlayerInfo("demo", "root")
                .setContact(new PlayerContact("fackemail@uno.com", "12345678910"))
                .setHistory(new PlayerHistory(1, 3));
        service.save(playerInfo);
    }

    @Test
    void saToken() {
        StpUtil.login("admin");
        System.out.println(StpUtil.getTokenName());
        System.out.println(StpUtil.getTokenValue());
        System.out.println(StpUtil.getLoginIdByToken(StpUtil.getTokenValue()));
        System.out.println(StpUtil.getTokenTimeout());
        System.out.println(StpUtil.getTokenTimeout(StpUtil.getTokenValue()));
        System.out.println(StpUtil.getTokenInfo());
        StpUtil.logout("admin");
    }

    @Test
    void roomID(){
        for (int i = 0; i< 10; ++i){
            System.out.println("-----------------");
            System.out.println(System.currentTimeMillis());
            System.out.println(RoomIDUtil.getNextId());
        }
    }

    @Test
    void jsonParse() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        PlayerContact contact = new PlayerContact(null, null);
        System.out.println(contact.getEmail());
        System.out.println(contact.getPhone());
        System.out.println(objectMapper.writeValueAsString(contact));
    }

    @Test
    void wapper() {
        LambdaQueryWrapper<PlayerInfo> wrapper = new LambdaQueryWrapper<>( );
        wrapper.clear();
        wrapper.select();
        List<PlayerInfo> list = service.list(wrapper);
        list.forEach(System.out::println);
        wrapper.select(PlayerInfo::getPassword).eq(PlayerInfo::getPlayerName, "admin");
        PlayerInfo one = service.getOneOpt(wrapper).get();
        System.out.println(one);
    }
}
