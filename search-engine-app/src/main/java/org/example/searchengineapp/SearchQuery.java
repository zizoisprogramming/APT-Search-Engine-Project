package org.example.searchengineapp;

import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

import java.io.StringReader;
import java.util.HashSet;
import java.util.Set;
public class SearchQuery
{
    public void Search(MongoCollection<Document> collection)
    {
        // Now perform your text search using $text operator
        Document searchQuery = new Document("word", "hi"); //loop on stemmed words
        MongoCursor<Document> cursor = collection.find(searchQuery).iterator();

        // Iterate over each document and print its contents
        while (cursor.hasNext())
        {
            //document that has target word
            //extract info and store in object
            //for a specific word& webpage => TF&DF
            //object
            //word , list of document, Df



            //push object to list of web pages //ely hn3mlha ranking
            //
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
        //return list of objects
    }
    public List<Document> filter_collection(MongoCollection<Document> collection, Set<String> query_words)
    {

        // List to store the matching documents
        List<Document> matchingDocs = new ArrayList<>();

        // Iterate over the collection and collect documents matching the filter
        for (Document doc : collection.find()) {
            if (query_words.contains(doc.getString("word"))) {
                // Add the matching document to the list
                matchingDocs.add(doc);
            }
        }
        // Print or process the matching documents
        for (Document matchingDoc : matchingDocs) { //for debugging
            System.out.println(matchingDoc.toJson());
        }
        return matchingDocs;

    }
}
