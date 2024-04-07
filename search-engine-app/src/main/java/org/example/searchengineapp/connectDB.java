package org.example.searchengineapp;


import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import java.util.List;

public class connectDB
{
    public MongoCollection<Document> connection()
    {
        // Creating a Mongo client
        MongoClient mongoClient = new MongoClient( "localhost" , 27017 );
        System.out.println("Created Mongo Connection successfully");

        MongoDatabase db = mongoClient.getDatabase("indexer");
        MongoCollection<Document> collection= db.getCollection("dummy");

        return collection;
    }
}
