package com.purbon.search.fair.utils;

import org.apache.commons.math3.distribution.BinomialDistribution;

public class Distribution {

    public static void main(String [] args) throws  Exception {

        // trials == k , p == p
        BinomialDistribution d = new BinomialDistribution(8,0.7);
        /*
           NOTE:
             si alpha es 0.1, el valor triat es la primera x que sigui
             major a 0.1
          */
        d.cumulativeProbability(3);

    }
}
