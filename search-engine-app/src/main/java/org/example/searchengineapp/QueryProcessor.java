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
import java.util.HashMap;
import java.util.Map;

public class QueryProcessor {
    private final MongoCollection<Document> collection;
    private Ranker ranker;
    private Map<PairSS,List<String>> TagPos= new HashMap<>(); //map[<word,url>]=list of tags
    private Map<String,Integer> DF=new HashMap<>();
    private  Map<PairSS,Double> TF=new HashMap<>();
    private Map<String,Double> urlScore=new HashMap<>(); //map each url to corresponding score
    private static Vector<String> StopWords = new Vector<String>();
    private connectWebPage webpageConnect= new connectWebPage();

    public QueryProcessor()
    {
        // Establish connection and retrieve collection in the constructor
        connectDB c = new connectDB();
        this.collection = c.connection();
        this.ranker=new Ranker();
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

    public Set<String> Normalize(String text)
    {
        Set<String> stemmedWords = new HashSet<>();
        String temp=text.replaceAll("\"","");
        String[] words=temp.split(" ");
        PorterStemmer obj=new PorterStemmer();
        for(String str:words)
        {
            if(!StopWords.contains(str.toLowerCase()))
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
        List<Document> sec= (List<Document>) doc.get("url_list"); //list of url-lists of a single word
        for(Document d1:sec) //loop on url-list
        {
            String url=d1.getString("_id");
            Double n=d1.getDouble("tf-idf");

            //TODO:urlScore should be a map of <webPage object,score>
            //TODO: implement&call function set_webPage(url) that returns webpage object carrying its data
//            WebPage wp=getWebPage(url);
            //TODO: set_webPage should be in connectWebPage class
            
            /////////////TF-IDF directly in url-scores
            // Add entry if key doesn't exist, or do nothing if already exists
            urlScore.putIfAbsent(url, 0.0);
            // Update
            urlScore.put(url, urlScore.get(url) + n);
            ////////////

            PairSS temp1=new PairSS(word,url);
            //TF.put(temp1,n);
            List<Document> locs= (List<Document>) d1.get("loc");
            for (Document l:locs)
            {
                Set<String> taglist=l.keySet();
                String tag=taglist.iterator().next();
//                System.out.println(tag);
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
       // Integer val2 = doc.getInteger("count");
        //DF.put(word,val2);
    }
    public Set<Document> filter_collection(MongoCollection<Document> collection, Set<String> query_words)
    {
        // Set to store the matching documents (no duplicates)
        Set<Document> matchingDocs = new HashSet<>();

        // Iterate over the words and collect documents matching
        for(String str : query_words)
        {
            Document result = collection.find(Filters.eq("_id", str)).first();
            if(result!=null)
            { //result has the document with id=word
                System.out.println("found matching doc for word "+str+"doc is");
                matchingDocs.add(result);
                try {
                    mapping(result,str);
                }
                catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }
            else
            {
                System.out.println("no matching docs"+str);
            }
        }
        // Print or process the matching documents
//        for (Document matchingDoc : matchingDocs) { //for debugging
//            System.out.println(matchingDoc.toJson());
//        }
        return matchingDocs;

    }

    public List<WebPage> process_query(String query)
    {
        Set<String> normalizedQuery=Normalize(query); //stemming
        System.out.println("normalized");
        System.out.println(normalizedQuery);

        //finds matching documents and maps them to tf-idf score
        Set<Document> matching= filter_collection(collection,normalizedQuery);

        //////debugging///////
//        System.out.println("initial score(tf-idf)");
//        printScoreMap();
        //////////////////////////////

//        System.out.println(urlScore.size());
        if(query.contains("\""))
        {
            System.out.println("start phrasing");
            PhraseSearch phrasing=new PhraseSearch();
            if(query.contains("OR")||query.contains("AND")||query.contains("NOT"))
            {
                System.out.println(urlScore.size());
                phrasing.bonus(query,urlScore);
                System.out.println(urlScore.size());
            }
            else
            {
                phrasing.phraseSearch(query,urlScore);
            }
        }
//        System.out.println(urlScore.size());

        ////debugging
//        System.out.println("before tag ranking");
//        printScoreMap();
        //

        ranker.rankbyTag(TagPos,urlScore);

        ////debugging
//        System.out.println("after tag ranking");
//        printScoreMap();
        //

        ranker.rank_by_popularity(urlScore);

        ////debugging
//        System.out.println("after popularity ranking");
//        printScoreMap();
        //
        //TODO:this should be a list of webpages
        //web pages are objects that carry url,body,title attributes

        List<WebPage> rankedWebPages=webpageConnect.getWebPages(urlScore);

//        for(WebPage wp:rankedWebPages)
//        {
//            System.out.println(wp.getTitle());
//        }
//        System.out.println(rankedWebPages.size());

        //sort
        Collections.sort(rankedWebPages);

        ////sorted
//        System.out.println("after sorting");
//        for(WebPage wp:rankedWebPages)
//        {
//            System.out.println(wp.getTitle());
//            System.out.println(wp.getScore());
//        }
//
        for(WebPage wp:rankedWebPages)
        {
            if(query.contains("\"")&&(query.contains("AND")||query.contains("OR")||query.contains("NOT")))
            {
                System.out.println("BONUS");
                ranker.bonusParagraph(wp,query);
            }
            else
                ranker.setRelevantParagraph(wp,query);
        }

        //this should return list of ranked webPages
        return rankedWebPages;
    }
    private  void printScoreMap() //for debugging
    {
        for (String key : urlScore.keySet()) {
            Double value = urlScore.get(key);
            System.out.println("url: " + key + ", score: " + value);
        }
    }
    public void clean_up()
    {
       urlScore.clear();
       TF.clear();
       DF.clear();
    }
}
