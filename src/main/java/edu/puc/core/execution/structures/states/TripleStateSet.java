package edu.puc.core.execution.structures.states;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class TripleStateSet extends DoubleStateSet {
    private final Set<Integer> thirdStateSet;

    public TripleStateSet(Set<Integer> firstStateSet, Set<Integer> secondStateSet, Set<Integer> thirdStateSet) {
        super(firstStateSet, secondStateSet);
        this.thirdStateSet = new HashSet<>(thirdStateSet);
    }

    public Set<Integer> getThirdStateSet() {
        return thirdStateSet;
    }

    @Override
    public final int hashCode() {
        return Objects.hash(stateSet, secondStateSet, thirdStateSet);
    }

    @Override
    public final boolean equals(final Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof TripleStateSet)) return false;
        return (stateSet.equals(((TripleStateSet) obj).stateSet)) &&
                secondStateSet.equals(((TripleStateSet) obj).secondStateSet) &&
                thirdStateSet.equals(((TripleStateSet) obj).thirdStateSet);
    }

    @Override
    public final String toString() {
        return "TripleStateSet(" + stateSet + ", " + secondStateSet + ", " + thirdStateSet + ")";
    }
}
