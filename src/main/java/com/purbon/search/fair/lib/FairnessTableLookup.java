package com.purbon.search.fair.lib;

public interface FairnessTableLookup {

    /**
     * Single element fairness calculation
     * @param trials k value
     * @param proportion p value
     * @param significance alpha value
     * @return int
     */
    int fairness(int trials, float proportion, float significance);

    /**
     * Return table calculation interface
     * @param k k value
     * @param p p value
     * @param a alpha value
     * @param n n value
     * @return int[]
     */
    int[] fairnessAsTable(int k, float p, float a, int n);

}
