package com.uno_restart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class UnoRestartApplication {
    // TODO 玩家通过前端不可能触发的问题, 都使用抛出异常进行处理

    public static void main(String[] args) {
        SpringApplication.run(UnoRestartApplication.class, args);
    }

}
