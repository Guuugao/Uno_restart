package com.uno_restart;

import cn.dev33.satoken.stp.StpUtil;
import com.uno_restart.service.PlayerInfoService;
import com.uno_restart.types.player.PlayerContact;
import com.uno_restart.types.player.PlayerHistory;
import com.uno_restart.types.player.PlayerInfo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

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
}
