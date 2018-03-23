package com.purbon.search.fair.utils;

import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.util.PriorityQueue;

public class DocumentPriorityQueue extends PriorityQueue<ScoreDoc> {

    public DocumentPriorityQueue(int k) {
        super(k);
    }

    @Override
    protected boolean lessThan(ScoreDoc a, ScoreDoc b) {
        return a.score < b.score;
    }
}
