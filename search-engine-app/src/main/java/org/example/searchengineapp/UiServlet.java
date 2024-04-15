package org.example.searchengineapp;

import java.io.*;
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
        response.setContentType("text/html");
        List<WebPage> result=q.process_query(query);
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
        out.println("</style>");
        out.println("</head>");
        out.println("<body>");
        out.println("<h1>Search Result</h1>");
        out.println("<p>Query: " + query + "</p>");
        for (WebPage wp:result)
        {
            out.println("<div class=\"result\">");
            out.println("<h4>Title: " + wp.getTitle() + "</h4>");
            out.println("<a href=\""+wp.getUrl()+"\">Url: " + wp.getUrl() + "</a>");
            out.println("<p>Paragraph: " + wp.getBody() + "</p>");
            out.println("</div>" );

        }
        out.println("</body>");
        out.println("</html>");
    }

    public void destroy() {
    }
}