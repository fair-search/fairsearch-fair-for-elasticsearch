package com.purbon.search.fair.utils;

import org.apache.commons.math3.distribution.BinomialDistribution;

import java.util.ArrayList;
import java.util.HashMap;

public class AlphaAdjuster {


    private int n;
    private double p;
    private double alpha;
    private int[] mTable;
    private DataFrame auxMTable;
    private MTableGenerator mTableGenerator;

    /*
        Algorithm 1 of the k fair ranking Paper
        @param k the size of the ranking to produce
        @param p the expected proportion of protected elements
        @param alpha the significance for each individual test
        @return the probability of rejecting a fair ranking
     */

    public AlphaAdjuster(int n, double p, double alpha) {
        if (n < 10 || n>400) {
            throw new IllegalArgumentException("Parameter n must be in [10, 400]");
        }
        if (p < 0.05 || p > 0.95) {
            throw new IllegalArgumentException("Parameter p must be in [0.05, 0.95]");
        }
        if (alpha <= 0d || alpha >= 1.0) {
            throw new IllegalArgumentException("Parameter alpha must be in ]0.0, 1.0[");
        }

        this.n = n;
        this.p = p;
        this.alpha = alpha;

        this.mTableGenerator = new MTableGenerator(n, p, alpha, false);
        this.mTable = this.mTableGenerator.getMTable();
        this.auxMTable = this.mTableGenerator.computeAuxTMTable();

    }



    /**
     * Computes the probability of rejecting a fair ranking with the given parameters n, p and alpha
     *
     * @return The probability of rejecting a fair ranking
     */
    /**
     * Computes the probability of rejecting a fair ranking with the given parameters n, p and alpha
     *
     * @return The probability of rejecting a fair ranking
     */
    public double computeFailureProbability() {
        if (mTable[mTable.length - 1] == 0) {
            return 0;
        }
        DataFrame auxMTable = this.mTableGenerator.computeAuxTMTable();
        int maxProtected = auxMTable.getLengthOf("inv") - 1;
        int minProtected = 1;
        double successProbability = 0;
        ArrayList<Double> currentTrial;
        double[] successObtainedProb = new double[maxProtected];
        successObtainedProb[0] = 1.0;
        HashMap<Integer, ArrayList<Double>> pmfCache = new HashMap<>();

        while (minProtected <= maxProtected) {
            //get the current blockLength from auxMTable

            int blockLength = auxMTable.at(minProtected, "block");
            if (pmfCache.get(blockLength) != null) {
                currentTrial = pmfCache.get(blockLength);
            } else {
                currentTrial = new ArrayList<>();
                BinomialDistribution binomDist = new BinomialDistribution(blockLength, p);
                for (int i = 0; i <= blockLength; i++) {
                    currentTrial.add(binomDist.probability(i));
                }
                pmfCache.put(blockLength, currentTrial);
            }
            //initialize with zeroes
            double[] newSuccessObtainedProb = new double[maxProtected];
            for (int i = 0; i <= blockLength; i++) {
                //shifts all values to the right for i positions (like python.roll)
                //multiplies the current value with the currentTrial of the right position
                double[] increase = increase(i, successObtainedProb, currentTrial);
                //store the result
                newSuccessObtainedProb = addEntryWise(increase, newSuccessObtainedProb);
            }

            newSuccessObtainedProb[minProtected - 1] = 0;


            successObtainedProb = newSuccessObtainedProb;
            successProbability = sum(successObtainedProb);

            minProtected += 1;
        }

        return 1 - successProbability;

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


    /**
     * Shifts all entries of an array to the right for pos positions
     * Example: shiftToRight('1,2,3,4',2) ---> 3,4,1,2
     *
     * @param array the array that should be shifted
     * @param pos   positions to shift to the right
     * @return the shifted array
     */
    private double[] shiftToRight(double[] array, int pos) {
        if (pos > array.length)
            pos = pos % array.length;

        double[] result = new double[array.length];

        for (int i = 0; i < pos; i++) {
            result[i] = array[array.length - pos + i];
        }

        int j = 0;
        for (int i = pos; i < array.length; i++) {
            result[i] = array[j];
            j++;
        }
        return result;
    }

    private double sum(double[] array) {
        double sum = 0;
        for (double anArray : array) {
            sum += anArray;
        }
        return sum;
    }

}
