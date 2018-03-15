package com.purbon.search.fair.lib;

import org.apache.commons.math3.distribution.BinomialDistribution;
import org.apache.lucene.util.PriorityQueue;

import java.util.List;

/**
 * 
 */
public class FairTopK {

    /**
     * FAIR top K based on https://arxiv.org/pdf/1706.06368.pdf
     * @param docs Docs
     * @param k Size
     * @param proportion Proportion (p param)
     * @param significance Significance (alpha)
     */
    public static Document[] topk(List<Document> docs, int k, float proportion, float significance) {

        PriorityQueue<Document> p0 = new DocPriorityQueue(k);
        PriorityQueue<Document> p1 = new DocPriorityQueue(k);

        for(Document doc : docs) {
            if (doc.isProtected()) {
                p1.add(doc);
            } else {
                p0.add(doc);
            }
        }

        float [] m = new float[k];

        for(int i=0; i < k; i++) {
            m[i] = fairness(i, proportion, significance);
        }

        Document [] t = new Document[k];
        int tp = 0;
        int tn = 0;

        while (tp+tn < k) {
            if (tp < m[tp+tn+1]) {
                // protected candidates
                tp = tp + 1;
                t[tp + tn] = p1.pop();
            } else {
                // Non protected candidates
                tn = tn + 1;
                t[tp + tn] = p0.pop();
            }
        }
        return t;
    }

    // m[i] ← F-1(αc ;i, p)
    private static float fairness(int trials, float proportion, float significance) {

        // trials == k , p == p
        BinomialDistribution d = new BinomialDistribution(trials, proportion);
        /*
           NOTE:
             if alpha is 0.1,  the output should be the first value x where
               the cumulative probability is bigger than alpha (significance)
          */
        int x = 0;
        do {
            x = x + 1;
        } while (d.cumulativeProbability(x) > significance);

        return x;
    }
}
