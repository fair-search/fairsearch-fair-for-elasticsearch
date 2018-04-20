package com.purbon.search.fair.lib;

import com.purbon.search.fair.lib.fairness.InternalFairnessTableLookup;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.elasticsearch.client.Client;

import java.util.Arrays;
import java.util.List;

public class FairTopKImpl implements FairTopK {

    private FairnessTableLookup fairnessLookup;

    public FairTopKImpl(Client client)
    {
        fairnessLookup = new InternalFairnessTableLookup(client);
    }

    public FairTopKImpl(FairnessTableLookup fairnessLookup) {
        this.fairnessLookup = fairnessLookup;
    }

    public TopDocs fairTopK(List<ScoreDoc> npQueue, List<ScoreDoc> pQueue, int k, float p, float alpha) {

        FairScorer scorer = new FairScorer(k);

        int [] m = fairnessLookup.fairnessAsTable(k, p, alpha);

        int npSize = npQueue.size();
        int pSize = pQueue.size();

        ScoreDoc[] t = new ScoreDoc[npSize+pSize];

        int tp = 0;
        int tn = 0;
        int i = 0;
        int countProtected = 0;
        float maxScore = 0.0f;
        while ( ((tp+tn) < k)) {
            ScoreDoc doc;
            if (tp  >= pSize) { // no more protected candidates available, take non protected
                doc = npQueue.get(tn);
                doc.score = scorer.score(doc);
                t[i] = doc;
                i = i + 1;
                tn = tn + 1;
            } else if (tn >= npSize) { // no more non protected candidates, take protected instead.
                doc = pQueue.get(tp);
                doc.score = scorer.score(doc);
                t[i] = doc;
                i = i + 1;
                tp = tp + 1;
                countProtected = countProtected + 1;
            } else if (countProtected < m[tp+tn]) { // protected candidates
                doc = pQueue.get(tp);
                doc.score = scorer.score(doc);
                t[i] = doc;
                i = i + 1;
                tp = tp + 1;
                countProtected = countProtected + 1;
            } else { // Non protected candidates
                if (pQueue.get(tp).score >= npQueue.get(tn).score) {
                    doc = pQueue.get(tp);
                    doc.score = scorer.score(doc);
                    t[i] = doc;
                    i = i + 1;
                    tp = tp + 1;
                    countProtected = countProtected + 1;
                } else {
                    doc = npQueue.get(tn);
                    doc.score = scorer.score(doc);
                    t[i] = doc;
                    i = i + 1;
                    tn = tn + 1;
                }
            }
            if (doc != null) {
                if (doc.score > maxScore) {
                    maxScore = doc.score;
                }
            }
        }

        while(tp < pQueue.size()) {
            ScoreDoc doc = pQueue.get(tp);
            doc.score = 0;
            t[i] = doc;
            i = i + 1;
            tp = tp + 1;
        }

        while(tn < npQueue.size()) {
            ScoreDoc doc = npQueue.get(tn);
            doc.score = 0;
            t[i] = doc;
            i = i + 1;
            tn = tn + 1;
        }

        TopDocs docs = new TopDocs(t.length, t, k);
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
}
