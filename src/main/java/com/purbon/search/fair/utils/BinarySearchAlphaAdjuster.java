package com.purbon.search.fair.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;

public class BinarySearchAlphaAdjuster {

    private int n;
    private int k;
    private double p;
    private double alpha;
    private BigDecimal eps = new BigDecimal(0.0001);

    public BinarySearchAlphaAdjuster(int n, int k, double p, double alpha) {
        this.n = n;
        this.k = k;
        this.p = p;
        this.alpha = alpha;
    }

    public double adjustAlpha() {
        double step = 0.0001;
        AlphaAdjuster adjuster = new AlphaAdjuster(n,k,p,alpha);
        double oldSuccProb = adjuster.computeSuccessProbability();
        double oldAlpha = this.alpha;
        double newAlpha = oldAlpha/2.0;
        return adjust(oldAlpha, newAlpha,oldSuccProb);

    }

    private double adjust(double oldAlpha, double newAlpha, double oldSuccProb) {
        System.out.println(oldSuccProb);
        if(oldSuccProb<=0.0000000001){
            return oldAlpha;
        }else {
            //System.out.println(oldAlpha);
            double step = 0.0001;
            AlphaAdjuster adjuster = new AlphaAdjuster(n, k, p, newAlpha);
            double newSuccProb = adjuster.computeSuccessProbability();
            //System.out.println(roundToFourDigits(oldSuccProb));
            //oldSuccProb.setScale(4,RoundingMode.CEILING);
            return adjust(newAlpha, newAlpha/2.0, newSuccProb);
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
