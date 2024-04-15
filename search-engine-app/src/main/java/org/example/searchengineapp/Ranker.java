package org.example.searchengineapp;

import org.bson.Document;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
public class Ranker {
    public List<WebPage> rank_documents(List<Document> relevant_docs)
    {
        //takes list of mongodb documents(enteries of db)
        //returns list of ranked webpages
        Map<WebPage, Integer> webpageScores = new HashMap<>();
        //giant loop and logic

        return  sortByScore(webpageScores);


    }
    private List<WebPage> sortByScore(Map<WebPage,Integer> webPages)
    {
        //convert map to list
        List<Map.Entry<WebPage, Integer>> entryList = new ArrayList<>(webPages.entrySet());

        // Sorting the entry list based on score (higher score comes 1st)
        entryList.sort((entry1, entry2) -> entry2.getValue().compareTo(entry1.getValue()));

        // list of the sorted webpages
        List<WebPage> sortedWebpages = new ArrayList<>();

        for (Map.Entry<WebPage, Integer> entry : entryList)
        {
            sortedWebpages.add(entry.getKey());
        }

        return sortedWebpages;

    }

    public void rankbyTag(Map<PairSS,List<String>> TagPos,Map<String,Double> urlScores)
    {
        // Assign weights (adjust as needed)
        double h6Weight = 0.2;
        double h5Weight = 0.22;
        double h4Weight = 0.24;
        double h3Weight = 0.26;
        double h2Weight = 0.28;
        double h1Weight = 0.3;
        double titleWeight = 0.4;
        double paragraphWeight = 0.1;

        double initialScore = 0.0;
        for (Map.Entry<PairSS, List<String>> entry : TagPos.entrySet())
        {
            initialScore = 0.0;
            for(String s:entry.getValue())
            {
                if(s.equals("title"))
                {
                    initialScore=initialScore+titleWeight;
                }
                if(s.equals("h1"))
                {
                    initialScore+=h1Weight;
                }
                if(s.equals("h2"))
                {
                    initialScore+=h2Weight;
                }
                if(s.equals("h3"))
                {
                    initialScore+=h3Weight;
                }
                if(s.equals("h4"))
                {
                    initialScore+=h4Weight;
                }
                if(s.equals("h5"))
                {
                    initialScore+=h5Weight;
                }
                if(s.equals("h6"))
                {
                    initialScore+=h6Weight;
                }
                if(s.equals("text"))
                {
                    initialScore+=paragraphWeight;
                }
            }
            if(urlScores.containsKey(entry.getKey().getsecond()))
            {
                initialScore += urlScores.get(entry.getKey().getsecond());
                urlScores.put(entry.getKey().getsecond(), initialScore);
            }
        }
    }
}
