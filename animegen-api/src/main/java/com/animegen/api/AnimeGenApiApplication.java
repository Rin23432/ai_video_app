package com.animegen.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.mybatis.spring.annotation.MapperScan;

@SpringBootApplication(scanBasePackages = "com.animegen")
@MapperScan("com.animegen.dao.mapper")
public class AnimeGenApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(AnimeGenApiApplication.class, args);
    }
}
