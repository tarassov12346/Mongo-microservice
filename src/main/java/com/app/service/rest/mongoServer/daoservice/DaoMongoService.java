package com.app.service.rest.mongoServer.daoservice;

import com.app.service.rest.mongoServer.model.Game;
import com.app.service.rest.mongoServer.model.SavedGame;

public interface DaoMongoService {
    //   void runMongoServer();

    void prepareMongoDBForNewPLayer(String playerName);

    boolean isImageFilePresentInMongoDB(String fileName);

    boolean isSavedGamePresentInMongoDB(String fileName);

    void cleanImageMongodb(String playerName, String fileName);

    void cleanSavedGameMongodb(String playername);

    void loadSavedGameIntoMongodb(SavedGame savedGame, String playerName);

    SavedGame loadSavedGameFromMongodb(String playerName);

    //   void loadSnapShotIntoMongodb(String playerName, String fileName);

    void loadMugShotIntoMongodb(String playerName, byte[] data);

    // void makeDesktopSnapshot(String fileNameDetail, State state, String bestPlayerName, int bestPlayerScore);

    byte[] loadByteArrayFromMongodb(String playerName, String fileName);
}
