package edu.upenn.cis455.dblink;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.upenn.cis455.tfidf.Document;

public class DBReader {
    
    public static Connection getConnection() {
		Connection c = null;
		try {
			Class.forName("org.postgresql.Driver");
			c = DriverManager.getConnection(
					"jdbc:postgresql://cis455medium.ctmwv2fvbnxz.us-east-1.rds.amazonaws.com:5432/crawler", "cis455",
					"prsajara");
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
		}
		return c;
	}
	
	public static Map<Document, Map<String, WordScore>> getVectors(String queryWord) {
	    Connection c = getConnection();
	    Map<Document, Map<String, WordScore>> vectors = new HashMap<>();
	    
		try {
		    PreparedStatement pstmt;
		    pstmt = c.prepareStatement("SELECT w.word as word, w.num_doc as num_doc, ii.tf as tf, d.url as url, d.page_rank as page_rank FROM inv_index ii, words w, documents d WHERE w.id = ii.word_id AND ii.doc_id = d.id AND ii.doc_id IN" +
		                              "(SELECT ii.doc_id as doc_id FROM inv_index ii, words w WHERE w.word = ? AND w.id = ii.word_id LIMIT 1000)");
            pstmt.setString(1, queryWord);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				String url = rs.getString("url");
				String word = rs.getString("word");
				float tf = rs.getFloat("tf");
				float pageRank = rs.getFloat("page_rank");
				int numDoc = rs.getInt("num_doc");
				Document document = new Document(url, 0.0f, pageRank);
				
				if (!vectors.containsKey(document)) {
				    vectors.put(document, new HashMap<>());
				}
				vectors.get(document).put(word, new WordScore(tf, numDoc));
			}
			rs.close();
			pstmt.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
		}
		return vectors;
	}
	
	public static int getDocNum() {
	    Connection c = getConnection();
	    int count = 0;
	    
	    try {
		    PreparedStatement pstmt;
		    pstmt = c.prepareStatement("SELECT count(*) AS count FROM documents");
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				count = rs.getInt("count");
			}
			rs.close();
			pstmt.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
		}
		
		return count;
	}
	
	public static Map<String, Integer> getWords() {
	    Connection c = getConnection();
	    Map<String, Integer> words = new HashMap<>();
	    
	    try {
		    PreparedStatement pstmt;
		    pstmt = c.prepareStatement("SELECT word, num_doc FROM words");
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				String word = rs.getString("word");
				int docNum = rs.getInt("num_doc");
				words.put(word, docNum);
			}
			rs.close();
			pstmt.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
		}
		
		return words;
	}
    
}
