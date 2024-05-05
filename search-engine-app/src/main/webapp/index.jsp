<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
  <title>Spider Web</title>
  <style>
    @import url('https://fonts.googleapis.com/css2?family=Creepster&family=Rubik:wght@300&family=Stick+No+Bills&family=Ubuntu&family=Unlock&display=swap');
    @import url('https://fonts.googleapis.com/css2?family=Eater&display=swap');
  </style>
  <style>
    body {
      font-family: Arial, sans-serif;
      background-color: #FFF7EA;
      background-image: url('bg.png');
      background-size: cover;
      margin: 0;
      padding: 0;
      display: flex;
      justify-content: center;
      align-items: center;
      height: 100vh;
    }
    h1 {
      color: #333;
      text-align: center;
    }
    h3 {
      color: #333;
      text-align: center;
      font-family: "Eater", serif;
    }
    .container {
      text-align: center;
      margin-top: -120px;
    }
    form {
      text-align: center;
    }
    input[type="text"] {
      width: 350px;
      padding: 10px;
      margin-right: 100px;
      border-radius: 5px;
      border: 1px solid #ccc;
      font-size: 16px;
      padding: 15px;
      box-shadow: 3px 5px 6px rgba(0, 0, 0, 0.1);
    }
    button[type="submit"] {
      padding: 10px 20px;
      background-color: #E06126;
      color: white;
      border: none;
      border-radius: 5px;
      cursor: pointer;
      font-size: 16px;
    }
    button[type="submit"]:hover {
      background-color: #FF864E;
    }
    .autocomplete {
      position: relative;
      display: inline-block;
    }
    .autocomplete-content {
      display: none;
      position: absolute;
      background-color: white;
      width: calc(100% - 40px);
      margin-top: 5px;
      box-shadow: 0px 8px 16px 0px rgba(0,0,0,0.2);
      z-index: 1;
    }
    .autocomplete-content a {
      color: black;
      text-align: left;
      padding: 12px 16px;
      text-decoration: none;
      display: block;
    }
    .autocomplete-content a img {
      margin-right: 10px;
    }
    .autocomplete-content a:hover {
      background-color: #f1f1f1;
    }
    .logo img {
      width: 150px;
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
              var suggestionLink = $('<a>', {
                href: '#',
                html: '<img src="spider.png" alt="Icon" width="16" height="16">' + suggestion,
                click: function() {
                  selectSuggestion(suggestion);
                  return false;
                }
              });
              autocompleteContent.append(suggestionLink);
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
      $('#queryInput').val(suggestion);
      $("#autocompleteDropdown").hide();
    }
  </script>
  <link rel="icon" type="image/png" href="spider.png">
</head>
<body>
<div class="container">
  <div>
    <img src="spider_logo.png" class="logo" width="300" height="300">
  </div>
  <div class="autocomplete">
    <form action="ui-servlet" method="get">
      <h3>Search Spider Web or type a phrase</h3>
      <div style="position: relative;">
        <input type="text" id="queryInput" name="query" placeholder="Enter your query..." autocomplete="off">
        <div id="autocompleteDropdown" class="autocomplete-content">
          <div id="autocompleteContent"></div>
        </div>
        <button type="submit" style="position: absolute; right: 0; top: 0; height: 100%;">Go!</button>

      </div>
    </form>
  </div>
</div>
</body>
</html>
