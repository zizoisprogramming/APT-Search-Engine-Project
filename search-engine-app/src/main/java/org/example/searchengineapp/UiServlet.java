package org.example.searchengineapp;

import java.io.*;
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
        String result=q.process_query(query);
        // Hello
        PrintWriter out = response.getWriter();
        out.println("<html><body>");
        out.println("<h1>" + query+" "+result+ "</h1>");
        out.println("</body></html>");
    }

    public void destroy() {
    }
}