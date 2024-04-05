import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Vector;
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
            SharedVars.numFiles = SharedVars.files.size();
            System.out.println("Size equal to: " + SharedVars.numFiles);

        } else {
            System.out.println("Starting Crawling");

            readLinks(filePath);
        }
    }

    private static void readLinks(String filePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            // Read each line from the file until reaching the end
            while ((line = br.readLine()) != null) {
                // Process the line here (e.g., add it to the shared variable)
                SharedVars.files.addElement(line);
                SharedVars.numFiles += 1;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void readCompactStrings(String filePath) {
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

    public static void main(String[] args) {
        String filePath = "./seeds.txt";
        readSeed(filePath, true);
        int index = 0;
        try (BufferedWriter linksWriter = new BufferedWriter(new FileWriter(LINKS_OUTPUT_FILE, true));
             BufferedWriter compactStringsWriter = new BufferedWriter(new FileWriter(COMPACT_STRINGS_OUTPUT_FILE, true))) {
            while (SharedVars.numFiles < 10000000 && index < SharedVars.numFiles) {
                crawl(SharedVars.files.get(index), linksWriter, compactStringsWriter);
                index++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static boolean isAllowed(String url) {
        try {
            String robotsTxtUrl = url + "/robots.txt";
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
        if (isAllowed(url)) {
            try {
                Connection con = Jsoup.connect(url);
                Document doc = con.get();
                if (con.response().statusCode() == 200) {
                    System.out.println("Crawling: " + url);
                    String compactString = compactString(doc.text());
                    if (!SharedVars.CompactStrings.contains(compactString)) {
                        SharedVars.CompactStrings.addElement(compactString);
                        compactStringsWriter.write(compactString + "\n"); // Write compact string to output file

                    }
                    else
                    {
                        System.out.println("String Contained: " + compactString);
                        return;
                    }

                    for (Element link : doc.select("a[href]")) {
                        String nextLink = link.absUrl("href");
                        if (!SharedVars.files.contains(nextLink)) {
                            System.out.println("Found: " + nextLink);
                            linksWriter.write(nextLink + "\n"); // Write crawled link to output file
                            SharedVars.files.addElement(nextLink);
                            SharedVars.numFiles += 1;
                        }
                        else
                        {
                            System.out.println("Already Contained: " + nextLink);
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
}

// This class contains all the shared variables
class SharedVars {
    public static int numFiles = 0; // number of files in the seed set
    public static Vector<String> files = new Vector<>(); // vector to hold the urls
    public static Vector<String> CompactStrings = new Vector<>(); // vector to hold the compact strings
    public static int numThreads; // number of threads specified by the user
}
