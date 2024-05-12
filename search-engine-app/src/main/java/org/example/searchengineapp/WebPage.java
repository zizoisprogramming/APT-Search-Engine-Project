package org.example.searchengineapp;

public class WebPage  {
    //,url,id,any other data
    private String url;
    private String body;
    private String title;
    private Double Score;
    public WebPage(String u,String b,String t,Double s)
    {
        url=u;
        body=b;
        title=t;
        Score=s;
    }
    //getters
    public String getUrl(){return url;}
    public String getBody(){return body;}
    public  String getTitle(){return title;}
    public Double getScore(){return Score;}

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

//    @Override
//    //public int compareTo(WebPage o)
//    {
//        return (int) (o.Score*100000-this.Score*100000);
//    }
}
