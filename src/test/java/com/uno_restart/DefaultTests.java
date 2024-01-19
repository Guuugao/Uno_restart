package com.uno_restart;

import cn.dev33.satoken.stp.StpUtil;
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


@SpringBootTest
class DefaultTests {
    @Autowired
    PlayerInfoService service;

    @Test
    void select() {
        PlayerInfo playerInfo = service.getById("admin");
        System.out.println(playerInfo);
    }

    @Test
    void selectPage(){

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
}
