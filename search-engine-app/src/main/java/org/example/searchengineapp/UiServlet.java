package org.example.searchengineapp;

import java.io.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

@WebServlet(name = "UI_Servlet", value = "/ui-servlet")
public class UiServlet extends HttpServlet {
    private String message;
    private QueryProcessor q;

    public void init() {

        q=new QueryProcessor();
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String query=request.getParameter("query");
        int page=1;
        if(request.getParameter("page")!=null)
        {
            page = Integer.parseInt(request.getParameter("page")); // Get the requested page number
        }
        int resultsPerPage = 10; // Number of results per page


        response.setContentType("text/html");
        List<WebPage> result=q.process_query(query);

        // Calculate the starting index and ending index for the current page
        int startIndex = (page - 1) * resultsPerPage;
        int endIndex = Math.min(startIndex + resultsPerPage, result.size());

        // Hello
        PrintWriter out = response.getWriter();
        // Write HTML response with styling
        out.println("<!DOCTYPE html>");
        out.println("<html>");
        out.println("<head>");
        out.println("<title>Search Result</title>");
        out.println("<style>");
        out.println("body { font-family: Arial, sans-serif; background-color: #f2f2f2; margin: 0; padding: 0; }");
        out.println("h1 { color: #333; text-align: center; }");
        out.println("h4 { color: #333; margin: 0; padding: 0; }");
        out.println("p {margin: 0; padding: 0; }");
        out.println("a { font-size:11px; }");
        out.println(".result { margin: 10px; padding: 10px; }");
        out.println(".pagination_section {display: flex;justify-content: center;align-items: center;margin: 10px; padding: 10px;}");
        out.println(".pagination_section a { color: black; padding: 10px 18px; text-decoration: none; }");
        out.println(".pagination_section a:hover:not(.active) { background-color: #031F3B; color: white; }");
        out.println("</style>");
        out.println("</head>");
        out.println("<body>");
        out.println("<h1>Search Result</h1>");
        out.println("<p>Query: " + query + "</p>");

        for (int i=startIndex;i<endIndex;i++)
        {
            WebPage wp=result.get(i);
            String body_html=formatbody(wp.getBody(),query);
            out.println("<div class=\"result\">");
            out.println("<h4>Title: " + wp.getTitle() + "</h4>");
            out.println("<a href=\""+wp.getUrl()+"\">Url: " + wp.getUrl() + "</a>");
            out.println(body_html);
            out.println("</div>" );

        }
        // Generate pagination links
        int totalPages = (int) Math.ceil((double) result.size() / resultsPerPage);
        out.println("<div class=\"pagination_section\">");
        for (int i = 1; i <= totalPages; i++)
        {
            out.println("<a href=\"?query=" + query + "&page=" + i + "\">Page " + i + "</a>");
        }
        out.println("</div>" );

        out.println("</body>");
        out.println("</html>");
    }
    private String formatbody(String body,String query)
    {
        String[] words = body.split("\\s+");
        String[] highlightWords_aux=query.split("\\s+");
        // set for query words
        Set<String> highlightWords = new HashSet<>();

        // Add words to the set
        for (String word : highlightWords_aux) {
            highlightWords.add(word.toLowerCase());
        }
        StringBuilder htmlOutput = new StringBuilder("<p>Paragraph: ");

        for (String word : words) {
             if (highlightWords.contains(word.toLowerCase())){
                // Highlight
                htmlOutput.append("<b>").append(word).append("</b>");
            } else {
                htmlOutput.append(word);
            }
            // add space
            htmlOutput.append(" ");
        }

        // Close the paragraph tag
        htmlOutput.append("</p>");
        return htmlOutput.toString();
    }
    public void destroy() {
    }

}