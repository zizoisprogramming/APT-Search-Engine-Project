import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoClients;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.IOException;
//import java.sql.ShardingKey;
import java.util.*;

import org.jsoup.Connection;
import java.io.File;
import org.jsoup.Jsoup;
//import org.jsoup.nodes.Document;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
//import java.sql.Connection;
import java.util.Vector;

public class indexer {
    public static PorterStemmer porterStemmer = new PorterStemmer();
    private static Vector<String> StopWords = new Vector<String>();
    private static Map<String, Vector<Map<String, Integer>>> WordsPerSite = new HashMap<>();
    //private static Map <String, Map<String, Integer>> WordsPerSite = new HashMap<String, Map<String, Integer>>(new HashMap<String, Map<String, Integer>>());
    private static Vector<String> URLs = new Vector<String>();
    private static Vector<String> indexed = new Vector<String>();
    public static void readIndexed(String filePath) {
        try (BufferedReader br =new BufferedReader(new FileReader(filePath))){
            String line;
            // Read each line from the file until reaching the end
            while ((line = br.readLine()) != null) {
                // Process the line here (e.g., add it to the shared variable)

                indexed.add(line);
            }
        } catch(IOException e){
            e.printStackTrace();
        }

    }
    public static void readLinks(String filePath) {
        try (BufferedReader br =new BufferedReader(new FileReader(filePath))){
            String line;
            // Read each line from the file until reaching the end
            while ((line = br.readLine()) != null) {
                // Process the line here (e.g., add it to the shared variable)
                if (indexed.contains(line))
                    continue;
                URLs.add(line);
            }
        } catch(IOException e){
            e.printStackTrace();
        }

    }
    private static int addWord(String words, String type, int index) {
        for(String word : words.split(" ")) {
            word = word.toLowerCase();
            if (StopWords.contains(word)) {
                continue;
            }
            // Stem the word from commas, periods, etc. using regex
            // HABET TANDEEF
            word = word.replaceAll("[^a-zA-Z0-9]", "");

            word = porterStemmer.stem(word);
            if (word.length() == 0)
            {
                continue;
            }
            if (StopWords.contains(word)) {
                continue;
            }
            index++;
            // Check if the word exists in WordsPerSite


            if (WordsPerSite.containsKey(word)) {
                // If the word exists, get its map and update it
                Vector<Map<String, Integer>> wordMap = WordsPerSite.get(word);
                Map<String, Integer> temp = new HashMap<>();
                temp.put(type, index);
                wordMap.add(temp);
            } else {
                // If the word doesn't exist, create a new map and add it to WordsPerSite
                Map<String, Integer> wordMap = new HashMap<>();
                wordMap.put(type, index);
                Vector<Map<String, Integer>> temp = new Vector<>();
                temp.add(wordMap);
                WordsPerSite.put(word, temp);

            }
        }
        return index;
    }
    private static database mydb = new database();
    public static boolean isValidURL(String url) {
        try {
            new java.net.URL(url).toURI();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    private static int getContent(String url) {
        /* connect to the url and get the content */
        int wordsCount = 0;
        int index = 0;
        try {
            if(!isValidURL(url))
                return 0;
            Connection con = Jsoup.connect(url);
            Document doc = con.get();

            // Get title
            String title = doc.title();

            // Add title to WordsPerSite
            index = addWord(title, "title", index);
            // Get all elements in the document
            Elements allElements = doc.getAllElements();

            // Define a set to keep track of encountered texts
            Set<String> encounteredTexts = new HashSet<>();
            for (Element element : allElements) {
                if (element.tagName().matches("h[1-6]")) {
                    String headerText = element.text();
                    // Check if the header text has not been encountered before
                    if (!encounteredTexts.contains(headerText)) {
                        // Add the header text to the set of encountered texts
                        encounteredTexts.add(headerText);
                        // Add the header text to your index
                        index = addWord(headerText, element.tagName(), index);
                    }
                }
                // Check if the element is a paragraph
                else if (element.tagName().equals("p")) {
                    String paragraphText = element.text();
                    // Check if the paragraph text has not been encountered before
                    if (!encounteredTexts.contains(paragraphText)) {
                        // Add the paragraph text to the set of encountered texts
                        encounteredTexts.add(paragraphText);
                        // Add the paragraph text to your index
                        index = addWord(paragraphText, "text", index);
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return wordsCount = index;
    }
    public static void index() {

        for (String url : URLs) {
            // Get the content of the page
            int words = getContent(url);
            // Tokenize the content
            for(Map.Entry<String, Vector<Map<String, Integer>>> entry : WordsPerSite.entrySet()) {
                String word = entry.getKey();
                Vector<Map<String, Integer>> wordMap = entry.getValue();
                ArrayList<Map<String, Integer>> List = new ArrayList<>();
                for(Map<String, Integer> map : wordMap) {
                    List.add(map);
                }
                float TF = (float) wordMap.size() / words;
                if(!mydb.add(word, url, List, TF, 0))
                    mydb.updateSites(word, url, List, TF);
            }
            WordsPerSite.clear();

            //add link to indexed txt
            try {
                FileWriter fw = new FileWriter("indexed.txt", true);
                BufferedWriter bw = new BufferedWriter(fw);
                bw.write(url);
                bw.newLine();
                bw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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
    public static void main(String[] args) {
        readIndexed("indexed.txt");
        readLinks("links.txt");
        readStopWords("stop_words.txt");
        mydb.startConnection();
        //get time
        long startTime = System.currentTimeMillis();
        index();
        long endTime = System.currentTimeMillis();
        long timeElapsed = endTime - startTime;
        float minutes = timeElapsed / 60000;
        System.out.println("Time elapsed 6k " + minutes + " minutes");
        mydb.endConnection();

    }
}