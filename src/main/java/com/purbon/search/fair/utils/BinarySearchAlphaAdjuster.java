package com.purbon.search.fair.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;

public class BinarySearchAlphaAdjuster {

    private int n;
    private int k;
    private double p;
    private double alpha;
    private static final double STEP = 0.0000000000000001;

    public BinarySearchAlphaAdjuster(int n, int k, double p, double alpha) {
        this.n = n;
        this.k = k;
        this.p = p;
        this.alpha = alpha;
    }

    public double adjustAlpha() {
        return adjustIterative(alpha);
    }


    private double adjustIterative(double alpha){
        double adjustedAlpha;
        double left = Double.MIN_VALUE;
        double right = alpha;
        AlphaAdjuster adj = new AlphaAdjuster(n,k,p,alpha);
        double min = adj.computeSuccessProbability();
        double secondMin = 0;
        double minOptAlpha = alpha;
        double secondMinOptAlpha = 0;
        ArrayList<SuccessProbAlphaPair> succProbs=new ArrayList<>();

        while(left<=right){
            adjustedAlpha = (left+right)/2.0;
            AlphaAdjuster adjuster = new AlphaAdjuster(n,k,p,adjustedAlpha);
            double succProb = adjuster.computeSuccessProbability();
            succProbs.add(new SuccessProbAlphaPair(succProb, adjustedAlpha));
            if(succProb <=0.00001 && succProb >0){
                return adjustedAlpha;
            }
            if(0.00001<succProb){
                right=adjustedAlpha-STEP;
            }else{
                left=adjustedAlpha+STEP;
            }
            if(succProb<min){
                secondMin = min;
                secondMinOptAlpha =minOptAlpha;
                min=succProb;
                minOptAlpha = adjustedAlpha;

            }

        }
        return secondSearch(minOptAlpha, secondMinOptAlpha);
    }

    private double secondSearch(double left, double right){
        double step = 0.00000001;
        AlphaAdjuster adj = new AlphaAdjuster(n,k,p,left);
        double oldSuccProb = 1-adj.computeSuccessProbability();
        ArrayList<SuccessProbAlphaPair> values = new ArrayList<>();
        while(left<right){
            left=left+step;
            AlphaAdjuster adjuster = new AlphaAdjuster(n,k,p,left);
            double succProb = 1-adjuster.computeSuccessProbability();
            if(oldSuccProb<succProb){
                break;
            }
            oldSuccProb = succProb;
            values.add(new SuccessProbAlphaPair(succProb,left));
        }
        return values.size()>1 ? getMinAlpha(values): left;
    }

    private double getMinAlpha(ArrayList<SuccessProbAlphaPair> list){
        System.out.println("Looking for minimum");
        double min = list.get(0).getSuccProb();
        double alpha = list.get(0).getAlpha();
        for(SuccessProbAlphaPair p : list){
            if(p.getSuccProb()<min){
                min = p.getSuccProb();
                alpha = p.getAlpha();
            }
        }
        return alpha;
    }

    public int getN() {
        return n;
    }

    public void setN(int n) {
        this.n = n;
    }

    public int getK() {
        return k;
    }

    public void setK(int k) {
        this.k = k;
    }

    public double getP() {
        return p;
    }

    public void setP(double p) {
        this.p = p;
    }

    public double getAlpha() {
        return alpha;
    }

    public void setAlpha(double alpha) {
        this.alpha = alpha;
    }

    private class SuccessProbAlphaPair{
        double succProb;
        double alpha;

        public SuccessProbAlphaPair(double succProb, double alpha){
            this.succProb = succProb;
            this.alpha = alpha;
        }

        public double getSuccProb() {
            return succProb;
        }

        public double getAlpha() {
            return alpha;
        }
    }
}
