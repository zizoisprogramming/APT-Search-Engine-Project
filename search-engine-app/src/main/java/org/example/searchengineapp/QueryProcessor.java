package org.example.searchengineapp;

import com.mongodb.client.MongoCollection;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.bson.Document;

import java.io.StringReader;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
public class QueryProcessor {
    private final MongoCollection<Document> collection;
    private Ranker ranker;

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
    public Set<String> process_query(String query)
    {
        Set<String> normalizedQuery=Normalize(query); //stemming
        
        SearchQuery search = new SearchQuery();
        List<Document> matching=search.filter_collection(collection,normalizedQuery);
        List<WebPage> ranked_webPages=ranker.rank_documents(matching);

        //this should return list of ranked webPages
        return normalizedQuery;
    }
}
