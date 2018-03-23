package com.purbon.search.fair.utils;

import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.util.PriorityQueue;

public class IntPriorityQueue extends PriorityQueue<Integer> {

    public IntPriorityQueue(int k) {
        super(k);
    }

    @Override
    protected boolean lessThan(Integer a, Integer b) {
        return a < b;
    }
}
