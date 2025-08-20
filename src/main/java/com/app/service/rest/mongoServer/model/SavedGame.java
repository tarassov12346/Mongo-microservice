package com.app.service.rest.mongoServer.model;

import org.bson.codecs.pojo.annotations.BsonCreator;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class SavedGame {
    @BsonProperty("playerName")
    private String playerName;

    @BsonProperty("playerScore")
    private int playerScore;

    @BsonProperty("cells")
    private char[][] cells;

    @BsonCreator
    private SavedGame(@BsonProperty("playerName") String playerName, @BsonProperty("playerScore") int playerScore, @BsonProperty("cells") char[][] cells) {
        this.playerName = playerName;
        this.playerScore = playerScore;
        this.cells = cells;
    }

    private SavedGame() {
    }

    public SavedGame buildSavedGame(String playerName, int playerScore, char[][] cells) {
        return new SavedGame(playerName, playerScore, cells);
    }

    public String getPlayerName() {
        return playerName;
    }

    public int getPlayerScore() {
        return playerScore;
    }

    public char[][] getCells() {
        return cells;
    }

    @Override
    public String toString() {
        return "SavedGame{" +
                "playerName='" + playerName + '\'' +
                ", playerScore=" + playerScore +
                ", cells=" + Arrays.toString(cells) +
                '}';
    }
}
