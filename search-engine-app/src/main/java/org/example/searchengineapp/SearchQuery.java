package org.example.searchengineapp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mongodb.client.MongoCursor;
import org.bson.Document;
import com.mongodb.client.MongoCollection;
import java.util.*;

public class SearchQuery
{
    private Map<PairSS,List<PairSI>> TagPos= new HashMap<>();
    private Map<String,Integer> DF=new HashMap<>();
    private  Map<PairSS,Integer> TF=new HashMap<>();
    private Set<String> urls=new HashSet<>();

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
    public void mapping(Document doc,String word) throws JsonProcessingException
    {
        List<Document> field2Value = new ArrayList<>();
        field2Value.add((Document) doc.get(word));
        Document str=field2Value.get(0);
        List<Document> sec= (List<Document>) str.get("details");
        for(Document d1:sec)
        {
            String s=d1.getString("url");
            urls.add(s);
            Integer n=d1.getInteger("tf");
            PairSS temp1=new PairSS(word,s);
            TF.put(temp1,n);
            List<Document> locs= (List<Document>) d1.get("locs");
            for (Document l:locs)
            {
                String tag=l.getString("tag");
                Integer p=l.getInteger("pos");
                PairSI temp=new PairSI(tag,p);

                List<PairSI> psilist=TagPos.get(temp1);
                if(psilist==null)
                {
                    psilist=new ArrayList<PairSI>();
                    psilist.add(temp);
                }
                else
                {
                    psilist.add(temp);
                }
                TagPos.put(temp1,psilist);
            }
        }
        Integer val2 = str.getInteger("count");
        DF.put(word,val2);
    }
    public List<Document> filter_collection(MongoCollection<Document> collection, Set<String> query_words)
    {

        // List to store the matching documents
        List<Document> matchingDocs = new ArrayList<>();

        // Iterate over the words and collect documents matching
        for(String str : query_words)
        {
            Document query = new Document(str, new Document("$exists", true));
            Document result = collection.find(query).first();

            if(collection.countDocuments(query)>0)
            {
                matchingDocs.add(result);
                try {
                    mapping(result,str);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        // Print or process the matching documents
        for (Document matchingDoc : matchingDocs) { //for debugging
            System.out.println(matchingDoc.toJson());
        }
        return matchingDocs;

    }
}
