package com.purbon.search.fair.lib;

import org.apache.commons.math3.distribution.BinomialDistribution;

public class BionomialFairnessTableLookup implements FairnessTableLookup {

    public float fairness(int trials, float proportion, float significance) {

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
