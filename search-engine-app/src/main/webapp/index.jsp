<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
  <title>JSP - Web UI</title>
  <style>
    body {
      font-family: Arial, sans-serif;
      background-color: #f2f2f2;
      margin: 0;
      padding: 0;
    }
    h1 {
      color: #333;
      text-align: center;
    }
    .container {
      display: flex;
      justify-content: center; /* Center horizontally */
      align-items: flex-start; /* Align to the top */
      padding: 20px; /* Add some padding */
    }
    form {
      text-align: center;
    }
    input[type="text"] {
      width: 300px;
      padding: 10px;
      border-radius: 5px;
      border: 1px solid #ccc;
      font-size: 16px;
      text-align: center; /* Center the text */
    }
    button[type="submit"] {
      margin-left: 10px; /* Add some space between input and button */
      padding: 10px 20px;
      background-color: #4CAF50;
      color: white;
      border: none;
      border-radius: 5px;
      cursor: pointer;
      font-size: 16px;
    }
    button[type="submit"]:hover {
      background-color: #45a049;
    }
    /* Autocomplete styles */
    .autocomplete {
      position: relative;
      display: inline-block;
    }
    .autocomplete-content {
      display: none;
      position: absolute;
      background-color: #f9f9f9;
      min-width: 300px;
      box-shadow: 0px 8px 16px 0px rgba(0,0,0,0.2);
      z-index: 1;
    }
    .autocomplete-content a {
      color: black;
      padding: 12px 16px;
      text-decoration: none;
      display: block;
    }
    .autocomplete-content a:hover {
      background-color: #f1f1f1;
    }
  </style>
  <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.5.1/jquery.min.js"></script>
  <script>
    $(document).ready(function(){
      $("#queryInput").on("input", function() {
        var partialQuery = $(this).val();
        if (partialQuery.trim().length === 0) {
          $("#autocompleteDropdown").hide();
          return;
        }
        $.ajax({
          url: "ui-servlet",
          method: "GET",
          data: { partialQuery: partialQuery },
          success: function(data) {
            var autocompleteContent = $("#autocompleteContent");
            autocompleteContent.empty();
            data.forEach(function(suggestion) {
              autocompleteContent.append('<a href="#" onclick="selectSuggestion(\'' + suggestion + '\')">' + suggestion + '</a>');
            });
            if (data.length > 0) {
              $("#autocompleteDropdown").show();
            } else {
              $("#autocompleteDropdown").hide();
            }
          }
        });
      });
    });

    function selectSuggestion(suggestion) {
      $("#queryInput").val(suggestion);
      $("#autocompleteDropdown").hide();
    }
  </script>
</head>
<body>
<div class="container">
  <div>
    <h1>Welcome to the Search Engine</h1>
    <div class="autocomplete">
      <form action="ui-servlet" method="get">
        <!-- Query input field -->
        <input type="text" id="queryInput" name="query" placeholder="Enter your query..." autocomplete="off">
        <!-- Autocomplete dropdown -->
        <div id="autocompleteDropdown" class="autocomplete-content">
          <div id="autocompleteContent"></div>
        </div>
        <!-- Submit button -->
        <button type="submit">Submit</button>
      </form>
    </div>
  </div>
</div>
</body>
</html>
