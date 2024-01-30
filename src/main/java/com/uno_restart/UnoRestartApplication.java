package com.uno_restart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// TODO 修改日志: service日志为debug级别, datafetcher日志级别为info
// TODO 增加订阅用于获取游戏结束时的玩家排名
// TODO 使用登录验证拦截器
// TODO 游戏结束时更新玩家游戏数据
// TODO 位于房间时修改playerName, 修改playerName应该会下线, 下线时退出游戏房间
// TODO 添加Redis
@SpringBootApplication
public class UnoRestartApplication {
    public static void main(String[] args) {
        SpringApplication.run(UnoRestartApplication.class, args);
    }

}
