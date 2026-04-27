package com.app.service.rest.mongoServer.controller;

import com.app.service.rest.mongoServer.daoservice.DaoMongoService;
import com.app.service.rest.mongoServer.model.SavedGame;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;


@RestController
@RequiredArgsConstructor
@Slf4j
public class MongoController {

    private final DaoMongoService daoMongoService;

    @PostMapping("/save")
    public void gameSave(@RequestBody SavedGame savedGame) {
        // Запускаем в виртуальном потоке, чтобы мгновенно освободить HTTP-канал
        CompletableFuture.runAsync(() -> {
            String key = savedGame.getPlayerName() + "SavedGame";
            if (daoMongoService.isSavedGamePresentInMongoDB(key)) {
                daoMongoService.cleanSavedGameMongodb(savedGame.getPlayerName());
            }
            daoMongoService.loadSavedGameIntoMongodb(savedGame, savedGame.getPlayerName());
        });
    }

    @GetMapping("/restart")
    public Optional<SavedGame> gameRestart(@RequestParam String playerName) {
        log.info("CONTROLLER RESTART для игрока: {}", playerName);
        // Здесь асинхронность не нужна, так как нам ОБЯЗАТЕЛЬНО нужен результат сейчас
        if (daoMongoService.isSavedGamePresentInMongoDB(playerName + "SavedGame")) {
            return Optional.of(daoMongoService.loadSavedGameFromMongodb(playerName));
        }
        return Optional.empty();
    }

    @DeleteMapping("/delete")
    public void doDeleteGame(@RequestParam String playerName) {
        CompletableFuture.runAsync(() -> daoMongoService.cleanSavedGameMongodb(playerName));
    }

    @PostMapping("/prepare")
    public void prepare(@RequestParam String playerName) {
        CompletableFuture.runAsync(() -> {
            if (!daoMongoService.isImageFilePresentInMongoDB(playerName)) {
                daoMongoService.prepareMongoDBForNewPLayer(playerName);
            }
        });
    }

    @DeleteMapping("/delete_image")
    public void doDeleteImage(@RequestParam String playerName, @RequestParam String fileName) {
        CompletableFuture.runAsync(() -> daoMongoService.cleanImageMongodb(playerName, fileName));
    }
}
