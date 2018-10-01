package edu.upenn.cis455.searchengine;

import static spark.Spark.*;

public class SearchEngine {
    
    public static void main(String[] args) {
        int port = 8000;
		port(port);
		
		get("/search", new SearchHandler());
	}
}
