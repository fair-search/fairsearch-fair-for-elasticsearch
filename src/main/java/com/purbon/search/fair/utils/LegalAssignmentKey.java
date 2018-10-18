package com.purbon.search.fair.utils;

import java.util.ArrayList;

public class LegalAssignmentKey {

    private final int remainingCandidates;
    private final ArrayList<Integer> remainingBlockSizes;
    private final int currentBlockNumber;
    private final int candidatesAssignedSoFar;

    public LegalAssignmentKey(int remainingCandidates, ArrayList<Integer> remainingBlockSizes
            , int currentBlockNumber, int candidatesAssignedSoFar) {
        this.remainingCandidates = remainingCandidates;
        this.remainingBlockSizes = remainingBlockSizes;
        this.currentBlockNumber = currentBlockNumber;
        this.candidatesAssignedSoFar = candidatesAssignedSoFar;
    }

    @Override
    public boolean equals(final Object o){
        if(!(o instanceof LegalAssignmentKey)) return false;
        if(((LegalAssignmentKey) o).remainingCandidates != remainingCandidates) return false;
        if (((LegalAssignmentKey) o).currentBlockNumber != currentBlockNumber) return false;
        if(((LegalAssignmentKey) o).candidatesAssignedSoFar != candidatesAssignedSoFar) return false;
        if(!remainingBlockSizesAreEqual(((LegalAssignmentKey) o).remainingBlockSizes, remainingBlockSizes)) return false;
        return true;
    }

    @Override
    public int hashCode(){
        return (remainingCandidates+remainingBlockSizes.size() << 16) + currentBlockNumber+candidatesAssignedSoFar;
    }

    private boolean remainingBlockSizesAreEqual(ArrayList<Integer> bs1, ArrayList<Integer> bs2){
        if(bs1.size() != bs2.size()) return false;
        for(int i=0; i<bs1.size(); i++){
            if(!bs1.get(i).equals(bs2.get(i))){
                return false;
            }
        }
        return true;
    }

    public int getRemainingCandidates() {
        return remainingCandidates;
    }

    public ArrayList<Integer> getRemainingBlockSizes() {
        return remainingBlockSizes;
    }

    public int getCurrentBlockNumber() {
        return currentBlockNumber;
    }

    public int getCandidatesAssignedSoFar() {
        return candidatesAssignedSoFar;
    }
}
