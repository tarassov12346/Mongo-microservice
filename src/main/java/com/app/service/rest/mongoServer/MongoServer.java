package com.app.service.rest.mongoServer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableDiscoveryClient
@EnableCaching // <-- Добавляем этот "рубильник"
public class MongoServer {
    public static void main(String[] args) {
        System.setProperty("spring.config.name", "mongo-server");
        SpringApplication.run(MongoServer.class, args);
    }
}
