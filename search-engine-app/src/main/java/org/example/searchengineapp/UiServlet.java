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
       out.println("<style>\n");
        out.println("form {text-align: center;margin-top: 50px;}");
        out.println("input[type=\"text\"] {width: 300px;padding: 10px;border-radius: 5px;border: 1px solid #ccc;font-size: 16px;}\n");
        out.println("button[type=\"submit\"] {padding: 10px 20px;background-color: #4CAF50;color: white;border: none;border-radius: 5px;cursor: pointer;font-size: 16px;}\n");
        out.println("button[type=\"submit\"]:hover {background-color: #45a049;}\n");

        out.println("    body {\n"+
                "        font-family: Arial, sans-serif;\n" +
               "        background-color: #f2f2f2;\n" +
               "        margin: 0;\n" +
               "        padding: 20px;\n" +
               "    }\n" +
               "\n" +
               "    h1 {\n" +
               "        color: #1a0dab;\n" +
               "        text-align: center;\n" +
               "    }\n" +
               "\n" +
               "    h4 {\n" +
               "        color: #1a0dab;\n" +
               "        margin: 0;\n" +
               "        padding: 0;\n" +
               "        font-size: 18px;\n" +
               "        margin-bottom: 5px;\n" +
               "    }\n" +
               "\n" +
               "    p {\n" +
               "        margin: 0;\n" +
               "        padding: 0;\n" +
               "        font-size: 14px;\n" +
               "        color: #4d5156;\n" +
               "    }\n" +
               "\n" +
               "    a {\n" +
               "        color: #1a0dab;\n" +
               "        text-decoration: none;\n" +
               "    }\n" +
               "\n" +
               "    .result {\n" +
               "        margin: 20px 0;\n" +
               "        padding: 10px;\n" +
               "        background-color: #fff;\n" +
               "        border-radius: 8px;\n" +
               "        box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);\n" +
               "    }\n" +
               "\n" +
               "    .result:hover {\n" +
               "        box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);\n" +
               "    }\n" +
               "\n" +
               "    .result a {\n" +
               "        font-size: 14px;\n" +
               "        color: #1a0dab;\n" +
               "    }\n" +
               "\n" +
               "    .result a:hover {\n" +
               "        text-decoration: underline;\n" +
               "    }\n" +
               "\n" +
               "    .highlight {\n" +
               "        background-color: #ffa;\n" +
               "    }\n" +
               "\n" +
               "    .pagination_section {\n" +
               "        display: flex;\n" +
               "        justify-content: center;\n" +
               "        align-items: center;\n" +
               "        margin-top: 20px;\n" +
               "    }\n" +
               "\n" +
               "    .pagination_section a {\n" +
               "        color: #1a0dab;\n" +
               "        padding: 5px 10px;\n" +
               "        margin: 0 5px;\n" +
               "        border: 1px solid #dadce0;\n" +
               "        border-radius: 2px;\n" +
               "        text-decoration: none;\n" +
               "    }\n" +
               "\n" +
               "    .pagination_section a:hover {\n" +
               "        background-color: #f1f1f1;\n" +
               "    }\n" +
               "\n" +
               "    .pagination_section .active {\n" +
               "        background-color: #f1f1f1;\n" +
               "    }\n" +
               "</style>\n");
        out.println("</head>");
        out.println("<body>");
        out.println("<h1>Search Result</h1>");

        out.println("<form action=\"ui-servlet\" method=\"get\">");
        query=query.replaceAll("\"","");
        out.println("Enter your query: <input type=\"text\" name=\"query\" value=\""+query+"\">");
        out.println("<button type=\"submit\">Submit</button>");
        out.println("</form>");
//        out.println("<p>Query: " + query + "</p>");

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