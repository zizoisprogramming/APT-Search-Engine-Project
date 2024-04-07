package org.example.searchengineapp;

import com.mongodb.client.MongoCursor;
import org.bson.Document;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.List;

public class SearchQuery
{
    public void Search(MongoCollection<Document> collection)
    {
        // Now perform your text search using $text operator
        Document searchQuery = new Document("word", "hi");
        MongoCursor<Document> cursor = collection.find(searchQuery).iterator();

        // Iterate over each document and print its contents
        while (cursor.hasNext())
        {
            Document document = cursor.next();

            String field1Value = document.getString("word");
            List<Document> field2Value = (List<Document>) document.get("docs");
            System.out.println(field2Value.size());
            Document str=field2Value.get(0);
            System.out.println(str);
            String val1 = str.getString("docname");
            Integer val2 = str.getInteger("TF");
            System.out.println(val1);
            System.out.println(val2);
        }
    }
}
