package com.purbon.search.fair.utils;


public class MTableGenerator {

    private int[] mTable;
    private int n;
    private int k;
    private double p;
    private double alpha;

    /**
     * @param n     Total number of elements
     * @param k     The size of the ranking
     * @param p     The proportion of protected candidates in the top-k ranking
     * @param alpha the significance level
     */
    public MTableGenerator(int n, int k, double p, double alpha) {
        this.n = n;
        this.k = k;
        this.p = p;
        this.alpha = alpha;
    }

    private int[] computeMTable() {
        int[] table = new int[this.n + 1];
        table[0] = 0;
        for (int i = 1; i < this.n + 1; i++) {
            table[i] = m(i);
        }
        return table;
    }

    private Integer m(int k) {
        if (k < 1)
            throw new IllegalArgumentException("Parameter k must be at least 1");
        else if (k > n) {
            throw new IllegalArgumentException("Parameter k must be at most n");
        }
        //care
        //TODO Find new package for Binomial Calculations esp. quantile function
        BinomialDistribution dist = new BinomialDistribution(k, p);
        return (int) dist.quantile(alpha);
    }

    public int[] getMTable() {
        if(this.mTable==null){
            this.mTable = computeMTable();
        }
        return mTable;
    }

    public int getK() {
        return k;
    }

    public int getN(){
        return n;
    }

    public double getP(){
        return p;
    }

    public double getAlpha() {
        return alpha;
    }
}
