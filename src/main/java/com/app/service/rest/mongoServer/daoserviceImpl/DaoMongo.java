package com.app.service.rest.mongoServer.daoserviceImpl;

import com.app.service.rest.mongoServer.daoservice.DaoMongoService;
import com.app.service.rest.mongoServer.model.SavedGame;
import com.google.gson.Gson;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.gridfs.model.GridFSUploadOptions;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
@Slf4j
@RequiredArgsConstructor
public class DaoMongo implements DaoMongoService {

    // Spring Boot автоматически создаст этот бин из spring.data.mongodb.uri
    private final MongoClient mongoClient;

    @Value("${mongoPrepareShotsPath}")
    private String mongoPrepareShotsPath;

    // Централизованный доступ к БД и бакету
    private MongoDatabase getDatabase() {
        return mongoClient.getDatabase("shopDB");
    }

    private GridFSBucket getGridFS() {
        return GridFSBuckets.create(getDatabase());
    }

    @Override
    public boolean isSavedGamePresentInMongoDB(String fileName) {
        return getDatabase().getCollection("saved_games")
                .find(Filters.eq("name", fileName)).first() != null;
    }

    @Override
    @CacheEvict(value = "items_list", key = "#playerName + #fileName + '_img'") // ✅ Удалит только эту картинку
    public void cleanImageMongodb(String playerName, String fileName) {
        GridFSBucket bucket = getGridFS();
        bucket.find(Filters.eq("filename", playerName + fileName + ".jpg"))
                .forEach(file -> bucket.delete(file.getId()));
    }

    @Override
    public void cleanSavedGameMongodb(String playerName) {
        getDatabase().getCollection("saved_games")
                .deleteMany(Filters.eq("name", playerName + "SavedGame"));
    }

    @Override
    @CachePut(value = "items_list", key = "#playerName + '_save'")
    public SavedGame loadSavedGameIntoMongodb(SavedGame savedGame, String playerName) {
        String key = playerName + "SavedGame";
        Document doc = Document.parse(new Gson().toJson(savedGame));
        doc.put("name", key);

        getDatabase().getCollection("saved_games").replaceOne(
                Filters.eq("name", key),
                doc,
                new ReplaceOptions().upsert(true)
        );

        return savedGame; // Нам нужно вернуть объект, чтобы Spring положил его в кэш!
    }


    @Override
    @Cacheable(value = "items_list", key = "#playerName + '_save'")
    public SavedGame loadSavedGameFromMongodb(String playerName) {
        Document doc = getDatabase().getCollection("saved_games")
                .find(Filters.eq("name", playerName + "SavedGame")).first();
        return doc != null ? new Gson().fromJson(doc.toJson(), SavedGame.class) : null;
    }

    @Override
    // Выбивает кэш конкретного скриншота
    @CacheEvict(value = "items_list", key = "#playerName + #fileName + '_img'")
    public void loadSnapShotIntoMongodb(String playerName, String fileName, byte[] data) {
        uploadToGridFS(playerName + fileName + ".jpg", data);
    }

    @Override
    // Выбивает кэш аватара (когда fileName при скачивании равен "mugShot")
    @CacheEvict(value = "items_list", key = "#playerName + 'mugShot_img'")
    public void loadMugShotIntoMongodb(String playerName, byte[] data) {
        uploadToGridFS(playerName + ".jpg", data);
    }

    private void uploadToGridFS(String fullFileName, byte[] data) {
        // Эта строка покажет, какой поток выполняет тяжелую запись
        log.info("🚀 Запись в Mongo. Поток: {}", Thread.currentThread());
        GridFSUploadOptions options = new GridFSUploadOptions()
                .chunkSizeBytes(1048576)
                .metadata(new Document("type", "jpg"));

        try (var uploadStream = getGridFS().openUploadStream(fullFileName, options)) {
            uploadStream.write(data);
            log.info("📸 Файл {} успешно сохранен. ID: {}", fullFileName, uploadStream.getObjectId());
        } catch (Exception e) {
            log.error("❌ Ошибка загрузки в GridFS: {}", e.getMessage());
        }
    }

    @Override
    @Cacheable(value = "items_list", key = "#playerName + #fileName + '_img'")
    public byte[] loadByteArrayFromMongodb(String playerName, String fileName) {
        String finalName = fileName.equals("mugShot") ? playerName + ".jpg" : playerName + fileName + ".jpg";
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            getGridFS().downloadToStream(finalName, bos);
            return bos.toByteArray();
        } catch (Exception e) {
            log.error("❌ Ошибка скачивания {}: {}", finalName, e.getMessage());
            return new byte[0];
        }
    }

    @Override
    public void prepareMongoDBForNewPLayer(String playerName) {
        fillMongoDB("Player", playerName);
        fillMongoDB("PlayerdeskTopSnapShotBest", playerName + "deskTopSnapShotBest");
        fillMongoDB("PlayerdeskTopSnapShot", playerName + "deskTopSnapShot");
    }

    @Override
    public boolean isImageFilePresentInMongoDB(String fileName) {
        return getDatabase().getCollection("fs.files")
                .find(Filters.eq("filename", fileName + ".jpg")).first() != null;
    }

    private void fillMongoDB(String fileNameOnPC, String fileNameINDB) {
        Path path = Path.of(System.getProperty("user.dir"), mongoPrepareShotsPath, fileNameOnPC + ".jpg");
        try {
            byte[] data = Files.readAllBytes(path);
            uploadToGridFS(fileNameINDB + ".jpg", data);
        } catch (IOException e) {
            log.error("❌ Ошибка чтения исходного файла {}: {}", path, e.getMessage());
        }
    }
}
