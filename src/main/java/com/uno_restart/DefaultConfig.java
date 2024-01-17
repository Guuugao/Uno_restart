package com.uno_restart;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Base64;

@Configuration
@MapperScan("com.uno_restart.mapper")
public class DefaultConfig {
    @Bean
    public Base64.Decoder base64Decoder(){
        return Base64.getDecoder();
    }

    @Bean
    public Base64.Encoder base64Encoder(){
        return Base64.getEncoder();
    }
}
