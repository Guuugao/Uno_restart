package com.uno_restart;

import org.jetbrains.annotations.NotNull;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

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

    // 设置允许跨域请求
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(@NotNull CorsRegistry registry) {
                registry
                        .addMapping("/**")
                        .allowedOriginPatterns("*") // 允许所有域
                        .allowedMethods("*") // 允许任何方法（post、get等）
                        .allowedHeaders("*") // 允许任何请求头
                        .allowCredentials(false) // 允许证书、cookie
                        .allowedOrigins("localhost:5173")
                        .exposedHeaders(HttpHeaders.SET_COOKIE)
                        .maxAge(3600L); // maxAge(3600)表明在3600秒内，不需要再发送预检验请求，可以缓存该结果
            }
        };
    }
}
