import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
/*
        THIS CLASS IS JUST A SAMPLE TO HOW TO DEAL WITH THE DATABASE CLASS.
 */
public class test {
    public static void main(String[] args) {
        database mydb = new database();
        // You must start the connection first
        mydb.startConnection();
        // HashMap for pairs
        Map<String, Integer> mymap = new HashMap<>();
        // sample pairs
        mymap.put("hana",2);
        mymap.put("fatma",10);
        // make an array of those pairs
        ArrayList<Map<String, Integer>> ll = new ArrayList<>();
        ll.add(mymap);
        // sample insertion into the database
        mydb.add("ziad","4a3bola.com",ll,11,12);
        // sample deletion
        mydb.delete("ziad");
        // terminate
        mydb.endConnection();
    }
}
