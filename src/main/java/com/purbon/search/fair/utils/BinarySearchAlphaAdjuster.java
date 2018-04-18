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
        double step = 0.001;
        AlphaAdjuster adjuster = new AlphaAdjuster(n,k,p,alpha);
        BigDecimal oldSuccProb = new BigDecimal(adjuster.computeSuccessProbability());
        oldSuccProb.setScale(4, RoundingMode.CEILING);
        BigDecimal oldAlpha = new BigDecimal(this.alpha);
        oldAlpha.setScale(4,RoundingMode.CEILING);
        BigDecimal newAlpha = oldAlpha.subtract(new BigDecimal(step));
        return adjust(oldAlpha, newAlpha,oldSuccProb,1);

    }

    private double adjust(BigDecimal oldAlpha, BigDecimal newAlpha, BigDecimal oldSuccProb,int i) {
        System.out.println(newAlpha);
        System.out.println(i);
        double step = 0.001;
        AlphaAdjuster adjuster = new AlphaAdjuster(n,k,p,newAlpha.doubleValue());
        BigDecimal newSuccProb = new BigDecimal(adjuster.computeSuccessProbability());
        //System.out.println(roundToFourDigits(oldSuccProb));
        oldSuccProb.setScale(4,RoundingMode.CEILING);
        if(oldSuccProb.equals(0)){
            return oldAlpha.doubleValue();
        }else{

            return adjust(newAlpha, newAlpha.subtract(new BigDecimal(step)), newSuccProb, i+1);
        }

    }

    private double roundToFourDigits(double alpha){
        System.out.println(alpha);
        return (double)Math.round(alpha*100000000000d) / 100000000000d;
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
