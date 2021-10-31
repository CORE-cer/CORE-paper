package edu.puc.core.execution.structures.states;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class DoubleStateSet extends SimpleStateSet {
    protected final Set<Integer> secondStateSet;

    public DoubleStateSet(Set<Integer> firstStateSet, Set<Integer> secondStateSet){
        super(firstStateSet);
        this.secondStateSet = new HashSet<>(secondStateSet);
    }

    public Set<Integer> getFirstStateSet() {
        return super.getStateSet();
    }

    public Set<Integer> getSecondStateSet() {
        return secondStateSet;
    }

    @Override
    public int hashCode() {
        return Objects.hash(stateSet, secondStateSet);
    }

    @Override
    public boolean equals(final Object obj){
        if (this == obj) return true;
        if (!(obj instanceof DoubleStateSet)) return false;
        return stateSet.equals(((DoubleStateSet) obj).stateSet) && secondStateSet.equals(((DoubleStateSet) obj).secondStateSet);
    }

    @Override
    public String toString(){
        return "DoubleStateSet(" + stateSet + ", " + secondStateSet + ")";
    }
}
