package edu.upenn.cis455.tfidf;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.List;
import java.util.Set;
import java.lang.Math;

import edu.upenn.cis455.dblink.DBReader;
import edu.upenn.cis455.dblink.WordScore;

public class TfIdf {
    
    private String query;
    private String[] queryWords;
    private Map<String, Integer> queryWordsDocNum;
    private int totalDocNum;
    
    public TfIdf(String query) {
        this.query = query;
        this.queryWords = query.replaceAll("[^a-zA-Z]"," ").split(" ");
        long t1 = System.currentTimeMillis();
        this.totalDocNum = DBReader.getDocNum();
        long t2 = System.currentTimeMillis();
        double time = (double)(t2 - t1) / 1000;
        System.out.println("Get number of doc : " + time + " seconds");
        this.queryWordsDocNum = new HashMap<>();
    }
    
    /**
     * Returns the list of all the documents the query words appear in with the tf-idf score
     */
    public List<Document> getDocuments() {
        List<Document> documents = new LinkedList<>();
        long t1 = System.currentTimeMillis();
        Map<Document, Map<String, WordScore>> docVectors = getDocVectors();
        long t2 = System.currentTimeMillis();
        double time = (double)(t2 - t1) / 1000;
        System.out.println("Get all vectors : " + time + " seconds");
        
        Map<String, WordScore> queryVector = getQueryVector();
        
        t1 = System.currentTimeMillis();
        for (Map.Entry<Document, Map<String, WordScore>> entry : docVectors.entrySet())
        {
            Document document = entry.getKey();
            Map<String, WordScore> docVector = entry.getValue();
            float score = computeCosSim(docVector, queryVector);
            document.setTfIdf(score);
            documents.add(document);
        }
        
        t2 = System.currentTimeMillis();
        time = (double)(t2 - t1) / 1000;
        System.out.println("Compute scores : " + time + " seconds");
        
        return documents;
    }
    
    /**
     * Creates the vectors of all the documents the query words appear in
     * Also store the number of doc each word of the query appears in
     */
    private Map<Document, Map<String, WordScore>> getDocVectors() {
        Map<Document, Map<String, WordScore>> vectors = new HashMap<>();
        
        for (String word : queryWords) {
            if (!word.equals("")) {
                long t1 = System.currentTimeMillis();
                Map<Document, Map<String, WordScore>> partVectors = DBReader.getVectors(word);
                long t2 = System.currentTimeMillis();
                double time = (double)(t2 - t1) / 1000;
                System.out.println("Query for " + word + " : " + time + " seconds");
                
                Document document = null;
                for (Map.Entry<Document, Map<String, WordScore>> entry : partVectors.entrySet())
                {
                    document = entry.getKey();
                    vectors.put(document, entry.getValue());
                }
                
                if (document != null) {
                    queryWordsDocNum.put(word, partVectors.get(document).get(word).getDocNum());
                } else {
                    query = query.replace(word, "");
                }
            }

        }
        queryWords = query.split(" ");
        
        return vectors;
    } 
    
    /**
     * Returns the weight of a word in a vector
     * Defaults to 0, if the key is not stored
     */
    private float getWeight(Map<String, WordScore> data, String key){
        if (data.get(key) == null){
            return 0;
        }
        else{
            return data.get(key).getWeight(totalDocNum);
        }
    }
    
    /**
     * Returns the value of a map, defaults to 0, if the key is not stored
     */
    private float getVal(Map<String, Float> data, String key){
        if (data.get(key) == null){
            return 0;
        }
        else{
            return data.get(key);
        }
    }
    
    /**
     * Computes the dot product between two vectors
     */
    private float dotProduct(Map<String, WordScore> vec1, Map<String, WordScore> vec2) {
        float res = 0;
        for(String word: vec1.keySet()){
            res += getWeight(vec1, word) * getWeight(vec2, word);
        }
        return res;
    }
    
    /**
     * Computes Cosine Similarity between a query vector and a doc vector
     */
    private float computeCosSim(Map<String, WordScore> doc, Map<String, WordScore> query){
        return dotProduct(query, doc) / (float)(Math.sqrt(dotProduct(doc, doc)) * Math.sqrt(dotProduct(query, query)));
    }

    /**
     * converts a query string into a vector
     */
    private Map<String, WordScore> getQueryVector(){
        Map<String, Float> query_map = new HashMap<>();
        
        float max = 0;
        float temp;
        // computes TF for every term, and finds max freq
        for(String word: queryWords){
            if (!word.equals("")){
                temp = getVal(query_map, word) + 1;
                if (temp > max) max = temp;
                query_map.put(word, getVal(query_map, word) + 1);
            }
        }


        max = max > 1? max:1;

        Map<String, WordScore> queryVector = new HashMap<>();
        // computes normalized tf-idf
        for (String word: query_map.keySet()){
            queryVector.put(word, new WordScore(0.5f + 0.5f*getVal(query_map, word)/max, queryWordsDocNum.get(word)));
        }

        return queryVector;
    }
    
    
}
