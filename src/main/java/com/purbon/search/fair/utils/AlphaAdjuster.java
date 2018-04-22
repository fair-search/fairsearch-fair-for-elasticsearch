package com.purbon.search.fair.utils;

import org.apache.commons.math3.distribution.BinomialDistribution;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
        if (alpha <= 0.0 || alpha >= 1.0) {
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
                //System.out.println("call " + i + "currentTrial[i]" + currentTrial.get(i));
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

    public ArrayList<ArrayList<Double>> adjustPmfCache(ArrayList<ArrayList<Double>> pmfCache, int blocklength) {
        if (pmfCache.size() < blocklength) {
            for (int i = pmfCache.size(); i <= blocklength; i++) {
                pmfCache.add(null);
            }
        }
        return pmfCache;
    }

    public double[] increase(int i, double[] successObtainedProb, ArrayList<Double> currentTrial) {
        double[] shifted = shiftToRight(successObtainedProb, i);
        for (int j = 0; j < shifted.length; j++) {
//            System.out.println("|shifted[j]|"+shifted[j]+"|shiftedLength|"+shifted.length+"|currentTrialSize|"+currentTrial.size()+"|i|"+i+"|currentTrial[i]|"+currentTrial.get(i));
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
        for (int i = 0; i < array.length; i++) {
            sum += array[i];
        }
        return sum;
    }

    public static void main(String[] args) {



//        double a = 0.37500000000000017;
//        BigDecimal b = new BigDecimal(a).setScale(16,RoundingMode.FLOOR);
//        System.out.println(b.doubleValue());
//        double[] arr = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        AlphaAdjuster alphaAdjuster = new AlphaAdjuster(10000, 10000, 0.5, 0.1);
//        double[] result = alphaAdjuster.shiftToRight(arr, 12);
//        String s = "{";
//        for (int i = 0; i < result.length; i++) {
//            s += result[i];
//            s += ", ";
//        }
//        s+="}";
//        System.out.println(s);
        //        //for(int i=0; i<alphaAdjuster.mTable.length; i++){
//        //    System.out.println(i+"  "+alphaAdjuster.mTable[i]);
//        //}
//
//        //BinomialDistribution dist = new BinomialDistribution(39,0.5);
//        //System.out.println(dist.inverseCumulativeProbability(0.1));
//        //System.out.println(alphaAdjuster.computeAuxTMTable().toString());
        System.out.println(1.0-alphaAdjuster.computeSuccessProbability());
//        BigDecimal count = new BigDecimal(alphaAdjuster.computeSuccessProbability());
//        count = count.setScale(7, RoundingMode.CEILING);
//        //double count = (double)Math.round((alphaAdjuster.computeSuccessProbability()*100000.0)/100000.0);
//        System.out.println(count);
//        //System.out.println(count.doubleValue());
//        BinarySearchAlphaAdjuster adjuster = new BinarySearchAlphaAdjuster(1500, 1500, 0.2, 0.1);
//        System.out.println(adjuster.adjustAlpha());

}


}
