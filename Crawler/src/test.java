import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class test {
    public static void main(String[] args) {
        database mydb = new database();
        mydb.startConnection();
        Map<String, Integer> mymap = new HashMap<>();
        mymap.put("hana",2);
        mymap.put("fatma",10);
        ArrayList<Map<String, Integer>> ll = new ArrayList<>();
        ll.add(mymap);
        mydb.add("ziad","4a3bola.com",ll,11,12);
        mydb.delete("ziad");
        mydb.endConnection();
    }
}
