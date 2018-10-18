package com.purbon.search.fair.utils;

public class BinomDistKey {

    private final int trials;
    private final int successes;

    public BinomDistKey(int trials, int successes){
        this.trials = trials;
        this.successes = successes;
    }

    @Override
    public boolean equals(final Object o){
        if(!(o instanceof BinomDistKey)) return false;
        if(((BinomDistKey) o).getSuccesses() != successes) return false;
        if(((BinomDistKey) o).getTrials() != trials) return false;
        return true;
    }

    @Override
    public int hashCode(){
        return (trials << 16) + successes;

    }

    public int getTrials(){
        return trials;
    }

    public int getSuccesses(){
        return successes;
    }
}
