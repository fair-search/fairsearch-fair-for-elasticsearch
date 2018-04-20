package com.purbon.search.fair.lib;

import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.util.PriorityQueue;

import java.util.List;

public interface FairTopK {

    TopDocs fairTopK(List<ScoreDoc> npQueue, List<ScoreDoc> pQueue, int k, float p, float alpha);

    @Deprecated
    TopDocs fairTopK(PriorityQueue<ScoreDoc> p0, PriorityQueue<ScoreDoc> p1, int k, float p, float alpha);
}
