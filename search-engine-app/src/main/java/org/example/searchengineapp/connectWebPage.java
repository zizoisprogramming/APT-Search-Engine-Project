package org.example.searchengineapp;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;

import java.util.*;

public class connectWebPage
{
    private static MongoCollection<Document> collection;
    public MongoCollection<Document> connection()
    {
        try {
            // Creating a Mongo client
            MongoClient mongoClient = new MongoClient("localhost", 27017);
            MongoDatabase db = mongoClient.getDatabase("webPages");
            collection = db.getCollection("clean_run");
            System.out.println("Created Mongo Connection successfully");

            return collection;
        }
        catch (MongoClientException e)
        {
           e.printStackTrace();
           return null;
        }
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
    public Document find(String url)
    {
        // Create a filter to match documents with the specified URL
        Document filter = new Document("url", url);

        // Find the document with the specified URL
        Document result = collection.find(filter).first();
        return  result;
    }
    public List<WebPage> getWebPages(Map<String,Double> urls)
    {
        List<WebPage> result=new ArrayList<>();
        Set<Map.Entry<String, Double>> entries = new HashSet<>(urls.entrySet());
        for(Map.Entry<String,Double> url:entries)
        {
            // Create a filter to match documents with the specified URL
//            Document filter = new Document("url", url.getKey());

            // Find the document with the specified URL
            Document doc = find(url.getKey());
//            System.out.println(url.getKey());
//            System.out.println(doc);
            if(doc!=null)
            {
                String title = doc.getString("title");
                String body = doc.getString("body");
                WebPage temp = new WebPage(url.getKey(), body, title,url.getValue());
                result.add(temp);
            }
        }
        return  result;
    }
}
