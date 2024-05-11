package org.example.searchengineapp;


import com.mongodb.MongoClient;
import com.mongodb.MongoClientException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import java.util.List;

public class connectDB //indexer db
{
    public MongoCollection<Document> connection()
    {
        try {

            // Creating a Mongo client
            MongoClient mongoClient = new MongoClient("localhost", 27017);
            System.out.println("Created Mongo Connection successfully");

            MongoDatabase db = mongoClient.getDatabase("Web-urls");
            MongoCollection<Document> collection = db.getCollection("urls");

            return collection;
        }
        catch (MongoClientException e)
        {
            e.printStackTrace();
            return null;
        }
    }
}
