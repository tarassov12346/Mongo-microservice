package com.app.service.rest.mongoServer.controller;

import com.app.service.rest.mongoServer.daoservice.DaoMongoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;


@RestController
@RequiredArgsConstructor
@Slf4j
public class MongoController {

    private final DaoMongoService daoMongoService;
    private final Executor virtualThreadExecutor; // Внедряем наш пул виртуальных потоков

    @DeleteMapping("/delete")
    public void doDeleteGame(@RequestParam String playerName) {
        // Передаем пул вторым аргументом
        CompletableFuture.runAsync(() -> daoMongoService.cleanSavedGameMongodb(playerName), virtualThreadExecutor);
    }

    @PostMapping("/prepare")
    public void prepare(@RequestParam String playerName) {
        CompletableFuture.runAsync(() -> {
            if (!daoMongoService.isImageFilePresentInMongoDB(playerName)) {
                daoMongoService.prepareMongoDBForNewPLayer(playerName);
            }
        }, virtualThreadExecutor); // И сюда
    }

    @DeleteMapping("/delete_image")
    public void doDeleteImage(@RequestParam String playerName, @RequestParam String fileName) {
        CompletableFuture.runAsync(() -> daoMongoService.cleanImageMongodb(playerName, fileName), virtualThreadExecutor); // И сюда
    }
}
