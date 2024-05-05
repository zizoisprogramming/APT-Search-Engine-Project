import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.MongoConfigurationException;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCursor;
import org.bson.Document;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RankerOffline {
    //loops on all indexer db
    //extract count (DF)
    //DF/no of docs =IDF
    //for each element on url_list
    //extract tf
    //add attribute "tf-idf" =tf*idf
    private static String db_name="Web-urls";
    private static String collection_name="clean_indexer";
    private static MongoClient mongoClient;
    private static  MongoCollection<Document> collection;
    private static final int no_of_docs=6000;

    public static void main(String [] args)
    {
        connect_db();
        calculate_TF_IDF();
    }
    private static void  connect_db()
    {
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
        MongoDatabase mydatabase = mongoClient.getDatabase(db_name);
        collection = mydatabase.getCollection(collection_name);
    }
    private static void calculate_TF_IDF()
    {
        MongoCursor<Document> cursor = collection.find().iterator();
        try {
            while (cursor.hasNext())
            {
                Document document = cursor.next();

                // count,idf calculations
                int DF = (int) document.get("count");
                double idf=(double)no_of_docs/DF;
                double IDF=Math.log(idf);

                //extract url list
                List<Document> urlList = (List<Document>) document.get("url_list");


                for (Document urlDocument : urlList) {
                    // update the document to have tf-idf
                    String url = urlDocument.getString("url");
                    Number tfNumber = (Number) urlDocument.get("tf");
                    double tf = tfNumber.doubleValue();

                    double tf_idf = tf * IDF;
                    urlDocument.put("tf-idf", tf_idf);
                }

                Object id = document.get("_id");
                //update url list in db
                collection.updateOne(
                        new Document("_id", id),
                        new Document("$set", new Document("url_list", urlList))
                );
            }
        } finally {
            cursor.close();
            mongoClient.close();
        }
    }



}
