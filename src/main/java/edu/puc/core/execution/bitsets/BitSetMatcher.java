package edu.puc.core.execution.bitsets;

import edu.puc.core.parser.plan.cea.BitVector;

import java.util.BitSet;

public class BitSetMatcher {

    /**
     * The satisfaction is defined in terms of the {@link BitSet} having the
     * same value as the {@link BitVector} for every predicate present in the
     * latter.
     *
     * @param bitSet
     * @param bitVector
     * @return True iff the {@link BitVector} is satisfied.
     */
    public static boolean bitSetSatisfiesVector(BitSet bitSet, BitVector bitVector){
        if (bitVector.isFalseVector()) return false;
        if (bitVector.isTrueVector()) return true;
        BitSet result = (BitSet)bitSet.clone();
        result.and(bitVector.getMask());
        result.xor(bitVector.getMatch());
        return result.isEmpty();
    }

}
