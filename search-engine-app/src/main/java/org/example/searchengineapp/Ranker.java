package org.example.searchengineapp;

import org.bson.Document;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class Ranker {
    private static Vector<String> StopWords = new Vector<String>();

    public Ranker()
    {
        readStopWords("C:\\Users\\ASM EL Masrya\\Desktop\\ostor yarab\\APT-Search-Engine-Project\\search-engine-app\\src\\main\\java\\org\\example\\searchengineapp\\stop_words.txt");
    }
    public static void readStopWords(String filePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            // Read each line from the file until reaching the end
            while ((line = br.readLine()) != null) {
                // Process the line here (e.g., add it to the shared variable)
                StopWords.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void rankbyTag(Map<PairSS,List<String>> TagPos,Map<String,Double> urlScores)
    {
        // Assign weights (adjust as needed)
        double h6Weight = 2;
        double h5Weight = 2;
        double h4Weight = 4;
        double h3Weight = 4;
        double h2Weight = 8;
        double h1Weight = 8;
        double titleWeight = 16;
        double paragraphWeight = 1;

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
                initialScore *= urlScores.get(entry.getKey().getsecond());
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
            Double existingScore = urlScore.get(key);

            // Check if the existing score is null
            if (existingScore != null) {
                Double p = DB.get_page_popularity(key); //get popularity from db
                 if (p != null)
                urlScore.put(key, urlScore.get(key) + p);
            }
        }
    }
    //TODO:setRelevantParagraph
    //takes a map of webpage,score
    //set webpage.body=relevant paragraph
    public void setRelevantParagraph(WebPage wp,String query)
    {
        boolean phrasing = false;
        String text=query;
        wp.setBody(wp.getBody().replaceAll("[\\p{Punct}]",""));
        if(query.contains("\""))
        {
            text=query.replaceAll("\"","");
            phrasing=true;
        }
        Integer index=0;
        if(phrasing)
        {
            index=wp.getBody().toLowerCase().indexOf(" "+text.toLowerCase()+" ");
        }
        else
        {
            String[] words=text.split(" ");
            index=stem(wp.getBody().toLowerCase(),words);
        }
//        System.out.println(index);
        Integer start=0,end=0;
        if (index != -1) { // Check if the index is found
            if (index - 100 > 0) {
                start = index - 100;
                while (start != 0 && wp.getBody().charAt(start) != ' ') {
                    start--;
                }
            }
            if (index + 100 < wp.getBody().length()) {
                end = index + 100;
                while (end != wp.getBody().length() - 1 && wp.getBody().charAt(end) != ' ') {
                    end++;
                }
            }
        }
        if (start >= 0 && end > start && end < wp.getBody().length()) {
            String relevantParagraph = wp.getBody().substring(start, end) + "...";
            wp.setBody(relevantParagraph);
        } else {
            // Handle case where relevant paragraph cannot be extracted
            wp.setBody("No relevant paragraph found");
        }
//        System.out.println(wp.getBody());
    }

    public void bonusParagraph(WebPage wp,String query)
    {
        String processedQuery=query;
        while(processedQuery.contains("NOT"))
        {
            int indexNot=processedQuery.indexOf("NOT");
            String temp=processedQuery.substring(indexNot+5);
            int lastindex=temp.indexOf("\"");
            StringBuilder sb = new StringBuilder(processedQuery);
            if (indexNot != -1) {
                sb.delete(indexNot, indexNot+6 + lastindex);
            }
            processedQuery = sb.toString();
        }

        String[] phrases= processedQuery.trim().replaceAll("\"","").split("\\s+AND\\s+|\\s+OR\\s+");
        Integer index=0;
        for(String phrase:phrases)
        {
            System.out.println(phrase);
            if(wp.getBody().toLowerCase().contains(" "+phrase.toLowerCase()+" "))
            {
                index=wp.getBody().toLowerCase().indexOf(" "+phrase.toLowerCase()+ " ");
                break;
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
    }

    private Integer stem(String body,String[] words)
    {
        Integer index=0;
        String regex = "[\\p{Punct}]";
        String[] bodyWords=body.toLowerCase().replaceAll(regex,"").split(" ");
        PorterStemmer obj=new PorterStemmer();
        String[] auxWords=new String[bodyWords.length];
        for(String str:words)
        {
            String stemmed = obj.stem(str).toLowerCase();
            for(int i=0;i<bodyWords.length;i++)
            {
                if(obj.stem(bodyWords[i]).equals(stemmed) && !StopWords.contains(str.toLowerCase())&&(body.indexOf(" "+bodyWords[i]+" ")!=-1||body.indexOf(" "+bodyWords[i])!=-1))
                {
//                    System.out.println("found");
                    return body.indexOf(" "+bodyWords[i]+" ")!=-1?body.indexOf(" "+bodyWords[i]+" "):body.indexOf(" "+bodyWords[i]);
                }
            }
        }
        return index;
    }
}
