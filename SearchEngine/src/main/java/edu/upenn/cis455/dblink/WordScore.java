package edu.upenn.cis455.dblink;

import java.lang.Math;

public class WordScore {
    
    private float tf;
    private int docNum;
    
    public WordScore(float tf, int docNum) {
        this.tf = tf;
        this.docNum = docNum;
    }
    
    public float getTf() {
        return tf;
    }
    
    public int getDocNum() {
        return docNum;
    }
    
    private float getIdf(int totalDocNum) {
        return (float)Math.log((float)totalDocNum / docNum);
    }
    
    public float getWeight(int totalDocNum) {
        return tf * getIdf(totalDocNum);
    }
    
}
