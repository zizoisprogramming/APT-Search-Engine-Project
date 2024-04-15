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

    public PhraseSearch()
    {
        connectWebPage c = new connectWebPage();
        this.collection = c.connection();
    }
    public void phraseSearch(String query, Map<String,Double> urlScore)
    {
        query.replace('\"',' ');
        List<String> toberemoved=new ArrayList<>();
        for (Map.Entry<String, Double> entry : urlScore.entrySet())
        {
            Document result = collection.find(Filters.eq("url", entry.getKey())).first();
            if(result!=null) {
                String body = result.getString("body");
                if (!body.contains(query))
                {
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
