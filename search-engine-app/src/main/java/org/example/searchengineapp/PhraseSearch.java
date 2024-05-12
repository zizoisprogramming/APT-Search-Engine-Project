package org.example.searchengineapp;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import org.bson.Document;

import java.util.*;

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
//                System.out.println(body);
                if (!body.toLowerCase().contains(" "+modified_query.toLowerCase()+" "))
                {
                    System.out.println(body.contains("phrase not found"));
                    toberemoved.add(entry.getKey());
                }
            }
        }

        for(String str:toberemoved)
        {
            urlScore.remove(str);
        }
    }

    public void bonus(String query, Map<String,Double> urlScore)
    {
        //AND higher precedence than OR
        Set<String> toberemoved=new HashSet<>();
        Map<String,String> urlBody=new HashMap<>();
        String processedQuery=query;

        //turning database into Map<"url","body">
        for (Map.Entry<String, Double> entry : urlScore.entrySet())
        {
            Document result = webpagesConnect.find(entry.getKey());
            if(result!=null)
            {
                urlBody.put(entry.getKey(),result.getString("body"));
            }
        }

        //if contains NOT add them to toberemoved
        if(query.contains("NOT")) {
            String notPhrase="TO BE REMOVED";
            while(processedQuery.contains("NOT"))
            {
                int indexNot=processedQuery.indexOf("NOT");
                String temp=processedQuery.substring(indexNot+5);
                int lastindex=temp.indexOf("\"");
                StringBuilder sb = new StringBuilder(processedQuery);
                if (indexNot != -1) {
                    sb.delete(indexNot, indexNot+6 + lastindex);
                }
                notPhrase=processedQuery.substring(indexNot+3,indexNot+6 + lastindex);
                System.out.println("NOT PHRASE");
                System.out.println(urlBody.size());
                notPhrase=notPhrase.replaceAll("\"","").trim();
                System.out.println(notPhrase);

                processedQuery = sb.toString().trim();
                for (Map.Entry<String, String> entry : urlBody.entrySet()) {
                    if (entry.getValue().toLowerCase().contains(" "+notPhrase.toLowerCase()+" "))
                    {
                        toberemoved.add(entry.getKey());
                    }
                }
            }
        }
        if(!processedQuery.contains("OR")&&!processedQuery.contains("AND"))
        {
            System.out.println(processedQuery);
            System.out.println("to look for");
            processedQuery=processedQuery.replaceAll("\"","").trim();
            for (Map.Entry<String, String> entry : urlBody.entrySet()) {
                if (!entry.getValue().toLowerCase().contains(" "+processedQuery.toLowerCase()+" "))
                {
                    toberemoved.add(entry.getKey());
                }
            }
        }

        if(processedQuery.contains("AND")&&!processedQuery.contains("OR"))
        {
            String modifiedQuery=processedQuery.replaceAll("\"","");
            String[] Phrases=modifiedQuery.split("AND");
            for(String str:Phrases)
            {
                System.out.println("AND PHRASE");
                System.out.println(str.trim().toLowerCase());
                for (Map.Entry<String, String> entry : urlBody.entrySet()) //key is url value is body
                {
                    System.out.println("now scanning"+entry.getKey());
                    if (!entry.getValue().toLowerCase().contains(" "+str.trim().toLowerCase()+" "))
                    {
                        System.out.println("phrase not found");
                        toberemoved.add(entry.getKey());
                    }
                }

            }
        }
        if(!processedQuery.contains("AND")&&processedQuery.contains("OR"))
        {
//            System.out.println("here fatma");
            String modifiedQuery=processedQuery.replaceAll("\"","");
            String[] Phrases=modifiedQuery.split("OR");
            for (Map.Entry<String, String> entry : urlBody.entrySet()) //key is url value is body
            {
                Boolean Remove = true;
                for(String str:Phrases)
                {
                    if (entry.getValue().toLowerCase().contains(" "+str.trim().toLowerCase()+" "))
                    {
//                        System.out.println("phrase found");
                        Remove=false;
                        break;
                    }
                }
                if(Remove)
                {
                    toberemoved.add(entry.getKey());
                }
            }
//            System.out.println("here fatma");
        }
        if(query.contains("AND")&&query.contains("OR"))
        {
            // Find the position of "AND" and "OR"
            int andIndex = query.indexOf("AND");
            int orIndex = query.indexOf("OR");
            // Extract the substrings
            String firstPhrase = "";
            String secondPhrase = "";
            String thirdPhrase="";
            if(andIndex<orIndex) { //(1 and 2) or 3
                firstPhrase = query.substring(0, andIndex).trim().toLowerCase().replaceAll("\"",""); //1
                secondPhrase = query.substring(andIndex + 3, orIndex).trim().toLowerCase().replaceAll("\"","");  //2
                thirdPhrase = query.substring(orIndex + 2).trim().toLowerCase().replaceAll("\"","");  //3
            }
            else  //3 or (2 and 1)
            {
                firstPhrase = query.substring(andIndex+3).trim().toLowerCase().replaceAll("\"","");
                secondPhrase = query.substring(orIndex+2, andIndex).trim().toLowerCase().replaceAll("\"","");
                thirdPhrase = query.substring(0,orIndex).trim().toLowerCase().replaceAll("\"","");
            }
            System.out.println("phrases: "+firstPhrase+secondPhrase+thirdPhrase);


            for (Map.Entry<String, String> entry : urlBody.entrySet()) //key is url value is body
            {
                String tobeSearched=entry.getValue().toLowerCase();
                if (!((tobeSearched.contains(" "+firstPhrase+" ")&&tobeSearched.contains(" "+secondPhrase+" "))||tobeSearched.contains(" "+thirdPhrase+" ")))
                {
                    System.out.println("not found");
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
