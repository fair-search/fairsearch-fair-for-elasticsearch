package com.purbon.search.fair.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;

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
        AlphaAdjuster adjuster = new AlphaAdjuster(n, k, p, alpha);
        double oldSuccProb = adjuster.computeSuccessProbability();
        double oldAlpha = this.alpha;
        return adjust(oldAlpha, oldSuccProb, alpha);

    }

    private double adjust(double oldAlpha, double oldSuccProb, double startAlpha) {
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
            return adjust(oldAlpha,oldSuccProb,startAlpha);
        }
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
}
