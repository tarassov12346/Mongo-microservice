package com.app.service.rest.mongoServer.controller;

import com.app.service.rest.mongoServer.daoservice.DaoMongoService;
import com.app.service.rest.mongoServer.model.SavedGame;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
public class MongoController {

    @Autowired
    private DaoMongoService daoMongoService;

    @RequestMapping("/save")
    public void gameSave(@RequestBody SavedGame savedGame) {
        if (daoMongoService.isSavedGamePresentInMongoDB(savedGame.getPlayerName() + "SavedGame")) {
            daoMongoService.cleanSavedGameMongodb(savedGame.getPlayerName());
        }
        daoMongoService.loadSavedGameIntoMongodb(savedGame, savedGame.getPlayerName());
    }

    @RequestMapping("/restart")
    public Optional<SavedGame> gameRestart(@RequestParam String playerName) {
        if (daoMongoService.isSavedGamePresentInMongoDB(playerName + "SavedGame")) {
            return Optional.of(daoMongoService.loadSavedGameFromMongodb(playerName));
        } else return Optional.empty();
    }

    @RequestMapping("/delete")
    public void doDelete(@RequestParam String playerName) {
        daoMongoService.cleanSavedGameMongodb(playerName);
    }

    @RequestMapping("/prepare")
    public void prepare(@RequestParam String playerName) {
        if (!daoMongoService.isImageFilePresentInMongoDB(playerName))
            daoMongoService.prepareMongoDBForNewPLayer(playerName);
    }

    @RequestMapping("/delete_image")
    public void doDelete(@RequestParam String playerName, @RequestParam String fileName) {
        daoMongoService.cleanImageMongodb(playerName, fileName);
    }

    @RequestMapping("/bytes")
    public byte[] loadByteArrayFromMongodb(@RequestParam String playerName, @RequestParam String fileName) {
       return daoMongoService.loadByteArrayFromMongodb(playerName,fileName);
    }

}
