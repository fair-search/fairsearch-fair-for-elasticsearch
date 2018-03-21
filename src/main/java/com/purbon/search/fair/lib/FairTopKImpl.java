package com.purbon.search.fair.lib;

import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.util.PriorityQueue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FairTopKImpl implements FairTopK {

    private FairnessTableLookup fairnessLookup = new BionomialFairnessTableLookup();

    public FairTopKImpl() {

    }
    public TopDocs fairTopK(PriorityQueue<ScoreDoc> p0, PriorityQueue<ScoreDoc> p1, int k, float p, float alpha) {

        float [] m = new float[k];

        for(int i=0; i < k; i++) {
            m[i] = fairnessLookup.fairness(i, p, alpha);
        }

        List<ScoreDoc> t = new ArrayList<ScoreDoc>();
        int tp = 0;
        int tn = 0;
        float maxScore = 0.0f;
        while ( ((tp+tn) < k)) {
            ScoreDoc scoreDoc;
            if (tp  > p1.size() + 1) {
                scoreDoc = p0.pop();
                scoreDoc.score = 1;
                t.add(scoreDoc);
                tn = tn + 1;
            } else if (tn > p0.size() + 1) {
                scoreDoc = p1.pop();
                scoreDoc.score = 1;
                t.add(scoreDoc);
                tp = tp + 1;
            } else if (tp < m[tp+tn]) { // protected candidates
                scoreDoc = p1.pop();
                scoreDoc.score = 1;
                t.add(scoreDoc);
                tp = tp + 1;
            } else { // Non protected candidates
                assert p1.size() > 0 && p0.size() > 0;
                if (p1.top().score >= p0.top().score) {
                    scoreDoc = p1.pop();
                    scoreDoc.score = 1;
                    t.add(scoreDoc);
                    tp = tp + 1;
                } else {
                    scoreDoc = p0.pop();
                    scoreDoc.score = 1;
                    t.add(scoreDoc);
                    tn = tn + 1;
                }
            }
            if (scoreDoc != null) {
                if (scoreDoc.score > maxScore) {
                    maxScore = scoreDoc.score;
                }
            }
        }

        accumulatePendingDocs(p1, t);
        accumulatePendingDocs(p0, t);

        TopDocs docs = new TopDocs(t.size(), t.toArray(new ScoreDoc[t.size()]), maxScore);
        Arrays.sort(docs.scoreDocs, (a, b) -> {
            if (a.score > b.score) {
                return -1;
            }
            if (a.score < b.score) {
                return 1;
            }
            // Safe because doc ids >= 0
            return a.doc - b.doc;
        });

        return docs;
    }

    private void accumulatePendingDocs(PriorityQueue<ScoreDoc> p, List<ScoreDoc> t) {
        while(p.size() > 0) {
            ScoreDoc doc = p.pop();
            doc.score = 0;
            t.add(doc);
        }
    }
}
