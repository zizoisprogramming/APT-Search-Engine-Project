import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.List;

public class webDB {
    MongoCollection<Document> collection;
    public webDB()
    {
        MongoClient mongoClient = new MongoClient( "localhost" , 27017 );
        System.out.println("Created Mongo Connection successfully");

        MongoDatabase db = mongoClient.getDatabase("webPages");
        collection= db.getCollection("dum");
    }

    public  void insert_to_DB(String url, List<String> outgoing_links)
    {
        try {
            // Create the query to find the document based on the URL
            Document query = new Document("url", url);

            // Create the update with the new outgoing links
            Document update = new Document("$set", new Document("outgoing_links", outgoing_links));

            // Perform the update
            collection.updateOne(query, update);

            System.out.println("Outgoing links added successfully for URL: " + url);
        } catch (Exception e) {
            System.err.println("Error adding outgoing links: " + e.getMessage());
        }

    }
    public void write_url(String url,String title,String body)
    {
        try {

            // Create the MongoDB document with the URL and outgoing links string
            Document webpage = new Document("url", url)
                    .append("score",0)
                    .append("title",title)
                    .append("body",body);

            // Insert the document into the webPagesCollection
            collection.insertOne(webpage);

            System.out.println("Document inserted successfully.");
        } catch (Exception e) {
            System.err.println("Error inserting document: " + e.getMessage());
        }
    }
}
