package org.example.searchengineapp;

import org.bson.Document;

import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class Ranker {
    public Set<String> rank_documents(Set<Document> relevant_docs)
    {
        //takes list of mongodb documents(enteries of db)
        //returns list of ranked webpages
        //retrieve popularity of web pages
        Map<String, Double> webpageScores = new HashMap<>();
        //giant loop and logic

        return  sortByScore(webpageScores);


    }
    public static Set<String> sortByScore(Map<String,Double> urlScores)
    {
        List<Map.Entry<String, Double>> entries = new ArrayList<>(urlScores.entrySet());
        entries.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));
        List<String> sortedKeys = new ArrayList<>();
        for (String key : urlScores.keySet())
        {
            sortedKeys.add(key);
        }

        return urlScores.keySet();
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

    public static void  rank_by_popularity(Map<String,Double> urlScore)
    {
        connectWebPage DB=new connectWebPage();
        DB.connection();

        //loop on map urls and get popularity for each and add it to the scores
        for (String key : urlScore.keySet()) {
           Double p=DB.get_page_popularity(key); //get popularity from db
           urlScore.put(key,urlScore.get(key)+p);
        }
    }
    //TODO:setRelevantParagraph
    //takes a map of webpage,score
    //set webpage.body=relevant paragraph
    public void setRelevantParagraph(WebPage wp,String query)
    {
        boolean phrasing = false;
        String text=query;
        if(query.contains("\""))
        {
            text=query.replaceAll("\"","");
            phrasing=true;
        }
        Integer index=0;
        if(phrasing)
        {
            index=wp.getBody().toLowerCase().indexOf(text.toLowerCase());
        }
        else
        {
            String[] words=text.split(" ");
//            PorterStemmer obj=new PorterStemmer();
            for(String str:words)
            {
//                String stemmed = obj.stem(str);
                if(wp.getBody().toLowerCase().indexOf(str.toLowerCase())>0)
                    index=wp.getBody().toLowerCase().indexOf(str.toLowerCase());
            }
        }
        System.out.println(index);
        Integer start=0,end=0;
        if(index-100>0)
        {
            start=index-100;
            while (start!=0&&wp.getBody().charAt(start)!=' ')
            {
                start--;
            }
        }
        if(index+100<wp.getBody().length())
        {
            end=index+100;
            while (end!=wp.getBody().length()-1&&wp.getBody().charAt(end)!=' ')
            {
                end++;
            }
        }
        String relevantParagraph=wp.getBody().substring(start,end)+"...";
        wp.setBody(relevantParagraph);
//        System.out.println(relevantParagraph);
    }
}
