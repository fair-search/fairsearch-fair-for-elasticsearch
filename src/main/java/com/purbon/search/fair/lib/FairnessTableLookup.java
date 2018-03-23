package com.purbon.search.fair.lib;

public interface FairnessTableLookup {

    float fairness(int trials, float proportion, float significance);

}
