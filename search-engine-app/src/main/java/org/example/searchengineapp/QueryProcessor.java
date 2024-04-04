package org.example.searchengineapp;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.StringReader;
import java.util.HashSet;
import java.util.Set;
public class QueryProcessor {

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
        Set<String> normalizedQuery=Normalize(query);
        return normalizedQuery;
    }
}
