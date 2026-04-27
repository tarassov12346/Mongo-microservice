package com.app.service.rest.mongoServer;

import com.app.service.rest.mongoServer.daoservice.DaoMongoService;
import com.app.service.grpc.*;             // Импорт сгенерированных gRPC классов
import com.google.protobuf.ByteString;
import io.grpc.stub.StreamObserver;       // Импорт для стримов gRPC
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;


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

}
