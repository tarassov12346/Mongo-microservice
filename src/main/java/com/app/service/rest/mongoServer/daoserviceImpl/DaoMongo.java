package com.app.service.rest.mongoServer.daoserviceImpl;

import com.app.service.rest.mongoServer.daoservice.DaoMongoService;
import com.app.service.rest.mongoServer.model.Game;
import com.app.service.rest.mongoServer.model.SavedGame;
import com.google.gson.Gson;
import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class DaoMongo implements DaoMongoService {

    @Value("${mongoUri}")
    String mongoUri;

    @Override
    public boolean isSavedGamePresentInMongoDB(String fileName) {
        String uri = mongoUri;
        MongoClient mongoClient = MongoClients.create(uri);
        MongoDatabase database = mongoClient.getDatabase("shopDB");

        BasicDBObject whereQuery = new BasicDBObject("name", fileName);

        return database.getCollection("saved_games").find(whereQuery).cursor().hasNext();
    }

    @Override
    public void cleanSavedGameMongodb(String playerName) {
        String uri = mongoUri;
        MongoClient mongoClient = MongoClients.create(uri);
        MongoDatabase database = mongoClient.getDatabase("shopDB");
        MongoCollection<Document> collection = database.getCollection("saved_games");
        Bson filter = Filters.eq("name", playerName + "SavedGame");
        collection.deleteMany(filter);
    }

    @Override
    public void loadSavedGameIntoMongodb(SavedGame savedGame, String playerName) {
        String uri = mongoUri;
        MongoClient mongoClient = MongoClients.create(uri);
        MongoDatabase database = mongoClient.getDatabase("shopDB");
        MongoCollection<Document> collection = database.getCollection("saved_games");
        Bson filter = Filters.eq("name", playerName + "SavedGame");
        collection.deleteMany(filter);
        Gson gson = new Gson();
        String json = gson.toJson(savedGame);
        Document document = Document.parse(json);
        document.put("name", playerName + "SavedGame");
        collection.insertOne(document);
    }

    @Override
    public SavedGame loadSavedGameFromMongodb(String playerName) {
        String uri = mongoUri;
        MongoClient mongoClient = MongoClients.create(uri);
        MongoDatabase database = mongoClient.getDatabase("shopDB");
        MongoCollection<Document> collection = database.getCollection("saved_games");
        Gson gson = new Gson();
        Bson filter = Filters.eq("name", playerName + "SavedGame");
        Document document = collection.find(filter).first();
        return gson.fromJson(document.toJson(), SavedGame.class);
    }




}
