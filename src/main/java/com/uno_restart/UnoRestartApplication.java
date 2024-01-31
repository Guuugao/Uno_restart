package com.uno_restart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// TODO 位于房间时修改playerName, 修改playerName应该会下线, 下线时退出游戏房间
// TODO 添加Redis
@SpringBootApplication
public class UnoRestartApplication {
    public static void main(String[] args) {
        SpringApplication.run(UnoRestartApplication.class, args);
    }

}
