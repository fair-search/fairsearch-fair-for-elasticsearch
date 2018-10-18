package com.purbon.search.fair.utils;

public class MTableFailProbPair {

    private int[] mTable;
    private int k;
    private double p;
    private double alpha;
    private double failProb;

    public MTableFailProbPair(int k, double p, double alpha, double failProb, int[] mTable){
        this.k = k;
        this.p = p;
        this.alpha = alpha;
        this.failProb = failProb;
        this.mTable = mTable;
    }

    public int getMassOfMTable(){
        int mass =0;
        for(int i = 0; i<mTable.length; i++){
            mass += mTable[i];
        }
        return mass;
    }


    public int[] getmTable() {
        return mTable;
    }

    public int getK() {
        return k;
    }

    public double getP() {
        return p;
    }

    public double getAlpha() {
        return alpha;
    }

    public double getFailProb() {
        return failProb;
    }
}
