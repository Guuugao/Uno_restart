package com.uno_restart;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.uno_restart.mapper")
public class UnoRestartApplication {

    public static void main(String[] args) {
        SpringApplication.run(UnoRestartApplication.class, args);
    }

}
