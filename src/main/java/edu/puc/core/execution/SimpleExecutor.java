package edu.puc.core.execution;


import edu.puc.core.execution.cea.Traverser;
import edu.puc.core.execution.structures.CDS.CDSNode;
import edu.puc.core.execution.structures.CDS.CDSOutputNode;
import edu.puc.core.execution.structures.CDS.CDSUnionNode;
import edu.puc.core.execution.structures.states.State;
import edu.puc.core.execution.structures.states.StateTuple;
import edu.puc.core.parser.plan.cea.Transition;
import edu.puc.core.runtime.events.Event;
import edu.puc.core.runtime.predicates.BitSetGenerator;
import edu.puc.core.runtime.profiling.Profiler;

import java.util.*;

public class SimpleExecutor extends BaseExecutor {

    /* used to ignore non active states when updating state status map */
    private Set<State<?>> activeStates;

    SimpleExecutor(Traverser traverser, BitSetGenerator bitSetGenerator, boolean discardPartials) {
        super(traverser, bitSetGenerator, discardPartials);
    }


    @Override
    void setupCleanExecutor() {
        states = new HashMap<>();
        states.put(traverser.getInitialState(), CDSOutputNode.BOTTOM);

        activeStates = new HashSet<>();
        activeStates.add(traverser.getInitialState());
    }

    @Override
    public boolean sendEvent(Event event){
        /* same algorithm as old ANY */
        long startExecutionTime = System.nanoTime();
        Set<State<?>> newStates = new HashSet<>();
        Map<State<?>, CDSNode> _states = new HashMap<>();
        activeFinalStates = new HashSet<>();

        BitSet bitSet = bitSetGenerator.getBitSetFromEvent(event);

        for (State<?> currentState : activeStates) {
            @SuppressWarnings("unchecked")
            StateTuple nextStates = traverser.nextState(currentState, bitSet);
            State<?> blackState = nextStates.getBlackState();
            State<?> whiteState = nextStates.getWhiteState();

            if (!blackState.isRejectionState()) {
                update(event, newStates, _states, currentState, blackState, Transition.TransitionType.BLACK);
                if (blackState.isFinalState()) {
                    activeFinalStates.add(blackState);
                }
            }

            if (!whiteState.isRejectionState()) {
                newStates.add(whiteState);
                CDSNode newNode = states.get(currentState);
                updateStates(_states, newNode, whiteState);
            }
        }

        states = _states;
        activeStates = newStates;

        Profiler.addExecutionTime(System.nanoTime() - startExecutionTime);

        if (!activeFinalStates.isEmpty()) {
            long startEnumerationTime = System.nanoTime();
            enumerate(event);
            Profiler.addEnumerationTime(System.nanoTime() - startEnumerationTime);
            return true;
        }
        return false;
    }

    private void update(Event event, Set<State<?>> newStates, Map<State<?>, CDSNode> _states, State<?> currentState, State<?> state, Transition.TransitionType transitionType) {
        newStates.add(state);
        CDSOutputNode newNode = new CDSOutputNode(states.get(currentState), transitionType, event);
        updateStates(_states, newNode, state);
    }


    private void updateStates(Map<State<?>, CDSNode> _states, CDSNode newNode, State<?> state) {
        CDSNode stateCDS = _states.get(state);
        if (stateCDS != null) {
            if (newNode instanceof CDSOutputNode) {
                CDSOutputNode newOutputNode = (CDSOutputNode) newNode;
                _states.put(state, new CDSUnionNode(newOutputNode, stateCDS));
                return;
            } else {
                CDSUnionNode newUnionNode = (CDSUnionNode) newNode;
                if (stateCDS instanceof CDSOutputNode) {
                    _states.put(state, new CDSUnionNode(newUnionNode, (CDSOutputNode) stateCDS));
                    return;
                } else {
                    _states.put(state, new CDSUnionNode(newUnionNode, (CDSUnionNode) stateCDS));
                    return;
                }
            }
        }
        _states.put(state, newNode);
    }
}
