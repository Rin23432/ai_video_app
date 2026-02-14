package com.animegen.worker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.mybatis.spring.annotation.MapperScan;

@SpringBootApplication(scanBasePackages = "com.animegen")
@EnableScheduling
@MapperScan("com.animegen.dao.mapper")
public class AnimeGenWorkerApplication {
    public static void main(String[] args) {
        SpringApplication.run(AnimeGenWorkerApplication.class, args);
    }
}
