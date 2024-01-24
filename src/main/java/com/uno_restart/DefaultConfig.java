package com.uno_restart;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.Base64;

@EnableAsync
@Configuration
@MapperScan("com.uno_restart.mapper")
public class DefaultConfig {
    // Java Base64解码器
    @Bean
    public Base64.Decoder base64Decoder(){
        return Base64.getDecoder();
    }
    // Java Base64编码器
    @Bean
    public Base64.Encoder base64Encoder(){
        return Base64.getEncoder();
    }
}
