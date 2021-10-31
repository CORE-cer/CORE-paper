package edu.puc.core.execution.cea;

import java.util.BitSet;
import java.util.Set;

public abstract class ExecutableCEA {
    public abstract Integer getInitialState();
    public abstract int getFinalState();
    public abstract boolean isFinal(Integer state);
    public abstract int getNStates();
    public abstract Set<Integer> blackTransition(Integer state, BitSet b);
    public abstract Set<Integer> whiteTransition(Integer state, BitSet b);
}
