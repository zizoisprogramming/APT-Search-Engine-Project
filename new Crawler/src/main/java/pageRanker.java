import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class pageRanker {
    //data base
    private static final String MONGODB_DATABASE_NAME = "webPages";
    private static final String MONGODB_COLLECTION_NAME = "clean_run";

    //PR algorithm
    private static final double DAMPING_FACTOR = 0.85;
    private static final int MAX_ITERATIONS = 100;
    private static final double CONVERGENCE_THRESHOLD = 0.0001;
    private static Vector<String> links=new Vector<>();

    public static void main(String[] args) {
        try  {
            //connect to db
            MongoClient mongoClient=new MongoClient("localhost",27017);
            MongoDatabase database = mongoClient.getDatabase(MONGODB_DATABASE_NAME);
            MongoCollection<Document> collection = database.getCollection(MONGODB_COLLECTION_NAME);
            //store web pages to a list to run the algorithm
            //readLinks("./links.txt");
            //outgoingLinks();
            List<WebPage> webPages = retrieveWebPages(collection);
            //  extractOutgoingLinks(webPages,collection);
            //run the algorithm
            System.out.println("algorithm starting..");



            Map<String, Double> pageRankScores = computePageRank(webPages);
            //update popularity in the db
            updatePageRankScores(collection, pageRankScores);
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
    private static List<WebPage> retrieveWebPages(MongoCollection<Document> collection) {

        List<WebPage> webPages = new ArrayList<>();
        FindIterable<Document> documents = collection.find();

        for (Document document : documents) {
            String url = document.getString("url");
            List<String> outgoingLinks = (List<String>) document.get("outgoing_links");

            // Retrieve the score field as Object to handle both Integer and Double types
            Object scoreObj = document.get("score");
            double score;
            if (scoreObj instanceof Integer) {
                // If score is Integer, cast it to Double
                score = ((Integer) scoreObj).doubleValue();
            } else if (scoreObj instanceof Double) {
                // If score is already Double, simply cast it
                score = (Double) scoreObj;
            } else {
                // Handle other cases or throw an exception if necessary
                throw new IllegalArgumentException("Invalid score type");
            }

            WebPage webPage = new WebPage(url, outgoingLinks, score);
            webPages.add(webPage);
        }

        return webPages;
    }

    private static Map<String, Double> computePageRank(List<WebPage> webPages) {

        int n = webPages.size(); //number of docs
        Map<String, Double> pageRank = new HashMap<>();
        Map<String, Double> newPageRank = new HashMap<>();

        // Initialize PageRank scores
        for (WebPage page : webPages) {
            pageRank.put(page.getUrl(), 1.0 / n); //map each url to initial score
        }

        // Perform iterative PageRank computation
        for (int iter = 0; iter < MAX_ITERATIONS; iter++) {
            System.out.println("iteration "+iter);
            double sumDelta = 0.0;

            for (WebPage page : webPages) {
                double contribution = 0.0;
                for (WebPage linkingPage : webPages) {
                    if (linkingPage.getOutgoingLinks()!=null&&linkingPage.getOutgoingLinks().contains(page.getUrl())) {
                        contribution += pageRank.getOrDefault(linkingPage.getUrl(), 0.0)
                                / linkingPage.getOutgoingLinks().size();
                    }
                }
                double newScore = (1.0 - DAMPING_FACTOR) / n + DAMPING_FACTOR * contribution;
                sumDelta += Math.abs(newScore - pageRank.getOrDefault(page.getUrl(), 0.0));
                newPageRank.put(page.getUrl(), newScore);
            }

            // Check for convergence
            if (sumDelta < CONVERGENCE_THRESHOLD) {
                break;
            }

            // Update PageRank scores
            pageRank.putAll(newPageRank);
        }

        return pageRank;
    }
    private static void updatePageRankScores(MongoCollection<Document> collection, Map<String, Double> pageRankScores) {
        pageRankScores.forEach((url, score) -> {
            collection.updateOne(Filters.eq("url", url), new Document("$set", new Document("score", score)));
        });
    }

    private static void readLinks(String filePath) {
            try (BufferedReader br =new BufferedReader(new FileReader(filePath))){
                String line;
                // Read each line from the file until reaching the end
                while ((line = br.readLine()) != null) {
                    // Process the line here (e.g., add it to the shared variable)
                    links.addElement(line);

                }
            } catch(IOException e){
                e.printStackTrace();
            }
    }
    public static boolean isValidURL(String url) {
        try {
            new java.net.URL(url).toURI();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    public static void outgoingLinks()
    {
        webDB WEBDB;
        WEBDB = new webDB();
        Map<String, List<String>> outgoing_links = new HashMap<String, List<String>>();
        Map<String, Vector<String>> linksContent = new HashMap<String, Vector<String>>();
        int count = 0;
        int sizeError = 0;
        int unlocated = 0;
        for(String url: links)
        {
            System.out.println("Getting Links " + count);
            count++;
            if(count == 100)
            {
                //break;
            }
            try {
                if (!isValidURL(url)) {
                    System.out.println("Invalid URL: " + url);
                    continue;
                }
                Connection con = Jsoup.connect(url);
                org.jsoup.nodes.Document doc = con.get();
                if (con.response().statusCode() == 200) {
                    Vector<String> temp = new Vector<String>();
                    temp.add(doc.title());
                    temp.add(doc.body().text());
                    linksContent.put(url, temp);
                    List<String> L = new ArrayList<String>();
                    for (Element link : doc.select("a[href]")) {

                        String nextLink = link.absUrl("href");
                        if(!links.contains(nextLink))
                        {
                            continue;
                        }

                        L.add(nextLink);
                    }
                    outgoing_links.put(url, L);
                }
                if(!linksContent.containsKey(url) || linksContent.get(url).size() != 2)
                {
                    if (!linksContent.containsKey(url) )
                    {
                        unlocated++;
                    }
                    else
                    {
                        sizeError++;
                    }
                    WEBDB.write_url(url, "", "");
                    System.out.println("Error in Vector size: " + sizeError + " Error in location: " + unlocated);
                }
                else
                {
                    WEBDB.write_url(url, linksContent.get(url).get(0), linksContent.get(url).get(1));
                }
                if(!outgoing_links.containsKey(url))
                {
                    WEBDB.insert_to_DB(url, new ArrayList<String>());
                }
                else {
                    System.out.println("OutgoingSize " + outgoing_links.get(url).size());
                    WEBDB.insert_to_DB(url, outgoing_links.get(url));
                }
                linksContent.clear();
                outgoing_links.clear();

            } catch (IOException e) {

            }
        }

    }

}
class WebPage {
    private String url;
    private List<String> outgoingLinks;
    private double score;

    public WebPage(String url, List<String> outgoingLinks, double score) {
        this.url = url;
        this.outgoingLinks = outgoingLinks;
        this.score = score;
    }

    public String getUrl() {
        return url;
    }

    public List<String> getOutgoingLinks() {
        return outgoingLinks;
    }
    public double getScore() {
        return score;
    }
}
