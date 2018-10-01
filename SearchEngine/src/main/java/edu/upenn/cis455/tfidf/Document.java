package edu.upenn.cis455.tfidf;

import java.io.UnsupportedEncodingException;

public class Document {
    
    private String url;
    private float tfIdf;
    private float pageRank;
    
    public Document(String url, float tfIdf, float pageRank) {
        try {
            this.url = java.net.URLDecoder.decode(url, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            this.url = url;
            e.printStackTrace();
        }
        this.tfIdf = tfIdf;
        setPageRank(pageRank);
    }
    
    private void setPageRank(float pageRank) {
        if (pageRank == 0) {
            pageRank = 37;
        }
        this.pageRank = pageRank;
    }
    
    public void setTfIdf(float tfIdf) {
        this.tfIdf = tfIdf;
    }
    
    public String getUrl() {
        return url;
    }
    
    public float getTfIdf() {
        return tfIdf;
    }
    
    public float getPageRank() {
        return pageRank;
    }
    
    public String getName() {
        String end = url.split("//")[1];
        String host = end.split("/")[0];
        return host;
    }
    
    public float getScore() {
        return tfIdf + pageRank/740;
    }
    
    @Override
    public int hashCode() {
        return url.hashCode();
    }
    
    @Override
    public boolean equals(Object other){
        if (other == null) return false;
        if (other == this) return true;
        if (!(other instanceof Document)) return false;
        Document otherDocument = (Document)other;
        return url.equals(otherDocument.url);
    }
    
    @Override
    public String toString() {
        return url + ": tfidf: " + tfIdf + ", pageRank: " + pageRank + ", score: " + getScore();
    }
    
}
