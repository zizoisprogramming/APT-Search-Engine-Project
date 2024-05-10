package org.example.searchengineapp;

import java.io.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gson.Gson;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

@WebServlet(name = "UI_Servlet", value = "/ui-servlet")
public class UiServlet extends HttpServlet {
    private String message;
    private QueryProcessor q;
    private  queries_db queryDB;

    public void init() {

        q=new QueryProcessor();
        queryDB=new queries_db();
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String partialQuery = request.getParameter("partialQuery");
        if (partialQuery != null && !partialQuery.isEmpty()) {
            // Fetch suggestions based on partial query
            List<String> suggestions = queryDB.get_suggestions(partialQuery);
            // Convert suggestions to JSON
            String json = new Gson().toJson(suggestions);
            // Set response content type and write JSON response
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(json);
        } else {
            String query = request.getParameter("query");
            long startTime = System.currentTimeMillis();
            queryDB.store_query(query);

            int page = 1;
            if (request.getParameter("page") != null) {
                page = Integer.parseInt(request.getParameter("page")); // Get the requested page number
            }
            int resultsPerPage = 10; // Number of results per page


            response.setContentType("text/html");

            List<WebPage> result = q.process_query(query);

//            // Calculate the starting index and ending index for the current page
//            int startIndex = (page - 1) * resultsPerPage;
//            int endIndex = Math.min(startIndex + resultsPerPage, result.size());
            long endTime = System.currentTimeMillis();
            long elapsedTime = endTime - startTime;

            // Hello
            PrintWriter out = response.getWriter();
            // Write HTML response with styling
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Search Result</title>");
            out.println("<style>\n" +
                    "    @import url('https://fonts.googleapis.com/css2?family=Creepster&family=Rubik:wght@300&family=Stick+No+Bills&family=Ubuntu&family=Unlock&display=swap');\n" +
                    "    @import url('https://fonts.googleapis.com/css2?family=Eater&display=swap');\n" +
                    "  </style>");
            out.println("<style>\n");
            out.println("form {text-align: center;margin-top: 50px;}");
            out.println("input[type=\"text\"] {width: 300px;padding: 10px;border-radius: 5px;border: 1px solid #ccc;font-size: 16px;}\n");
            out.println("button[type=\"submit\"] {padding: 10px 20px;background-color: #4CAF50;color: white;border: none;border-radius: 5px;cursor: pointer;font-size: 16px;}\n");
            out.println("button[type=\"submit\"]:hover {background-color: #45a049;}\n");

            out.println("    body {\n" +
                    "        font-family: Arial, sans-serif;\n" +
                    "        background-color: #f2f2f2;\n" +
                    "        margin: 0;\n" +
                    "        padding: 20px;\n" +
                    "    }\n" +
                    "\n" +
                    "    h1 {\n" +
                    "        color: #1a0dab;\n" +
                    "        text-align: center;\n" +
                    "       font-family: \"Eater\", serif;\n"+
                    " margin:20px;" +
                    "    }\n" +
                    "form{" +
                    "padding:5px;" +
                    "margin:5px;}" +
                    "\n" +
                    "    h4 {\n" +
                    "        color: #D44300;\n" +
                    "        margin: 0;\n" +
                    "        padding: 0;\n" +
                    "        font-size: 18px;\n" +
                    "        margin-bottom: 5px;\n" +
                    "    }\n" +
                    "\n" +
                    "    p {\n" +
                    "        margin: 10;\n" +
                    "        padding: 5;\n" +
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
                    "        margin: 20px ;\n" +
                    "        padding: 10px;\n" +
                    "        background-color: #FFFDF9;\n" +
                    "        border-radius: 8px;\n" +
                    "        box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);\n" +
                    " word-wrap: break-word;\n"+
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
                    "  flex-wrap: wrap;\n" +
                    "        justify-content: center;\n" +
                    "        align-items: center;\n" +
                    "        margin-top: 20px;\n" +
                    "    }\n" +
                    "\n" +
                    "    .pagination_section button {\n" +
                    "        color: #1a0dab;\n" +
                    "        padding: 5px 10px;\n" +
                    "        margin: 0 5px;\n" +
                    "        border: 1px solid #dadce0;\n" +
                    "        border-radius: 2px;\n" +
                    "        text-decoration: none;\n" +
                    "    }\n" +
                    "\n" +
                    "    .pagination_section button:hover {\n" +
                    "        background-color: #f1f1f1;\n" +
                    "    }\n" +
                    "\n" +
                    "    .pagination_section .active {\n" +
                    "        background-color: #f1f1f1;\n" +
                    "    }\n" +
                    "</style>\n");
            out.println("<style>\n" +
                    "    body {\n" +
                    "      font-family: Arial, sans-serif;\n" +
                    "      background-image: url('bg.png');\n" +
                    "      margin: 0;\n" +
                    "      padding: 0;\n" +
                    "    }\n" +
                    "    h1 {\n" +
                    "      color: #333;\n" +
                    "      text-align: center;\n" +
                     "font-family: \"Eater\", serif; \n"+
                    "    }\n" +
                    "    .container {\n" +
                    "      display: flex;\n" +
                    "      justify-content: center; /* Center horizontally */\n" +
                    "      align-items: flex-start; /* Align to the top */\n" +
                    "      padding: 20px; /* Add some padding */\n" +
                    "    }\n" +
                    "    form {\n" +
                    "      text-align: center;\n" +
                    "    }\n" +
                    "    input[type=\"text\"] {\n" +
                    "      width: 300px;\n" +
                    "      padding: 10px;\n" +
                    "      border-radius: 5px;\n" +
                    "      border: 1px solid #ccc;\n" +
                    "      font-size: 16px;\n" +
                    "      text-align: center; /* Center the text */\n" +
                    "    }\n" +
                    "    button[type=\"submit\"] {\n" +
                    "      margin-left: 10px; /* Add some space between input and button */\n" +
                    "      padding: 10px 20px;\n" +
                    "      background-color: #e06126;\n" +
                    "      color: white;\n" +
                    "      border: none;\n" +
                    "      border-radius: 5px;\n" +
                    "      cursor: pointer;\n" +
                    "      font-size: 16px;\n" +
                    "    }\n" +
                    "    button[type=\"submit\"]:hover {\n" +
                    "      background-color: #ff864e;\n" +
                    "    }\n" +
                    "    /* Autocomplete styles */\n" +
                    "    .autocomplete {\n" +
                    "      position: relative;\n" +
                    "      display: inline-block;\n" +
                    "    }\n" +
                    "    .autocomplete-content {\n" +
                    "      display: none;\n" +
                    "      position: absolute;\n" +
                    "      background-color: #f9f9f9;\n" +
                    "      min-width: 300px;\n" +
                    "      box-shadow: 0px 8px 16px 0px rgba(0,0,0,0.2);\n" +
                    "      z-index: 1;\n" +
                    "    }\n" +
                    "    .autocomplete-content a {\n" +
                    "      color: black;\n" +
                    "      padding: 12px 16px;\n" +
                    "      text-decoration: none;\n" +
                    "      display: block;\n" +
                    "    }\n" +
                    "    .autocomplete-content a:hover {\n" +
                    "      background-color: #f1f1f1;\n" +
                    "    }\n" +
                    "  </style>");
            out.println("<script src=\"https://ajax.googleapis.com/ajax/libs/jquery/3.5.1/jquery.min.js\"></script>");
            out.println("<script>");
            out.println("$(document).ready(function() {");
            out.println("$('#queryInput').on('input', function() {");
            out.println("var partialQuery = $(this).val();");

            out.println("if (partialQuery.trim().length === 0) {");

            out.println("$('#autocompleteDropdown').hide();");
            out.println("return;");
            out.println("}");
            out.println("$.ajax({");
            out.println("url: 'ui-servlet',");
            out.println("method: 'GET',");
            out.println("data: { partialQuery: partialQuery },");
            out.println("success: function(data) {");
            out.println("var autocompleteContent = $('#autocompleteContent');");
            out.println("autocompleteContent.empty();");
            out.println("data.forEach(function(suggestion) {");
//            out.println("autocompleteContent.append('<a href=\"#\" onclick=\"selectSuggestion(\\'' + suggestion + '\\')\">' + suggestion + '</a>');");
            out.println("var suggestionLink = $('<a>', {\n" +
                    "                href: '#',\n" +
                    "                text: suggestion,\n" +
                    "                click: function() {\n" +
                    "                  selectSuggestion(suggestion);\n" +
                    "                  return false;\n" +
                    "                }\n" +
                    "              });\n" +
                    "              autocompleteContent.append(suggestionLink);");
            out.println("});");
            out.println("if (data.length > 0) {");

            out.println("$('#autocompleteDropdown').show();");
            out.println("} else {");
            out.println("$('#autocompleteDropdown').hide();");
            out.println("}");
            out.println("}");
            out.println("});");
            out.println("});");
            out.println("});");
            out.println("function selectSuggestion(suggestion) {");
            out.println("$('#queryInput').val(suggestion);");
            out.println("$('#autocompleteDropdown').hide();");
            out.println("}");
            out.println("</script>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Search Result</h1>");

            // Output search box
            query=query.replaceAll("\"","\\\"");
            query=query.replaceAll("\'","\\\'");

            System.out.println(query);

            out.println("<div>");
            out.println("<div class=\"autocomplete\">");
            out.println("<form action=\"ui-servlet\" method=\"get\">");

            if(query.contains("\""))
                out.println("<input type=\"text\" id=\"queryInput\" name=\"query\" value=\'"+query+"\' placeholder=\"Enter your query...\" autocomplete=\"off\">");
            else
                out.println("<input type=\"text\" id=\"queryInput\" name=\"query\" value=\""+query+"\" placeholder=\"Enter your query...\" autocomplete=\"off\">");

            out.println("<div id=\"autocompleteDropdown\" class=\"autocomplete-content\">");
            out.println("<div id=\"autocompleteContent\"></div>");
            out.println("</div>");

            out.println("<button type=\"submit\" id=\"goButton\">Go!</button>");
            out.println("</form>");
            out.println("</div>");
            out.println("</div>");

            out.println("<p style=\"padding: 20px; color: #FF4D00;\">Elapsed Time: " + elapsedTime + " milliseconds " + result.size() + " results</p>");
//        out.println("<p>Query: " + query + "</p>");

            for (int i = 0; i < result.size(); i++) {
                WebPage wp = result.get(i);
                String body_html = formatbody(wp.getBody(), query);
                out.println("<div class=\"result\" id=\""+i+"\">");
                out.println("<h4>Title: " + wp.getTitle() + "</h4>");
                out.println("<a href=\"" + wp.getUrl() + "\">Url: " + wp.getUrl() + "</a>");
                out.println(body_html);
                out.println("</div>");
            }
            // Generate pagination links
            int totalPages = (int) Math.ceil((double) result.size() / resultsPerPage);
            out.println("<div class=\"pagination_section\">");
            System.out.println(query);
            for (int i = 1; i <= totalPages; i++) {
                out.println("<button class=\"pages\" id=\"b"+i+"\">Page " + i + "</button>");
            }
            out.println("</div>");
            out.println("\n" +
                    "<script>\n" +
                    "  document.addEventListener(\"DOMContentLoaded\", function() {\n" +
                    "    var btn1 = document.getElementById(\"goButton\");\n" +
                    "\n" +
                    "    btn1.addEventListener(\"click\", function() {\n" +
                    "      console.log(\"heyy\");\n" +
                    "      btn1.hidden=true;\n" +
                    "    });\n" +
                    "  });\n" +
                    "function showElementsInRange(startIndex, endIndex) {\n" +
                            "    var elements = document.getElementsByClassName(\"result\");\n" +
                            "\n" +
                            "    for (var i = 0; i < elements.length; i++) {\n" +
                            "        elements[i].style.display = \"none\";\n" +
                            "    }\n" +
                            "\n" +
                            "    for (var j = startIndex; j < Math.min(endIndex, elements.length); j++) {\n" +
                            "        elements[j].style.display = \"block\";\n" +
                            "    }\n" +
                            "}\n"+
                    "function showButtonsInRange(startIndex, endIndex) {\n" +
                    "    var elements = document.getElementsByClassName(\"pages\");\n" +

                    "\n" +
                    "    for (var i = 0; i < elements.length; i++) {\n" +
                    "        elements[i].style.display = \"none\";\n" +
                    "elements[i].style.border = \"0px solid #2c3e50\"; "+
                    "    }\n" +
                    "\n" +
                    "    for (var j = Math.max(startIndex,0); j < Math.min(endIndex, elements.length); j++) {\n" +
                    "        elements[j].style.display = \"block\";\n" +
                    "    }\n" +
                    "}\n"+
                    "showElementsInRange(0,10);"+
                    "showButtonsInRange(0,5);"+
                    "function addEventListenersToButtons(buttons) {\n" +
                    "    buttons.forEach(function(button, index) {\n" +
                    "        button.addEventListener(\"click\", function() {\n" +
                    "            // Calculate the range based on the button index\n" +
                    "            var startIndex = index * 10;\n" +
                    "            var endIndex = startIndex + 10;\n" +
                    "            // Call the function to show elements within the calculated range\n" +
                    "            showElementsInRange(startIndex, endIndex);\n" +
                    "            showButtonsInRange(index-5, index+5);\n" +
                    "buttons[index].style.border = \"2px solid #2c3e50\"; "+
                    "        });\n" +
                    "    });\n" +
                    "}\n" +
                    "\n" +
                    "// Get all buttons with a specific class\n" +
                    "var buttons = document.querySelectorAll(\".pages\");\n" +
                    "buttons[0].style.border = \"2px solid #2c3e50\"; "+
                    "\n" +
                    "// Add event listeners to the buttons\n" +
                    "addEventListenersToButtons(buttons);"+
                    "</script>");

            out.println("</body>");
            out.println("</html>");
            q.clean_up();
        }
    }
    private String formatbody(String body,String query)
    {
//        String regex = "[\\p{Punct}]";
        String[] words = body.split("\\s+");
        String[] highlightWords_aux=query.replaceAll("\"","").split("\\s+");
        // set for query words
        Set<String> highlightWords = new HashSet<>();
        PorterStemmer obj = new PorterStemmer();

        // Add words to the set
        for (String word : highlightWords_aux) {
            highlightWords.add(obj.stem(word.toLowerCase()));
        }
        StringBuilder htmlOutput = new StringBuilder("<p>Paragraph: ");

        for (String word : words)
        {
             if (highlightWords.contains(obj.stem(word.toLowerCase()))){
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
        return htmlOutput.toString()+"...";
    }
    public void destroy() {
        queryDB.close();
    }

}