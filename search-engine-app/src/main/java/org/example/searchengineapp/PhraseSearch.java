package org.example.searchengineapp;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PhraseSearch
{
    private final MongoCollection<Document> collection;
    connectWebPage webpagesConnect = new connectWebPage();


    public PhraseSearch()
    {
        this.collection = webpagesConnect.connection();
    }
    public void phraseSearch(String query, Map<String,Double> urlScore)
    {
        String modified_query=query.replace("\"", "");
        List<String> toberemoved=new ArrayList<>();
        for (Map.Entry<String, Double> entry : urlScore.entrySet())
        {
            Document result = webpagesConnect.find(entry.getKey());
            System.out.println("phrase searching "+result);
            if(result!=null)
            {
                System.out.println("now scanning"+entry.getKey());
                String body = result.getString("body");
                if (!body.toLowerCase().contains(modified_query))
                {
                    System.out.println("phrase found at"+entry.getKey());
                    System.out.println(body.contains("false"));
                    toberemoved.add(entry.getKey());
                }
            }
        }

        for(String str:toberemoved)
        {
            urlScore.remove(str);
        }
    }
}
