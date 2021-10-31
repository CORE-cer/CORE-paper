package edu.puc.core.execution.structures.states;

public class StateTuple {

    private State<?> blackState;
    private State<?> whiteState;

    public StateTuple(){ }

    public StateTuple(State<?> blackState, State<?> whiteState) {
        this.blackState = blackState;
        this.whiteState = whiteState;
    }

    public State<?> getBlackState() {
        return blackState;
    }

    public State<?> getWhiteState() {
        return whiteState;
    }

    public void setBlackState(State<?> blackState) {
        this.blackState = blackState;
    }

    public void setWhiteState(State<?> whiteState) {
        this.whiteState = whiteState;
    }
}