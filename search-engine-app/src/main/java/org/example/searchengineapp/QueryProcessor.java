package org.example.searchengineapp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import javassist.compiler.ast.Pair;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.fst.PairOutputs;
import org.bson.Document;

import java.io.DataInput;
import java.io.StringReader;
import java.util.*;

public class QueryProcessor {
    private final MongoCollection<Document> collection;
    private Ranker ranker;
    private Map<PairSS,List<PairSI>> TagPos= new HashMap<>();
    private Map<String,Integer> DF=new HashMap<>();
    private  Map<PairSS,Integer> TF=new HashMap<>();
    private Map<String,Integer> urlScore=new HashMap<>();

    public QueryProcessor()
    {
        // Establish connection and retrieve collection in the constructor
        connectDB c = new connectDB();
        this.collection = c.connection();
        this.ranker=new Ranker();
    }

    public Set<String> Normalize(String text)
    {
        Set<String> stemmedWords = new HashSet<>();
        EnglishAnalyzer analyzer = new EnglishAnalyzer();
        try (StringReader reader = new StringReader(text))
        {
            TokenStream tokenStream = analyzer.tokenStream(null, reader);
            CharTermAttribute termAttribute = tokenStream.addAttribute(CharTermAttribute.class);
            tokenStream.reset();
            while (tokenStream.incrementToken())
            {
                String stemmedWord = termAttribute.toString();
                stemmedWords.add(stemmedWord);
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }

        return stemmedWords;
    }

    public void mapping(Document doc,String word) throws JsonProcessingException
    {
        List<Document> sec= (List<Document>) doc.get("doc");
        for(Document d1:sec)
        {
            String url=d1.getString("_id");
            urlScore.put(url,0);
            Integer n=d1.getInteger("tf");
            PairSS temp1=new PairSS(word,url);
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
        Integer val2 = doc.getInteger("count");
        DF.put(word,val2);
    }
    public List<Document> filter_collection(MongoCollection<Document> collection, Set<String> query_words)
    {
        // List to store the matching documents
        List<Document> matchingDocs = new ArrayList<>();

        // Iterate over the words and collect documents matching
        for(String str : query_words)
        {
            Document result = collection.find(Filters.eq("_id", str)).first();
            if(result!=null)
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

    public Set<String> process_query(String query)
    {
        Set<String> normalizedQuery=Normalize(query); //stemming
        
        List<Document> matching= filter_collection(collection,normalizedQuery);
        List<WebPage> ranked_webPages=ranker.rank_documents(matching);
        System.out.println(TF);
        System.out.println(DF);
        System.out.println(TagPos);
        System.out.println(urlScore);

        //this should return list of ranked webPages
        return normalizedQuery;
    }
}
