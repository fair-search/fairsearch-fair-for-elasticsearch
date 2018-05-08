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
        if (alpha < 0.001 || alpha >= 1.0) {
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

    /**
     * Stores the inverse of an mTable entry and the size of the block with respect to the inverse
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

    /**
     * Computes the probability of rejecting a fair ranking with the given parameters n, p and alpha
     * @return The probability of rejecting a fair ranking
     */
    public double computeSuccessProbability() {
        int maxProtected = auxMTable.getLengthOf("inv") - 1;
        int minProtected = 1;
        double successProbability = 0;

        ArrayList<Double> currentTrial;

        double[] successObtainedProb = new double[maxProtected];
        successObtainedProb = fillWithZeros(successObtainedProb);
        successObtainedProb[0] = 1.0;
        //Cache for the probability Mass Function for every trial
        //a trial is a block and every list in pmfCache is the pmf of a block of
        //a certain size (pmfCache.get(2) is a list of the probability mass function values
        // of a block of the size 2)
        ArrayList<ArrayList<Double>> pmfCache = new ArrayList<>();

        while (minProtected < maxProtected) {
            //get the current blockLength from auxMTable
            int blockLength = auxMTable.at(minProtected, "block");
            if (blockLength <= pmfCache.size() && pmfCache.get(blockLength) != null) {
                currentTrial = pmfCache.get(blockLength);
            } else {
                currentTrial = new ArrayList<>();
                //this has to be done to simulate an arrayList of the blocklength-size
                for (int j = 0; j <= blockLength; j++) {
                    currentTrial.add(null);
                }
                BinomialDistribution binomDist = new BinomialDistribution(blockLength, p);
                for (int i = 0; i <= blockLength; i++) {
                    //enter the pmf value for position i in a block of blockLength size
                    currentTrial.set(i, binomDist.probability(i));
                }

                //insert empty lists so that we have the current trial inserted on the right position
                pmfCache = adjustPmfCache(pmfCache, blockLength);
                pmfCache.set(blockLength, currentTrial);
            }
            //initialize with zeroes
            double[] newSuccessObtainedProb = fillWithZeros(new double[maxProtected]);
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

    /**
     * Shifts all entries of an array to the right for pos positions
     * Example: shiftToRight('1,2,3,4',2) ---> 3,4,1,2
     * @param array the array that should be shifted
     * @param pos positions to shift to the right
     * @return the shifted array
     */
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
