<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
  <title>JSP - Web UI</title>
</head>
<body>
<h1><%= "welcome to search engine" %>
</h1>
<form action="ui-servlet" method="get">
  Enter your query: <input type="text" name="query">
  <button type="submit">Submit</button>
</form>
<br/>
</body>
</html>