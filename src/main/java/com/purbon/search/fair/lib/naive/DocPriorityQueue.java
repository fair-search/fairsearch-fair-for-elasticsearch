package com.purbon.search.fair.lib.naive;

import org.apache.lucene.util.PriorityQueue;

public class DocPriorityQueue extends PriorityQueue<Document> {

    public DocPriorityQueue(int k) {
        super(k);
    }

    @Override
    protected boolean lessThan(Document a, Document b) {
        return a.score() < b.score();
    }
}
