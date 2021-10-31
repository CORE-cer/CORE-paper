package edu.puc.core.execution.structures.states;

import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

public class State<T extends SimpleStateSet> {
    private int id;
    private int visited = 0;
    private boolean isFinal = false;
    private Map<BitSet, StateTuple> knownTransitions;
    public static final int rejectionId = -1;

    public State(int id){
        this.id = id;
    }

    public State(int id, int visited, boolean isFinal, T stateSet, Map<BitSet, StateTuple> knownTransitions){
        this.id = id;
        this.visited = visited;
        this.isFinal = isFinal;
        this.stateSet = stateSet;
        this.knownTransitions = new HashMap<>(knownTransitions);
    }

    public void setFinal(boolean isFinal) {
        this.isFinal = isFinal;
    }

    public int getId() {
        return id;
    }

    public boolean isFinalState() {
        return isFinal;
    }

    public boolean isRejectionState(){
        return id == rejectionId;
    }

    public void setKnownTransitions(Map<BitSet, StateTuple> knownTransitions) {
        this.knownTransitions = knownTransitions;
    }

    private T stateSet;

    public void setStateSet(T stateSet) {
        this.stateSet = stateSet;
    }

    public T getStateSet() {
        return stateSet;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof State) {
            return id == ((State<?>) obj).getId();
        }
        return false;
    }

    @Override
    public final String toString(){
        return "State(" + stateSet + ")";
    }
}
