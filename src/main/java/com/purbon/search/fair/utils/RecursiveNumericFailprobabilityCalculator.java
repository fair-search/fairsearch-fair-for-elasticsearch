package com.purbon.search.fair.utils;

import java.util.ArrayList;
import java.util.HashMap;

public class RecursiveNumericFailprobabilityCalculator extends FailprobabilityCalculator {

    private static final double EPS = 0.0000000000000001;

    private HashMap<LegalAssignmentKey, Double> legalAssignmentCache = new HashMap<>();

    public RecursiveNumericFailprobabilityCalculator(int k, double p, double alpha) {
        super(k, p, alpha);
    }


    public MTableFailProbPair adjustAlpha() {
        double aMin = 0;
        double aMax = alpha;
        double aMid = (aMin + aMax) / 2;

        MTableFailProbPair min = computeBoundary(k, p, aMin);
        MTableFailProbPair max = computeBoundary(k, p, aMax);
        MTableFailProbPair mid = computeBoundary(k, p, aMid);

        while (min.getMassOfMTable() < max.getMassOfMTable() && mid.getFailProb() != alpha) {


            if (mid.getFailProb() < alpha) {
                aMin = aMid;
                min = computeBoundary(k, p, aMin);
            } else if (mid.getFailProb() > alpha) {
                aMax = aMid;
                max = computeBoundary(k, p, aMax);
            }
            aMid = (aMin + aMax) / 2;
            mid = computeBoundary(k, p, aMid);

            int maxMass = max.getMassOfMTable();
            int minMass = min.getMassOfMTable();
            int midMass = mid.getMassOfMTable();
            if(maxMass-minMass == 1 || max.getAlpha()-min.getAlpha()<=EPS){
                double minDiff = Math.abs(min.getFailProb() - alpha);
                double maxDiff = Math.abs(max.getFailProb() - alpha);
                if(minDiff<= maxDiff){
                    return min;
                }else{
                    return max;
                }
            }
            if (maxMass-midMass == 1 && midMass-minMass == 1) {
                double minDiff = Math.abs(min.getFailProb() - alpha);
                double maxDiff = Math.abs(max.getFailProb() - alpha);
                double midDiff = Math.abs(mid.getFailProb() - alpha);
                if (midDiff <= maxDiff && midDiff <= minDiff) {
                    return mid;
                }
                if(minDiff <= midDiff && minDiff <= maxDiff){
                    return min;
                }else{
                    return max;
                }
            }
        }

        return mid;


    }

    private MTableFailProbPair computeBoundary(int k, double p, double alpha) {
        int[] mTable = new MTableGenerator(k, p, alpha, false).getMTable();
        double failProb = calculateFailprobability(mTable, alpha);
        return new MTableFailProbPair(k, p, alpha, failProb, mTable);
    }

    @Override
    double calculateFailprobability(int[] mtable, double alpha) {
        if (mtable[mtable.length - 1] == 0) {
            return 0;
        }
        MTableGenerator generator = new MTableGenerator(k, p, alpha, false);
        this.mTable = mtable;
        this.auxMTable = generator.computeAuxTMTable();
        int maxProtected = auxMTable.getSumOf("block");
        ArrayList<Integer> blockSizes = auxMTable.getColumn("block");
        blockSizes = sublist(blockSizes, 1, blockSizes.size());
        double succesProb = findLegalAssignments(maxProtected, blockSizes);
        return succesProb == 0 ? 0 : 1 - succesProb;
    }

    private double findLegalAssignments(int numCandidates, ArrayList<Integer> blockSizes) {

        return findLegalAssignmentsAux(numCandidates, blockSizes, 1, 0);
    }

    private double findLegalAssignmentsAux(int numCandidates, ArrayList<Integer> blockSizes
            , int currentBlockNumber, int candidatesAssignedSoFar) {
        if (blockSizes.size() == 0) {
            return 1;
        } else {
            int minNeededThisBlock = currentBlockNumber - candidatesAssignedSoFar;
            if (minNeededThisBlock < 0) {
                minNeededThisBlock = 0;
            }
            int maxPossibleThisBlock = Math.min(blockSizes.get(0), numCandidates);
            double assignments = 0;
            ArrayList<Integer> newRemainingBlockSizes = sublist(blockSizes, 1, blockSizes.size());
            for (int itemsThisBlock = minNeededThisBlock; itemsThisBlock <= maxPossibleThisBlock; itemsThisBlock++) {
                int newRemainingCandidates = numCandidates - itemsThisBlock;

                double suffixes = calculateLegalAssignmentsAux(newRemainingCandidates, newRemainingBlockSizes
                        , currentBlockNumber + 1, candidatesAssignedSoFar + itemsThisBlock);

                assignments = assignments + getFromPmfCache(maxPossibleThisBlock, itemsThisBlock) * suffixes;

            }
            return assignments;
        }
    }

    private double calculateLegalAssignmentsAux(int remainingCandidates, ArrayList<Integer> remainingBlockSizes
            , int currentBlockNumber, int candidatesAssignedSoFar) {
        LegalAssignmentKey key = new LegalAssignmentKey(remainingCandidates, remainingBlockSizes
                , currentBlockNumber, candidatesAssignedSoFar);
        if (legalAssignmentCache.get(key) != null) {
            return legalAssignmentCache.get(key);
        } else {
            double value = findLegalAssignmentsAux(remainingCandidates, remainingBlockSizes, currentBlockNumber, candidatesAssignedSoFar);
            legalAssignmentCache.put(key, value);
            return value;
        }

    }


}
