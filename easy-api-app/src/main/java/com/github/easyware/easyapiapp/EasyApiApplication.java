package com.github.easyware.easyapiapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * easy api app 入口类
 */
@SpringBootApplication
@EnableScheduling
public class EasyApiApplication {

    /**
     * 主要方法
     * @param args
     */
    public static void main(String[] args) {
        SpringApplication.run(EasyApiApplication.class, args);
    }

}
