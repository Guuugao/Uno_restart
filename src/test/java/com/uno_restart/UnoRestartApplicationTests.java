package com.uno_restart;

import com.uno_restart.service.PlayerInfoService;
import com.uno_restart.types.player.PlayerContact;
import com.uno_restart.types.player.PlayerHistory;
import com.uno_restart.types.player.PlayerInfo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class UnoRestartApplicationTests {
    @Autowired
    PlayerInfoService service;

    @Test
    void selectListDemo() {
        PlayerInfo playerInfo = service.getById("0e45527cef10f2c620e4ea1be5d214f6");
        System.out.println(playerInfo);
    }

    @Test
    void insertDemo() {
        PlayerInfo playerInfo = new PlayerInfo("demo", "root")
                .setContact(new PlayerContact("fackemail@uno.com", "12345678910"))
                .setHistory(new PlayerHistory(1, 3));
        service.save(playerInfo);
    }
}
