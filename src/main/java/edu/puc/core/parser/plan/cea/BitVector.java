package edu.puc.core.parser.plan.cea;

import java.util.BitSet;

public class BitVector {

    private BitSet mask;
    private BitSet match;

    private static final BitVector TRUE_BIT_VECTOR = new BitVector();
    private static final BitVector FALSE_BIT_VECTOR = new BitVector();

    private BitVector(){ }

    BitVector(BitSet mask, BitSet match){
        this.mask = mask;
        this.match = match;
    }

    /**
     * Returns the BitVector's mask. If this object is a TRUE_BIT_VECTOR or a FALSE_BIT_VECTOR
     * the return value may be null, be sure to check beforehand if this is the case.
     *
     * @return The vector's mask BitSet
     */
    public BitSet getMask() {
        return mask;
    }

    /**
     * Returns the BitVector's match bitSet. If this object is a TRUE_BIT_VECTOR or a FALSE_BIT_VECTOR
     * the return value may be null, be sure to check beforehand if this is the case.
     *
     * @return The vector's match BitSet
     */
    public BitSet getMatch() {
        return match;
    }

    /**
     * First checks if this {@link BitVector} is compatible with the other one,
     * then returns the {@link BitVector} resulting of performing the cojoin
     * operation (OR'ing each the mask and the match).
     * 
     * @param other {@link BitVector} to cojoin with.
     * @return the resulting {@link BitVector}. 
     */
    public BitVector cojoin(BitVector other){
        if (isTrueVector()) return other.copy();
        if (isFalseVector()) return copy();

        // Testing if these predicates are satisfiable together
        BitSet temp = (BitSet) match.clone();
        temp.and(other.mask);

        BitSet temp2 = (BitSet) other.match.clone();
        temp2.and(mask);

        temp.xor(temp2);

        if (!temp.isEmpty()){
            // these filters are incompatible
            return getFalseBitVector();
        }

        BitVector newBitVector = copy();
        newBitVector.mask.or(other.mask);
        newBitVector.match.or(other.match);

        if (newBitVector.match.isEmpty()) return FALSE_BIT_VECTOR;

        return newBitVector;
    }

    public BitVector copy() {
        if (isFalseVector() || isTrueVector()) return this;
        return new BitVector((BitSet)mask.clone(), (BitSet)match.clone());
    }

    public boolean isFalseVector() {
        return this == FALSE_BIT_VECTOR;
    }

    public boolean isTrueVector() {
        return this == TRUE_BIT_VECTOR;
    }

    public static BitVector getFalseBitVector(){
        return FALSE_BIT_VECTOR;
    }

    public static BitVector getTrueBitVector(){
        return TRUE_BIT_VECTOR;
    }

    @Override
    public int hashCode() {
        if (isFalseVector() || isTrueVector()) {
            return super.hashCode();
        }
        return mask.hashCode() * 29 + match.hashCode() * 13;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof BitVector)) return false;
        BitVector other = (BitVector)obj;
        return (mask.equals(other.mask) && match.equals(other.match));
    }

    @Override
    public String toString() {
        int nBits = PredicateFactory.getInstance().getBitCount();

        if (isTrueVector()) return String.format("[%-" + (nBits)  + "s]", String.format("%" + (4 + (nBits - 4) / 2) + "s", "TRUE"));
        if (isFalseVector()) return String.format("[%-" + (nBits)  + "s]", String.format("%" + (5 + (nBits - 5) / 2) + "s", "FALSE"));

        StringBuilder stringBuilder = new StringBuilder("[");

        for (int bit=0; bit < nBits; bit++){
            if (mask.get(bit)) stringBuilder.append(match.get(bit) ? "b" : "w");
            else stringBuilder.append("*");
        }

        return stringBuilder.append("]").toString();
    }
}

