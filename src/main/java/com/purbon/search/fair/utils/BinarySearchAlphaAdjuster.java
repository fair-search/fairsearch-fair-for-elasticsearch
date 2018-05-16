package com.purbon.search.fair.utils;

import java.util.ArrayList;

public class BinarySearchAlphaAdjuster {


    private int k;
    private double p;
    private double alpha;
    private static final double STEP = 0.000001;

    public BinarySearchAlphaAdjuster(int k, double p, double alpha) {
        this.k = k;
        this.p = p;
        if (alpha < 0.001) {
            throw new IllegalArgumentException("Alpha has to be greater than or equal to 0.001 for an adjustment.");
        }
        this.alpha = alpha;
    }

    public double adjustAlpha() {
        if(this.k<40){
            return adjustFlatSearch();
        }else {
            return adjustIterative();
        }
    }

    private double adjustFlatSearch(){
        AlphaAdjuster minAdjuster = new AlphaAdjuster(k,p,alpha);
        double min = minAdjuster.computeSuccessProbability();
        double minAlpha = alpha;
        //TODO Tom: calculate a reasonable search size
        for(int i=0; i<100; i++){
            double adjustedAlpha = alpha-(i*0.001);
            AlphaAdjuster adjuster = new AlphaAdjuster(k,p,adjustedAlpha);
            double currentSuccessProb = adjuster.computeSuccessProbability();
            if(Math.abs(currentSuccessProb-alpha)<Math.abs(min-alpha)){
                min = currentSuccessProb;
                minAlpha = adjustedAlpha;
            }
        }
        return minAlpha;
    }


    private double adjustIterative() {
        double adjustedAlpha;
        double left = Double.MIN_VALUE;
        double right = alpha;
        double minOptAlpha = (left + right) / 2.0;

        while (left <= right) {
            adjustedAlpha = (left + right) / 2.0;
            AlphaAdjuster adjuster = new AlphaAdjuster(k, p, adjustedAlpha);
            double succProb = adjuster.computeSuccessProbability();
            //succProbs.add(new SuccessProbAlphaPair(succProb, adjustedAlpha));
            if (Math.abs(succProb - alpha) <= 0.0001) {
                return adjustedAlpha;
            } else if (Math.abs(succProb - alpha) < Math.abs(succProb - minOptAlpha)) {
                minOptAlpha = adjustedAlpha;
            }
            if (alpha < succProb) {
                right = adjustedAlpha - STEP;
            } else {
                left = adjustedAlpha + STEP;
            }

        }
        return minOptAlpha;
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

}
