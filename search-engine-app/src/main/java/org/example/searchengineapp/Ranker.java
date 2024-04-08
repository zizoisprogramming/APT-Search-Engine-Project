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

        for (Map.Entry<WebPage, Integer> entry : entryList) {
            sortedWebpages.add(entry.getKey());
        }

        return sortedWebpages;

    }

}
