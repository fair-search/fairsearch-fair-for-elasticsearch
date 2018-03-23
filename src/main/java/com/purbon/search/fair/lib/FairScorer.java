package com.purbon.search.fair.lib;

import org.apache.lucene.search.ScoreDoc;

public class FairScorer {

    private int top;

    public FairScorer(int top) {
        this.top = top;
    }

    public int score(ScoreDoc doc) {
       int score = top;
       top = top - 1;
       return score;
    }
}
