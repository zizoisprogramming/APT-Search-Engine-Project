import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.*;

import org.jsoup.Connection;
import java.io.File;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

class Crawler {
    // This function reads the starting file (Seed set)
    private static final String LINKS_OUTPUT_FILE = "links.txt"; // File for storing crawled links
    private static final String COMPACT_STRINGS_OUTPUT_FILE = "compact_strings.txt"; // File for storing compact strings
    public static void readSeed(String filePath, boolean ContinueCrawling) {
        boolean linksFileExists = new File(LINKS_OUTPUT_FILE).exists();
        boolean compactStringsFileExists = new File(COMPACT_STRINGS_OUTPUT_FILE).exists();
        if (ContinueCrawling && linksFileExists && compactStringsFileExists) {
            System.out.println("Continuing Crawling");
            readLinks(LINKS_OUTPUT_FILE);
            readCompactStrings(COMPACT_STRINGS_OUTPUT_FILE);
            System.out.println("Size equal to: " + SharedVars.numFiles);

        } else {
            System.out.println("Starting Crawling");

            readLinks(filePath);
        }
    }
    private static void readLinks(String filePath) {
        synchronized (SharedVars.linksLock) {
            try (BufferedReader br =new BufferedReader(new FileReader(filePath))){
                String line;
                // Read each line from the file until reaching the end
                while ((line = br.readLine()) != null) {
                    // Process the line here (e.g., add it to the shared variable)
                    SharedVars.files.addElement(line);
                    synchronized(SharedVars.indexLock) {
                        SharedVars.numFiles += 1;
                    }
                }
            } catch(IOException e){
                e.printStackTrace();
            }
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
        for(String url: SharedVars.files)
        {
            System.out.println("Getting Links " + count);
            count++;
            if(count == 100)
            {
                //break;
            }
            try {
                Connection con = Jsoup.connect(url);
                Document doc = con.get();
                if (con.response().statusCode() == 200) {
                    Vector<String> temp = new Vector<String>();
                    temp.add(doc.title());
                    temp.add(doc.body().text());
                    linksContent.put(url, temp);
                    List<String> links = new ArrayList<String>();
                    for (Element link : doc.select("a[href]")) {
                        String nextLink = link.absUrl("href");
                        if(!SharedVars.files.contains(nextLink))
                        {
                            continue;
                        }

                        links.add(nextLink);
                    }
                    outgoing_links.put(url, links);
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
        int DBinserted = 0;
        /*for(String url: SharedVars.files) {
            System.out.println("Inserting to DB: " + DBinserted);
            DBinserted++;
            if(DBinserted == 100)
            {
                break;
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
        }*/
    }

    private static void readCompactStrings(String filePath) {
        synchronized (SharedVars.stringsLock) {
            try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
                String line;
                // Read each line from the file until reaching the end
                while ((line = br.readLine()) != null) {
                    // Process the line here (e.g., add it to the shared variable)
                    SharedVars.CompactStrings.addElement(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        // Program logic
        String filePath = "./seed.txt";
        readSeed(filePath, true);

        // SharedVars.numThreads = Integer.parseInt(args[0]); // Set the number of threads
        SharedVars.numThreads = 4; // Set the number of threads
        Thread[] spiders = new Thread[SharedVars.numThreads];
        // make new threads
        for(int i = 0; i < SharedVars.numThreads; i++) {
            spiders[i] = new Thread(new spiderWebs(i));
            spiders[i].setName("Thread " + i);
        }

        try {
            SharedVars.linksWriter = new BufferedWriter(new FileWriter(LINKS_OUTPUT_FILE, true));
            SharedVars.compactStringsWriter = new BufferedWriter(new FileWriter(COMPACT_STRINGS_OUTPUT_FILE, true));
            SharedVars.threshold = 6000;

            for(int i = 0; i < SharedVars.numThreads; i++) {
                spiders[i].start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // Close both writers in the finally block
            if (SharedVars.linksWriter != null) {
                try {
                    SharedVars.linksWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (SharedVars.compactStringsWriter != null) {
                try {
                    SharedVars.compactStringsWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        for(int i = 0; i < SharedVars.numThreads; i++) {
            try {
                // Wait for thread1 to finish
                spiders[i].join();
                System.out.println("Thread joined");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        long startTime = System.currentTimeMillis();
        outgoingLinks();
        long endTime = System.currentTimeMillis();
        long timeElapsed = endTime - startTime;
        float minutes = timeElapsed / 60000;
        System.out.println("Time elapsed: " + minutes + " minutes");
        // Exiting the program
    }
}
// This class contains all the shared variables
class SharedVars {
    public static int numFiles = 0; // number of files in the seed set
    public static Vector<String> files = new Vector<>(); // vector to hold the urls
    public static Vector<String> CompactStrings = new Vector<>(); // vector to hold the compact strings
    public static int numThreads; // number of threads specified by the user
    public static int threshold; // number of threads specified by the user
    public static final Object linksLock = 0; // links lock for links file, numFiles and files
    public static final Object stringsLock = 0; // strings lock for compactstrings and its file
    public static final Object indexLock = 0; // strings lock for compactstrings and its file
    public static BufferedWriter linksWriter;
    public static BufferedWriter compactStringsWriter;
    public static final String LINKS_OUTPUT_FILE = "links.txt"; // File for storing crawled links
    public static final String COMPACT_STRINGS_OUTPUT_FILE = "compact_strings.txt"; // File for storing compact strings
}

class spiderWebs implements Runnable {
    public static int freeIndex = 0; // the First free index for dynamic allocation of resources
    int myIndex; // the index the current thread is working on
    private static boolean flag = true;
    // 1. private static webDB WEBDB;
    spiderWebs(int i) {
        //1.WEBDB=new webDB();
        myIndex = i; // set the start
        freeIndex = SharedVars.numThreads + 1; // starting free index.
    }
    public void updateIndex() {
        // synchronize so no 2 threads access the freeIndex at the same time
        synchronized(SharedVars.indexLock) { // index lock synchronized
            myIndex = freeIndex; // update the new index to take a new job
            synchronized (SharedVars.linksLock) {
                freeIndex = freeIndex < SharedVars.numFiles - 1 ? freeIndex + 1 : SharedVars.numFiles;// free Index is now incremented
                if(SharedVars.numFiles >= SharedVars.threshold)
                    flag = false;
                System.out.println("Num Files is: " + SharedVars.numFiles); // just validation
            }
            System.out.println("Free index is: " + freeIndex); // just validation
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
    public static boolean isAllowed(String url) {

        try {
            String robotsTxtUrl = url + "/robots.txt";
            if (!isValidURL(robotsTxtUrl)) {
                System.out.println("Invalid URL: " + url);
                return false;
            }
            Connection.Response response = Jsoup.connect(robotsTxtUrl).ignoreContentType(true).execute();
            if (response.statusCode() == 200) {
                String robotsTxtContent = response.body();
                String[] lines = robotsTxtContent.split("\\r?\\n"); // Split by lines

                // Default allow flag
                boolean allow = true;

                // Loop through each line in robots.txt
                for (String line : lines) {
                    // Check if the line contains "User-agent: *"
                    if (line.trim().startsWith("User-agent: *")) {
                        // Check for "Disallow" directives
                        allow = true; // Reset to default allow
                        continue; // Skip to the next line
                    }

                    // Check if the line contains "Disallow" directive
                    if (line.trim().startsWith("Disallow:")) {
                        // Extract the disallowed path
                        String disallowedPath = line.trim().substring("Disallow:".length()).trim();
                        // Check if the URL matches the disallowed path
                        if (url.contains(disallowedPath)) {
                            allow = false; // URL is disallowed
                        }
                    }
                }
                return allow; // Return the final allow flag
            }
        } catch (IOException e) {
            System.out.println("Error: " + url + e.getMessage());
            // assume permission to crawl

        }
        // Default: return true if robots.txt not found or no restrictions specified
        return true;
    }
    public static String compactString(String text) {
        StringBuilder compactStringBuilder = new StringBuilder();
        boolean lastCharWasSpace = true; // Flag to handle consecutive spaces

        for (char c : text.toCharArray()) {
            // Check if the character is a letter or certain special characters
            if (Character.isLetter(c) || c == '.' || c == ',' || c == ';' || c == ':') {
                // Convert to lowercase for uniformity
                char lowercaseChar = Character.toLowerCase(c);
                // If the previous character was a space, append the lowercase character
                // This ensures that the compact string does not include consecutive spaces
                if (lastCharWasSpace) {
                    compactStringBuilder.append(lowercaseChar);
                    lastCharWasSpace = false;
                }
            } else if (Character.isWhitespace(c)) {
                // Set flag to true if the character is a space
                lastCharWasSpace = true;
            }
            // Ignore other characters
        }

        return compactStringBuilder.toString();
    }
    public static void crawl(String url, BufferedWriter linksWriter, BufferedWriter compactStringsWriter) {
        if (!isValidURL(url)) {
            System.out.println("Invalid URL: " + url);
            return;
        }
        if (isAllowed(url)) {
            try {
                Connection con = Jsoup.connect(url);
                Document doc = con.get();
                if (con.response().statusCode() == 200) {
                    System.out.println(Thread.currentThread().getName() + " Crawling: " + url);
                    String compactString = compactString(doc.text());
                    // links lock for synchronization
                    synchronized (SharedVars.linksLock) {
                        //     1.  List<String> outgoing_links= new ArrayList<String>();
                        for (Element link : doc.select("a[href]")) {

                            String nextLink = link.absUrl("href");
                            //  1. outgoing_links.add(nextLink);
                            if (!SharedVars.files.contains(nextLink)) {
                                System.out.println("Found: " + nextLink);
                                //write crawled link to db
                                //   1. Connection con_next = Jsoup.connect(nextLink);
                                //   1. Document doc_next = con_next.get();
                                //   1. WEBDB.write_url(nextLink,doc_next.title(),doc_next.body().text());
                                linksWriter.write(nextLink + "\n"); // Write crawled link to output file
                                SharedVars.files.addElement(nextLink);
                                synchronized (SharedVars.indexLock) {
                                    SharedVars.numFiles += 1;
                                }
                            } else {
                                System.out.println("Already Contained: " + nextLink);
                            }
                        }
                        //1. WEBDB.insert_to_DB(url,outgoing_links);
                    }
                    synchronized (SharedVars.stringsLock) {
                        if (!SharedVars.CompactStrings.contains(compactString)) {
                            SharedVars.CompactStrings.addElement(compactString);
                            compactStringsWriter.write(compactString + "\n"); // Write compact string to output file

                        } else {
                            System.out.println("String Contained: " + compactString);
                            return;
                        }
                    }
                }
            } catch (IOException e) {
                System.out.println("Error: " + url);
            }
        }
        else
        {
            System.out.println("Not allowed: " + url);
        }
    }

    public void run() {

        while(flag) { // TO BE CHANGED: condition to be changed to include the map and queue
            if(myIndex < SharedVars.numFiles) {
                String currUrl = SharedVars.files.get(myIndex); // the url to work at
                // TODO: crawl the url
                try (BufferedWriter linksWriter = new BufferedWriter(new FileWriter(SharedVars.LINKS_OUTPUT_FILE, true));
                     BufferedWriter compactStringsWriter = new BufferedWriter(new FileWriter(SharedVars.COMPACT_STRINGS_OUTPUT_FILE, true))) {
                    crawl(currUrl, linksWriter, compactStringsWriter); // each spider crawls it web
                    updateIndex(); // synchronized function

                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}