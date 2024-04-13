import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.MongoConfigurationException;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class database {
    // The object structure of the database is
    /*
    {
        "_id" : "word"
        "url_list":
            [
                {
                    "_id" : url,
                    "loc" : [(pos,index)],
                    "tf" :
                },
            ],
        "count" :
    }
    */
    private String databaseName = "Web-urls";
    private MongoClient mongoClient;
    private MongoDatabase mydatabase;
    private MongoCollection<Document> collection;
    // THIS IS A MUST TO CONNECT TO THE DATABASE
    public void startConnection() {
        String connectionString = "mongodb://localhost:27017";
        try {
            mongoClient = MongoClients.create(connectionString);
            System.out.println("Successfully connected to the database.");
            // Use the MongoClient instance
        } catch (MongoConfigurationException e) {
            // Handle the exception
            System.out.println("Error creating MongoDB client: " + e.getMessage());
            e.printStackTrace();
        }
        mydatabase = mongoClient.getDatabase(databaseName);
        collection = mydatabase.getCollection("urls");
    }

    // this add function
    // return false if it already exists
    // else adds the document and return true;
    public boolean add(String word, String site, ArrayList<Map<String,Integer>> locs , int tf, int count) {
        Document doc = collection.find(Filters.eq("_id", word)).first();
        if(doc != null) // If found before
            return false; // false
        List<Document> sites = new ArrayList<>(); // Initialize a list for urls
        // To be updated if there is change in structure
        sites.add(new Document("_id", site)
                .append("loc",locs)
                .append("tf", tf));
        // make a new document with _id the word
        doc = new Document("_id",word).append("url_list",sites).append("count",count);
        InsertOneResult result = collection.insertOne(doc); // insert
        if(result.wasAcknowledged()) {
            System.out.println("Inserted..");
            return true;
        }

        return false;
    }

    public boolean updateSites(String word, String site, ArrayList<Map<String,Integer>> locs , int tf) {
        // Find the doc first
        Document doc = collection.find(Filters.eq("_id", word)).first();
        if(doc == null) { // It doesn't exist
            System.out.println("Document doesn't exist.");
            return false;
        }
        // new object to be added to the array
        Document sites = new Document("_id",site)
                .append("loc",locs)
                .append("tf", tf);
        // push the object to the array of urls
        UpdateResult result = collection.updateOne(Filters.eq("_id",word),
                Updates.push("url_list", sites));
        // If modification happened
        if(result.getModifiedCount() == 1) {
            System.out.println("Updated..");
            return true;
        }
        return false;
    }

    public boolean delete(String word) {
        // Filter and get the document
        DeleteResult result = collection.deleteOne(Filters.eq("_id",word));
        // if an item is deleted
        if(result.getDeletedCount() == 1) {
            System.out.println("Deleted successfully");
            return true;
        }
        // no deletion took place
        return  false;
    }

    // update the count only
    public boolean updateCount(String word, int count) {

        UpdateResult result = collection.updateOne(Filters.eq("_id",word),
                Updates.set("count", count));
        if(result.getModifiedCount() == 1) {
            System.out.println("Updated..");
            return true;
        }
        System.out.println("Ouch..");
        return false;
    }
    // update the tf only
    public boolean updateTf(String word, int tf) {
        // update the tf this time
        UpdateResult result = collection.updateOne(Filters.eq("_id",word),
                Updates.set("tf", tf));
        // if # of modified is 1 then update took place
        if(result.getModifiedCount() == 1) {
            System.out.println("Updated..");
            return true;
        }
        // error
        System.out.println("Ouch..");
        return false;
    }
    // THIS IS A MUST TO END CONNECTION TO THE DATABASE
    public void endConnection() {
        mongoClient.close();
    }
}
