package edu.upenn.cis455.searchengine;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import edu.upenn.cis455.dblink.DBReader;
import edu.upenn.cis455.tfidf.Document;
import edu.upenn.cis455.tfidf.TfIdf;
import spark.Request;
import spark.Route;
import spark.Response;
import spark.HaltException;

public class SearchHandler implements Route {
    
    private Map<String, Double> wordScores = new HashMap<>();
    private Map<String, Integer> words;
    private Integer totalDocNum;
    private boolean debug = false;
    
    @Override
    public String handle(Request req, Response resp) throws HaltException {
        resp.type("text/html");
            
        String response = "<html><head><title>Search Engine</title>" +
        		"<style>" +
        		"#search-form {" +
        		"background-color: #f9f9f9;" +
        		"width: 100%" +
        		"margin: 0" +
        		"}" +
        		"body{" +
                "background: white;" +
                "margin: 0;" +
                "}" +
        		"</style></head>" +
        		"<body>" + 
        		"<section id=\"search-form\">" +
        		"<h1>Search</h1>" +
                "<form action=\"/search\">" +
                "<input type=\"text\" name=\"query\">" +
                "<input type=\"submit\" value=\"Search\"></br><BR>" +
                "</section>" +
                "<section id=\"results\">" +
                getSpellCheck(req) +
                getResultTable(req) +
                "</section>" +
                "</form></body></html>";
        
        return (response);
    }
    
    private float getScore(String word) {
        if (words == null) {
            words = DBReader.getWords();
        }
        if (totalDocNum == null) {
            totalDocNum = DBReader.getDocNum();
        }
        
        Integer docNum = words.get(word);
        if (docNum == null) {
            return 0;
        }
        return (float)docNum / totalDocNum;
    }
    
    private String getSpellCheck(Request req) {
        String query = req.queryParams("query");
        if (query == null) {
            return "";
        }
        query = query.replaceAll("[^a-zA-Z]"," ");
        String[] words = query.split(" ");
        
        boolean corrected = false;
        for (int j = 0; j < words.length; j++) {
            String word = words[j];
            float score = getScore(word);
            if (score < 0.2) {
                int wordLength = word.length();
                float maxScore = score;
                String correctedWord = word;
                for (int i = 0; i < wordLength; i++) {
                    for (char c = 'a'; c <= 'z'; c++) {
                        String tempWord = word.substring(0, i) + c + word.substring(i + 1, wordLength);
                        float tempScore = getScore(tempWord);
                        if (tempScore > maxScore) {
                            maxScore = tempScore;
                            correctedWord = tempWord;
                        }
                    }  
                }
                
                if (maxScore >= ((0.2f >= score*4) ? 0.2f : score*4)) {
                    words[j] = correctedWord;
                    corrected = true;
                }
            }
        }
        
        if (corrected) {
            String correctedString = "";
            for (String word: words) {
                correctedString += word + " ";
            }
            return "Did you mean <a href=\"/search?query=" + correctedString + "\">" + correctedString + "</a>?</br><BR>";
        }
        return "";
    }
    
    private String getResultTable(Request req) {
        String query = req.queryParams("query");
        if (query == null) {
            return "";
        }
        
        long t1 = System.currentTimeMillis();
        TfIdf tfIdf = new TfIdf(query);
        List<Document> documents = tfIdf.getDocuments();
        documents.sort((d1, d2) -> new Float(d2.getScore()).compareTo(d1.getScore()));
        if (documents.isEmpty()) {
            return "No result found";
        }
        
        long t2 = System.currentTimeMillis();
        double time = (double)(t2 - t1) / 1000 + 1;
        
        StringBuilder resultTable = new StringBuilder();
        resultTable.append(documents.size() + " results found in " + time + " seconds");
        resultTable.append("<table style=\"width:100%\">");
            
        for (Document document : documents) {
            resultTable.append("<tr>");
            resultTable.append("<ul>");
            resultTable.append("<a href=\""+ document.getUrl() + "\">" + document.getName() + "</a></br>");
            resultTable.append("<font color=\"green\">" + document.getUrl() + "</font></br>");
            if (debug) {
                resultTable.append("coSim = " + document.getTfIdf() + ", pageRank = " + document.getPageRank() + ", score = " + document.getScore());
            }
            resultTable.append("</ul>"); 
            resultTable.append("</tr>");
        }
            
        resultTable.append("</table>");
        
        return resultTable.toString();
    }
    
}
