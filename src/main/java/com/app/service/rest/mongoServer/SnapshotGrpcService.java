package com.app.service.rest.mongoServer;

import com.app.service.rest.mongoServer.daoservice.DaoMongoService;
import com.app.service.grpc.*;             // Импорт сгенерированных gRPC классов
import com.app.service.rest.mongoServer.model.SavedGame;
import com.google.protobuf.ByteString;
import io.grpc.stub.StreamObserver;       // Импорт для стримов gRPC
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.Arrays;
import java.util.List;


@Slf4j                    // Чтобы заработал 'log'
@GrpcService             // Чтобы Spring увидел сервис
@RequiredArgsConstructor   // Чтобы инициализировался daoMongoService
public class SnapshotGrpcService extends SnapshotServiceGrpc.SnapshotServiceImplBase{
    private final DaoMongoService daoMongoService;

    @Override
    public void uploadSnapShot(SnapshotRequest request, StreamObserver<SnapshotResponse> responseObserver) {
        try {
            log.info("🛰 gRPC принял скриншот для игрока: {}", request.getPlayerName());

            // Вызываем твой существующий метод в DaoMongo
            daoMongoService.loadSnapShotIntoMongodb(
                    request.getPlayerName(),
                    request.getFileName(),
                    request.getData().toByteArray()
            );

            // Отправляем ответ клиенту
            SnapshotResponse response = SnapshotResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Snapshot saved successfully")
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {
            log.error("❌ Ошибка gRPC при сохранении: {}", e.getMessage());
            responseObserver.onError(e);
        }
    }
    @Override
    public void uploadMugShot(MugShotRequest request, StreamObserver<SnapshotResponse> responseObserver) {
        daoMongoService.loadMugShotIntoMongodb(request.getPlayerName(), request.getData().toByteArray());

        responseObserver.onNext(SnapshotResponse.newBuilder().setSuccess(true).build());
        responseObserver.onCompleted();
    }

    @Override
    public void downloadBytes(DownloadRequest request, StreamObserver<DownloadResponse> responseObserver) {
        byte[] data = daoMongoService.loadByteArrayFromMongodb(request.getPlayerName(), request.getFileName());

        DownloadResponse response = DownloadResponse.newBuilder()
                .setData(ByteString.copyFrom(data))
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void saveGame(SaveGameRequest request, StreamObserver<SnapshotResponse> responseObserver) {
        try {
            // Превращаем список строк обратно в char[][]
            List<String> rowsList = request.getRowsList();
            char[][] cells = new char[rowsList.size()][];
            for (int i = 0; i < rowsList.size(); i++) {
                cells[i] = rowsList.get(i).toCharArray();
            }

            // Создаем объект для БД
            SavedGame savedGame = new SavedGame(
                    request.getPlayerName(),
                    request.getPlayerScore(),
                    cells
            );

            // Сохраняем в Mongo
            daoMongoService.loadSavedGameIntoMongodb(savedGame, savedGame.getPlayerName());

            responseObserver.onNext(SnapshotResponse.newBuilder().setSuccess(true).build());
            responseObserver.onCompleted();

        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    @Override
    public void getSavedGame(GetSavedGameRequest request, StreamObserver<GetSavedGameResponse> responseObserver) {
        try {
            String playerName = request.getPlayerName();
            // Ищем игру в твоем DAO
            SavedGame game = daoMongoService.loadSavedGameFromMongodb(playerName);

            GetSavedGameResponse.Builder responseBuilder = GetSavedGameResponse.newBuilder();

            if (game != null) {
                // Если нашли — мапим данные в gRPC ответ
                List<String> rows = Arrays.stream(game.getCells())
                        .map(String::new)
                        .toList();

                responseBuilder.setFound(true)
                        .setPlayerName(game.getPlayerName())
                        .setPlayerScore(game.getPlayerScore())
                        .addAllRows(rows);
            } else {
                responseBuilder.setFound(false);
            }

            responseObserver.onNext(responseBuilder.build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("❌ Ошибка при поиске игры: {}", e.getMessage());
            responseObserver.onError(e);
        }
    }



}
