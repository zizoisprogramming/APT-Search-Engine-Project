import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class pageRanker {
    //data base
    private static final String MONGODB_DATABASE_NAME = "webPages";
    private static final String MONGODB_COLLECTION_NAME = "webPages_collection";

    //PR algorithm
    private static final double DAMPING_FACTOR = 0.85;
    private static final int MAX_ITERATIONS = 100;
    private static final double CONVERGENCE_THRESHOLD = 0.0001;

    public static void main(String[] args) {
        try  {
            //connect to db
            MongoClient mongoClient=new MongoClient("localhost",27017);
            MongoDatabase database = mongoClient.getDatabase(MONGODB_DATABASE_NAME);
            MongoCollection<Document> collection = database.getCollection(MONGODB_COLLECTION_NAME);
            //store web pages to a list to run the algorithm
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
    private static void extractOutgoingLinks(List<WebPage> pages, MongoCollection<Document> collection) {
        for (WebPage page : pages) {
            try {
                // Fetch the HTML content of the page using Jsoup
                org.jsoup.nodes.Document doc = Jsoup.connect(page.getUrl()).get();

                // Extract all <a> elements (links) from the page
                Elements links = doc.select("a[href]");

                // Iterate over the links and extract the "href" attribute
                List<String> outgoing=new ArrayList<String>();
                for (Element link : links) {
                    String outgoingLink = link.absUrl("href");
                    outgoing.add(outgoingLink); // Update WebPage object with outgoing link
                }

                // Update the document in the collection with the updated outgoing links
                collection.updateOne(Filters.eq("url", page.getUrl()), Updates.set("outgoing_links", outgoing));

            } catch (IOException e) {
                // Handle any IO exceptions
                e.printStackTrace();
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