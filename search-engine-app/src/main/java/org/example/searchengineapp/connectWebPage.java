package org.example.searchengineapp;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
    public List<WebPage> getWebPages(Map<String,Double> urls)
    {
        List<WebPage> result=new ArrayList<>();
        for(Map.Entry<String,Double> url:urls.entrySet())
        {
            Document doc=collection.find(Filters.eq("url",url.getKey())).first();
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
