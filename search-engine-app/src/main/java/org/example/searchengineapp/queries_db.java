package org.example.searchengineapp;


import com.mongodb.MongoClient;
import com.mongodb.MongoException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class queries_db
{
    private static MongoCollection<Document> collection;
    private static MongoClient mongoClient;
    public queries_db()
    {
        try {
            // Creating a Mongo client
            mongoClient = new MongoClient("localhost", 27017);
            MongoDatabase db = mongoClient.getDatabase("autocomplete");
            collection = db.getCollection("queries");
            System.out.println("Connected to MongoDB successfully");
        } catch (MongoException e) {
            System.err.println("Error connecting to MongoDB: " + e.getMessage());
        }
    }
    public void store_query(String query)
    {
        //store query in db if not already stored
        try {
            if (collection.countDocuments(Filters.eq("query", query)) == 0) {
                // if query not found in db,store it
                Document document = new Document("query", query);
                collection.insertOne(document);
                System.out.println("Query stored in the database: " + query);
            } else {
                System.out.println("Query already exists in the database: " + query);
            }
        } catch (MongoException e) {
            System.err.println("Error storing query in MongoDB: " + e.getMessage());
        }
    }
    public List<String> get_suggestions(String partial_query)
    {
        //takes string partial query and returns all queries with the prefix partial query
        List<String> suggestions = new ArrayList<>();
        try {
            String regex = "^" + partial_query;
            // find all docs matching regex pattern
            FindIterable<Document> cursor = collection.find(Filters.regex("query", regex));
            // iterate and add to the list
            for (Document document : cursor) {
                suggestions.add(document.getString("query"));
            }
        } catch (MongoException e) {
            System.err.println("Error retrieving suggestions from MongoDB: " + e.getMessage());
        }
        return suggestions;
    }
    public void close() {
        if (mongoClient != null) {
            mongoClient.close();
            System.out.println("MongoDB connection closed");
        }
    }

}

