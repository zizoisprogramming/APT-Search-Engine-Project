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

import java.io.*;
import java.util.*;

public class QueryProcessor {
    private final MongoCollection<Document> collection;
    private Ranker ranker;
    private Map<PairSS,List<String>> TagPos= new HashMap<>(); //map[<word,url>]=list of tags
    private Map<String,Integer> DF=new HashMap<>();
    private  Map<PairSS,Double> TF=new HashMap<>();
    private Map<String,Double> urlScore=new HashMap<>();
    private static Vector<String> StopWords = new Vector<String>();


    public QueryProcessor()
    {
        // Establish connection and retrieve collection in the constructor
        connectDB c = new connectDB();
        this.collection = c.connection();
        this.ranker=new Ranker();
        readStopWords("C:\\Users\\ASM EL Masrya\\Desktop\\ostor yarab\\APT-Search-Engine-Project\\search-engine-app\\src\\main\\java\\org\\example\\searchengineapp\\stop_words.txt");
        System.out.println(StopWords);
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

    public Set<String> Normalize(String text)
    {
        Set<String> stemmedWords = new HashSet<>();
        String temp=text.replaceAll("\"","");
        String[] words=temp.split(" ");
        PorterStemmer obj=new PorterStemmer();
        for(String str:words)
        {
            if(!StopWords.contains(str))
            {
                String stemmed = obj.stem(str);
                stemmedWords.add(stemmed.toLowerCase());
            }
        }
//        EnglishAnalyzer analyzer = new EnglishAnalyzer();
//        try (StringReader reader = new StringReader(text))
//        {
//            TokenStream tokenStream = analyzer.tokenStream(null, reader);
//            CharTermAttribute termAttribute = tokenStream.addAttribute(CharTermAttribute.class);
//            tokenStream.reset();
//            while (tokenStream.incrementToken())
//            {
//                String stemmedWord = termAttribute.toString();
//                stemmedWords.add(stemmedWord);
//            }
//        } catch (Exception e)
//        {
//            e.printStackTrace();
//        }

        return stemmedWords;
    }

    public void mapping(Document doc,String word) throws JsonProcessingException
    {
        List<Document> sec= (List<Document>) doc.get("url_list");
        for(Document d1:sec)
        {
            String url=d1.getString("_id");
            urlScore.put(url,0.0);
            Double n=d1.getDouble("tf");
            PairSS temp1=new PairSS(word,url);
            TF.put(temp1,n);
            List<Document> locs= (List<Document>) d1.get("loc");
            for (Document l:locs)
            {
                Set<String> taglist=l.keySet();
                String tag=taglist.iterator().next();
                System.out.println(tag);
//                Integer p=l.getInteger("pos");
//                PairSI temp=new PairSI(tag,p);

                List<String> psilist=TagPos.get(temp1);
                if(psilist==null)
                {
                    psilist=new ArrayList<String>();
                    psilist.add(tag);
                }
                else
                {
                    psilist.add(tag);
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
        System.out.println(urlScore.size());
        if(query.contains("\""))
        {
            PhraseSearch phrasing=new PhraseSearch();
            phrasing.phraseSearch(query,urlScore);
        }
        System.out.println(urlScore.size());

        System.out.println(urlScore);
        ranker.rankbyTag(TagPos,urlScore);
        System.out.println(urlScore);

        List<WebPage> ranked_webPages=ranker.rank_documents(matching);

        //this should return list of ranked webPages
        return normalizedQuery;
    }
}
