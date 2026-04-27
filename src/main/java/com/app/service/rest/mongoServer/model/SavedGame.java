package com.app.service.rest.mongoServer.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

@Data // Геттеры, сеттеры, toString
@NoArgsConstructor // Пустой конструктор для Mongo
@AllArgsConstructor // Публичный конструктор для gRPC сервиса
@Component
public class SavedGame {
    private String playerName;
    private int playerScore;
    private char[][] cells;
}
