package com.purbon.search.fair.utils;

import org.apache.commons.math3.distribution.BinomialDistribution;
import java.util.ArrayList;

public class AlphaAdjuster {


    private int n;
    private double p;
    private double alpha;
    private int[] mTable;
    private DataFrame auxMTable;
    private int currentHigh;
    private MTableGenerator mTableGenerator;

    /*
        Algorithm 1 of the k fair ranking Paper
        @param k the size of the ranking to produce
        @param p the expected proportion of protected elements
        @param alpha the significance for each individual test
        @return the probability of rejecting a fair ranking
     */

    public AlphaAdjuster(int n, double p, double alpha) {
        if (n < 1) {
            throw new IllegalArgumentException("Parameter n must be at least 1");
        }
        if (p <= 0.0 || p >= 1.0) {
            throw new IllegalArgumentException("Parameter p must be in ]0.0, 1.0[");
        }
        if (alpha <= 0.0 || alpha >= 1.0) {
            throw new IllegalArgumentException("Parameter alpha must be in ]0.0, 1.0[");
        }

        this.n = n;
        this.p = p;
        this.alpha = alpha;
        this.currentHigh = 0;

        this.mTableGenerator = new MTableGenerator(n, p, alpha);
        this.mTable = this.mTableGenerator.getMTable();
        this.auxMTable = this.computeAuxTMTable();
    }

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

    public double computeSuccessProbability() {
        int maxProtected = auxMTable.getLengthOf("inv") - 1;
        int minProtected = 1;
        double successProbability = 0;

        ArrayList<Double> currentTrial;

        double[] successObtainedProb = new double[maxProtected];
        successObtainedProb = fillWithZeros(successObtainedProb);
        successObtainedProb[0] = 1.0;
        ArrayList<ArrayList<Double>> pmfCache = new ArrayList<>();

        while (minProtected < maxProtected) {
            int blockLength = auxMTable.at(minProtected, "block");
            if (blockLength <= pmfCache.size() && pmfCache.get(blockLength) != null) {
                currentTrial = pmfCache.get(blockLength);
            } else {
                currentTrial = new ArrayList<>();
                for (int j = 0; j <= blockLength; j++) {
                    currentTrial.add(null);
                }
                BinomialDistribution binomDist = new BinomialDistribution(blockLength, p);
                for (int i = 0; i <= blockLength; i++) {
                    currentTrial.set(i, binomDist.probability(i));
                }

                pmfCache = adjustPmfCache(pmfCache, blockLength);
                pmfCache.set(blockLength, currentTrial);
            }

            double[] newSuccessObtainedProb = fillWithZeros(new double[maxProtected]);
            for (int i = 0; i <= blockLength; i++) {
                double[] increase = increase(i, successObtainedProb, currentTrial);
                newSuccessObtainedProb = addEntryWise(increase, newSuccessObtainedProb);
            }
            newSuccessObtainedProb[minProtected - 1] = 0;

            successObtainedProb = newSuccessObtainedProb;
            successProbability = sum(successObtainedProb);
            minProtected += 1;
        }

        return 1-successProbability;

    }

    private ArrayList<ArrayList<Double>> adjustPmfCache(ArrayList<ArrayList<Double>> pmfCache, int blocklength) {
        if (pmfCache.size() < blocklength) {
            for (int i = pmfCache.size(); i <= blocklength; i++) {
                pmfCache.add(null);
            }
        }
        return pmfCache;
    }

    private double[] increase(int i, double[] successObtainedProb, ArrayList<Double> currentTrial) {
        double[] shifted = shiftToRight(successObtainedProb, i);
        for (int j = 0; j < shifted.length; j++) {
            shifted[j] = shifted[j] * currentTrial.get(i);
        }
        return shifted;
    }


    private double[] addEntryWise(double[] arrayOne, double[] arrayTwo) {
        double[] sum = new double[arrayOne.length];
        for (int i = 0; i < arrayOne.length; i++) {
            sum[i] = arrayOne[i] + arrayTwo[i];
        }
        return sum;
    }

    private double[] fillWithZeros(double[] array) {
        for (int i = 0; i < array.length; i++) {
            array[i] = 0;
        }

        return array;
    }

    private double[] shiftToRight(double[] array, int pos) {
        double[] shifted = new double[array.length];
        pos = pos % array.length;
        for (int i = 0; i < shifted.length; i++) {
            if (pos == 0) {
                shifted[i] = array[i];
            } else if (i + pos > shifted.length - 1) {
                shifted[i % pos] = array[i];
            } else {
                shifted[i + pos] = array[i];
            }
        }
        return shifted;
    }

    private double sum(double[] array) {
        double sum = 0;
        for (double anArray : array) {
            sum += anArray;
        }
        return sum;
    }

}
