package org.example.searchengineapp;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

public class connectWebPage
{
    private static MongoCollection<Document> collection;
    public MongoCollection<Document> connection()
    {
        // Creating a Mongo client
        MongoClient mongoClient = new MongoClient( "localhost" , 27017 );
        System.out.println("Created Mongo Connection successfully");

        MongoDatabase db = mongoClient.getDatabase("webPages");
        collection= db.getCollection("webPages_collection");

        return collection;
    }
    public double get_page_popularity(String url)
    {

        // Create a filter to match documents with the specified URL
        Document filter = new Document("url", url);

        // Find the document with the specified URL
        Document result = collection.find(filter).first();

        if (result != null) {
            // Document found, retrieve the "score" attribute
            Double score = result.getDouble("score");
            return score;
        } else {
            // Document not found
           return 0.0;
        }
    }
}
