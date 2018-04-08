package com.purbon.search.fair.lib;

import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.util.PriorityQueue;

public interface FairTopK {

    TopDocs fairTopK(PriorityQueue<ScoreDoc> p0, PriorityQueue<ScoreDoc> p1, int k, float p, float alpha, int n);
}
