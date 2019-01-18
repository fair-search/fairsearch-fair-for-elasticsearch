package com.purbon.search.fair.utils;


import org.apache.commons.math3.distribution.BinomialDistribution;

public class MTableGenerator {

    private int[] mTable;
    private int n;
    private double p;
    private double alpha;
    private double adjustedAlpha;
    private boolean adjustAlpha;

    /**
     * @param n           Total number of elements
     * @param p           The proportion of protected candidates in the top-k ranking
     * @param alpha       the significance level
     * @param adjustAlpha should the alpha be adjusted
     */
    public MTableGenerator(int n, double p, double alpha, boolean adjustAlpha) {
        if (parametersAreValid(n, p, alpha)) {
            this.n = n;
            this.p = p;
            this.adjustAlpha = adjustAlpha;
            this.alpha = alpha;
            if (adjustAlpha) {
                RecursiveNumericFailprobabilityCalculator adjuster = new RecursiveNumericFailprobabilityCalculator(n, p, alpha);
                MTableFailProbPair failProbPair = adjuster.adjustAlpha();
                this.adjustedAlpha = failProbPair.getAlpha();
                this.mTable = failProbPair.getmTable();
            } else {
                this.adjustedAlpha = alpha;
                this.mTable = computeMTable();
            }
        } else {
            throw new IllegalArgumentException("Invalid Input Parameters for MTable calculation.");
        }
    }

    /**
     * Stores the inverse of an mTable entry and the size of the block with respect to the inverse
     *
     * @return A Dataframe with the columns "inv" and "block" for the values of the inverse mTable and blocksize
     */
    public DataFrame computeAuxTMTable() {
        DataFrame table = new DataFrame("inv", "block");
        int lastMSeen = 0;
        int lastPosition = 0;
        for (int position = 1; position < mTable.length; position++) {
            if (mTable[position] == lastMSeen + 1) {
                lastMSeen += 1;
                table.put(position, position, (position - lastPosition));
                lastPosition = position;
            } else if (mTable[position] != lastMSeen) {
                throw new RuntimeException("Inconsistent mtable");
            }
        }
        table.resolveNullEntries();
        return table;
    }

    private boolean parametersAreValid(int n, double p, double alpha) {
        return nIsValid(n) && pIsValid(p) && alphaIsValid(alpha);
    }

    private boolean alphaIsValid(double alpha) {
        if (alpha < 0 || alpha >=1) {
            throw new IllegalArgumentException("Parameter alpha must be at in [0, 1]");
        } else {
            return true;
        }
    }

    private boolean nIsValid(int n) {
        if (n < 10 || n > 400) {
            throw new IllegalArgumentException("Parameter n must be at in [10, 400]");
        } else {
            return true;
        }
    }

    private boolean pIsValid(double p) {
        if (p >= 1d || p <= 0.05d) {
            throw new IllegalArgumentException("Parameter p must be in [0.05, 0.95]");
        } else {
            return true;
        }
    }

    private int[] computeMTable() {
        int[] table = new int[this.n + 1];
        for (int i = 1; i < this.n + 1; i++) {
            table[i] = m(i);
        }
        return table;
    }

    private Integer m(int k) {

        BinomialDistribution dist = new BinomialDistribution(k, p);
        if (adjustAlpha) {
            return dist.inverseCumulativeProbability(adjustedAlpha);
        } else {
            return dist.inverseCumulativeProbability(alpha);
        }
    }

    public int[] getMTable() {
        if (this.mTable == null) {
            this.mTable = computeMTable();
        }
        return mTable;
    }

    public int getN() {
        return n;
    }

    public double getP() {
        return p;
    }

    public double getOriginalAlpha() {
        return alpha;
    }

    public double getAdjustedAlpha() {
        return adjustedAlpha;
    }

}
