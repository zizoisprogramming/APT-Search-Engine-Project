package Project.Crawler;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Vector;


class Crawler {
    // This function reads the starting file (Seed set)
    public static void readSeed(String filePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            // Read each line from the file until reaching the end
            while ((line = br.readLine()) != null) {
                // Process the line here (e.g., print it)
                SharedVars.files.addElement(line);
                SharedVars.numFiles += 1;
                
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args) {
        
        SharedVars.numThreads = Integer.parseInt(args[0]); // Set the number of threads
        Thread[] spiders = new Thread[SharedVars.numThreads]; 
        // make new threads
        for(int i = 0; i < SharedVars.numThreads; i++) {
            spiders[i] = new Thread(new spiderWebs(i));
        }
        
        String filePath =  "D:\\CMP Year two\\second semester\\Advanced Programming\\Project\\Crawler\\seed.txt";
        readSeed(filePath);

        // For testing
        for(int i = 0; i < SharedVars.numFiles; i++) {
            System.out.println(SharedVars.files.get(i));
        }
    }
}

// This class contains all the shared variables
class SharedVars {
    public static int numFiles = 0; // number of files in the seed set
    public static Vector<String> files = new Vector<>(); // vector to hold the urls
    public static int numThreads; // number of threads specified by the user
}

class spiderWebs implements Runnable {
    public Object lock; // For synchronization: lock on the freeIndex
    public static int freeIndex = 0; // the First free index for dynamic allocation of resources
    int myIndex; // the index the current thread is working on
    int size; // holds the current num of urls 

    spiderWebs(int i) {
        myIndex = i; // set the start
        freeIndex = SharedVars.numThreads + 1; 
        size = SharedVars.numFiles;
    }

    public void updateFree() {
        // synchronize so no 2 threads access the freeIndex at the same time
        synchronized(lock) {
            myIndex = freeIndex; // update the new index to take a new job
            freeIndex += 1; // free Index is now incremented
        }
    }

    public void run() {
        while(myIndex < size) { // TO BE CHANGED: condition to be changed to include the map and queue
            String currUrl = SharedVars.files.get(myIndex); // the url to work at
            // TODO: crawl the url
        }
    }
}