package org.example.searchengineapp;

public class WebPage {
    //,url,id,any other data
    private String url;
    private String body;
    private String title;
    //getters
    public String getUrl(){return url;}
    public String getBody(){return body;}
    public  String getTitle(){return title;}

    public void setBody(String body) {
        this.body = body;
    }
 //setters
    public void setTitle(String title) {
        this.title = title;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
