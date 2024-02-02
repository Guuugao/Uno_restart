package com.uno_restart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// TODO 添加Redis
// TODO 添加接口"specifyTheFirstCardColor", 在抽到的第一张卡为万能卡时, 庄家指定一种颜色, 然后游戏开始
// TODO DGS错误处理详细使用方法
// TODO 出牌时手上没有对应手牌怎么办
@SpringBootApplication
public class UnoRestartApplication {
    public static void main(String[] args) {
        SpringApplication.run(UnoRestartApplication.class, args);
    }

}
