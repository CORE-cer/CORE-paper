package edu.puc.core.execution.cea;

import edu.puc.core.execution.structures.states.SimpleStateSet;
import edu.puc.core.execution.structures.states.State;
import edu.puc.core.execution.structures.states.StateTuple;

import java.util.*;

public abstract class Traverser<S extends SimpleStateSet> {
    protected ExecutableCEA cea;
    private Integer newStateNumber = 0;
    private boolean initialized = false;

    // Each position corresponds to a state.
    List<Map<BitSet, StateTuple>> knownTransitionsList;

    Integer INITIAL;

    private Map<SimpleStateSet, State<S>> stateSetToStateMap; // From Set<Int> to State(contains the set of Ints)
    private final State<S> rejectState = new State<>(State.rejectionId);
    private final State<S> initialState = new State<>(0);

    Traverser(ExecutableCEA cea) {
        this.cea = cea;

        INITIAL = cea.getInitialState();

        knownTransitionsList = new ArrayList<>();
        knownTransitionsList.add(new HashMap<>());
    }

    void init(S startSet){
        ensureFirstInitialization();
        stateSetToStateMap = new HashMap<>();
        initialState.setFinal(cea.isFinal(INITIAL));
        initialState.setStateSet(startSet);
        initialState.setKnownTransitions(knownTransitionsList.get(0));
        stateSetToStateMap.put(startSet, initialState);
    }

    void getNextStates(Set<Integer> fromStates, BitSet vector, Set<Integer> blackSet, Set<Integer> whiteSet) {
        for (Integer s : fromStates) {
            blackSet.addAll(cea.blackTransition(s, vector));
            whiteSet.addAll(cea.whiteTransition(s, vector));
        }
    }

    State<S> getStateFromStateSet(S stateSet) {
        State<S> newState = stateSetToStateMap.get(stateSet);
        if (newState == null) {
            if (stateSet.isEmpty()){
                newState = rejectState;
            }
            else {
                HashMap<BitSet, StateTuple> knownTransitions = new HashMap<>();
                knownTransitionsList.add(knownTransitions);
                boolean isFinal = stateSet.getStateSet().stream().anyMatch(cea::isFinal);
                newState = new State<>(++newStateNumber, 0, isFinal, stateSet, knownTransitions);
            }
            stateSetToStateMap.put(stateSet, newState);

        }
        return newState;
    }

    /**
     * Return both {@link State States} from the white and the black transition.
     *
     * @param fromState Current {@link State} of execution.
     * @param vector {@link BitSet} to identify the transition to use.
     * @return The next states in execution.
     */
    public StateTuple nextState(State<S> fromState, BitSet vector){
        StateTuple toStates = knownTransitionsList.get(fromState.getId()).get(vector);
        if (toStates == null) {
            toStates = calculateNextState(fromState, vector);
            knownTransitionsList.get(fromState.getId()).put(vector, toStates);
        }
        return toStates;
    }

    abstract StateTuple calculateNextState(State<S> fromState, BitSet vector);

    public State<S> getInitialState() {
        return initialState;
    }

    public State getRejectState() {
        return rejectState;
    }

    private void ensureFirstInitialization() {
        if (initialized) {
            throw new Error("Traverser already initialized");
        }
        initialized = true;
    }

}
