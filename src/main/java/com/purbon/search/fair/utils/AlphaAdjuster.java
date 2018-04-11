package com.purbon.search.fair.utils;

import org.apache.commons.math3.distribution.BinomialDistribution;

import java.util.ArrayList;

public class AlphaAdjuster {


    private int n;
    private int k;
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

    public AlphaAdjuster(int n, int k, double p, double alpha) {
        if (n < 1) {
            throw new IllegalArgumentException("Parameter n must be at least 1");
        }
        if (p <= 0.0 || p >= 1.0) {
            throw new IllegalArgumentException("Parameter p must be in ]0.0, 1.0[");
        }
        if (alpha <= 0.0 || alpha > 1.0) {
            throw new IllegalArgumentException("Parameter alpha must be in ]0.0, 1.0[");
        }

        this.n = n;
        this.k = k;
        this.p = p;
        this.alpha = alpha;
        this.currentHigh = 0;

        this.mTableGenerator = new MTableGenerator(n, k, p, alpha);
        this.mTable = this.mTableGenerator.getMTable();
        this.auxMTable = this.computeAuxTMTable();
    }

    public DataFrame computeAuxTMTable() {
        DataFrame table = new DataFrame("inv", "block");
        int lastMSeen = 0;
        int lastPosition = 0;
        for (int position = 1; position < mTable.length - 1; position++) {
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
        int maxProtected = auxMTable.getLengthOf("inv");
        int minProtected = 1;
        double successProbability = 0;

        ArrayList<Double> currentTrial = new ArrayList<>();

        double[] successObtainedProb = new double[maxProtected];
        successObtainedProb = fillWithZeros(successObtainedProb);
        successObtainedProb[0] = 1.0;
        ArrayList<ArrayList<Double>> pmfCache = new ArrayList<>();
        while (minProtected < maxProtected) {
            int blockLength = auxMTable.at(minProtected, "block");
            if (blockLength <= pmfCache.size()) {
                currentTrial = pmfCache.get(blockLength);
            } else {
                currentTrial = new ArrayList<>(blockLength + 1);
                BinomialDistribution binomDist = new BinomialDistribution(blockLength, p);
                for (int i = 0; i < blockLength + 1; i++) {
                    currentTrial.add(i, binomDist.probability(i));
                }
                if (blockLength >= pmfCache.size()) {
                    int entriesToAdd = pmfCache.size();
                    for (int j = 0; j <= blockLength - entriesToAdd; j++) {
                        pmfCache.add(new ArrayList<Double>());
                    }
                }
                pmfCache.add(blockLength, currentTrial);
            }
            double[] newSuccessObtainedProb = fillWithZeros(new double[maxProtected]);
            for (int i = 0; i < blockLength + 1; i++) {
                double[] increase = increase(i, successObtainedProb, currentTrial);
                newSuccessObtainedProb = addEntryWise(increase, newSuccessObtainedProb);
            }
            newSuccessObtainedProb[minProtected - 1] = 0;

            successObtainedProb = newSuccessObtainedProb;
            successProbability = sum(successObtainedProb);

            minProtected += 1;
        }

        return successProbability;

    }

    public double[] increase(int i, double[] successObtainedProb, ArrayList<Double> currentTrial) {
        double[] shifted = shiftToRight(successObtainedProb, i);
        for (int j = 0; j < shifted.length; j++) {
            shifted[j] = shifted[j] * currentTrial.get(i);
        }
        return shifted;
    }

    public double[] addEntryWise(double[] arrayOne, double[] arrayTwo) {
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
        for (int i = 0; i < shifted.length; i++) {
            if (pos == 0) {
                shifted[i] = array[i];
            } else if (i + pos > shifted.length - 1) {
                shifted[i] = array[i % pos];
            } else {
                shifted[i] = array[i + (i % pos)];
            }
        }
        return shifted;
    }

    private double[] multiplyAndAddComponentwiseProbMass(int i, int b_j, double p, double[] s, double[] s_new) {
        BinomialDistribution binomDist = new BinomialDistribution(b_j, p);
        double f = binomDist.probability(i);
        s = shiftToRight(s, i);
        for (int j = 0; j < s.length; j++) {
            s_new[j] = s_new[j] + s[j] * f;
        }
        return s_new;
    }

    private double sum(double[] array) {
        double sum = 0;
        for (int i = 0; i < array.length; i++) {
            sum += array[i];
        }
        return sum;
    }

    private double[] copy(double[] s_new) {
        double[] copied = new double[s_new.length];
        for (int i = 0; i < s_new.length; i++) {
            copied[i] = s_new[i];
        }
        return copied;
    }


    public static void main(String[] args) {
        AlphaAdjuster alphaAdjuster = new AlphaAdjuster(40,40,0.4,0.1);
        System.out.println(alphaAdjuster.computeSuccessProbability());

    }

}
