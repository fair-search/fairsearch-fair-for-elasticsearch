package com.purbon.search.fair.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class BinarySearchAlphaAdjuster {

    private int n;
    private int k;
    private double p;
    private double alpha;
    private static final double EPS = 0.0001;
//    0.10002629313519074

    public BinarySearchAlphaAdjuster(int n, int k, double p, double alpha) {
        this.n = n;
        this.k = k;
        this.p = p;
        this.alpha = alpha;
    }

    public double adjustAlpha() {
        //double step = 0.001;
        //AlphaAdjuster adjuster = new AlphaAdjuster(n, k, p, alpha);
        //double oldSuccProb = adjuster.computeSuccessProbability();
        //double oldAlpha = this.alpha;
        //return adjustRecursive(oldAlpha, oldSuccProb, alpha);
        return adjustIterative(alpha);
    }

    private double adjustRecursive(double oldAlpha, double oldSuccProb, double startAlpha) {
        BigDecimal test = new BigDecimal(1-oldSuccProb);
        test.setScale(4, RoundingMode.FLOOR);
        BigDecimal a = new BigDecimal(startAlpha);
        a.setScale(4, RoundingMode.FLOOR);
        System.out.println(test.setScale(4,RoundingMode.FLOOR).doubleValue());
        if (test.setScale(4,RoundingMode.FLOOR).doubleValue() == a.setScale(4,RoundingMode.FLOOR).doubleValue()+EPS
                || test.setScale(4,RoundingMode.FLOOR).doubleValue() == a.setScale(4,RoundingMode.FLOOR).doubleValue() - EPS) {
            return oldAlpha;
        } else {
            if(1-oldSuccProb>startAlpha+EPS){
                oldAlpha = oldAlpha-(oldAlpha/2.0);
                //System.out.println(oldAlpha);
                AlphaAdjuster adjuster = new AlphaAdjuster(n, k, p, oldAlpha);
                oldSuccProb = adjuster.computeSuccessProbability();
            }else{
                oldAlpha = oldAlpha+(oldAlpha/2.0);
                //System.out.println(oldAlpha);
                AlphaAdjuster adjuster = new AlphaAdjuster(n, k, p, oldAlpha);
                oldSuccProb = adjuster.computeSuccessProbability();
            }
            return adjustRecursive(oldAlpha,oldSuccProb,startAlpha);
        }
    }

    private double adjustIterative(double alpha){
        double adjustedAlpha;
        double left = Double.MIN_VALUE;
        double right = alpha;
        AlphaAdjuster adj = new AlphaAdjuster(n,k,p,alpha);
        double min = 1-adj.computeSuccessProbability();
        double secondMin = 0;
        double minOptAlpha = alpha;
        double secondMinOptAlpha = 0;
        ArrayList<SuccessProbAlphaPair> succProbs=new ArrayList<>();

        while(left<=right){
            adjustedAlpha = (left+right)/2.0;
            AlphaAdjuster adjuster = new AlphaAdjuster(n,k,p,adjustedAlpha);
            double succProb = 1-adjuster.computeSuccessProbability();
            succProbs.add(new SuccessProbAlphaPair(succProb, adjustedAlpha));
            if(succProb <=0.00001 && succProb >0){
                return adjustedAlpha;
            }
            if(0.00001<succProb){
                //System.out.println(String.format("%.25f",adjustedAlpha));
                System.out.println(succProb);
                right=adjustedAlpha-0.0000000000000001;
                //oldSuccProb = succProb;
            }else{
                left=adjustedAlpha+0.0000000000000001;
                //System.out.println(String.format("%.25f",adjustedAlpha));
                System.out.println(succProb);
                //oldSuccProb = succProb;
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
        System.out.println("secondSearchStarted");
        double step = 0.00000001;
        AlphaAdjuster adj = new AlphaAdjuster(n,k,p,left);
        double oldSuccProb = 1-adj.computeSuccessProbability();
        ArrayList<SuccessProbAlphaPair> values = new ArrayList<>();
        while(left<right){
            left=left+step;
            AlphaAdjuster adjuster = new AlphaAdjuster(n,k,p,left);
            double succProb = 1-adjuster.computeSuccessProbability();
            System.out.println(succProb);
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
