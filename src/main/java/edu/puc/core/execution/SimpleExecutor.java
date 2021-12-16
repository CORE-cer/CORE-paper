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

// TODO this is outdated compared to the TimeWindowsExecutor.java
public class SimpleExecutor extends BaseExecutor {

    SimpleExecutor(Traverser traverser, BitSetGenerator bitSetGenerator, boolean discardPartials) {
        super(traverser, bitSetGenerator, discardPartials);
    }


    @Override
    void setupCleanExecutor() {
        states = new HashMap<>();
    }

    private void startNewRun() {
        states.put(traverser.getInitialState(), CDSNode.BOTTOM);
    }

    @Override
    public boolean sendEvent(Event event){
        /* same algorithm as old ANY */
        long startExecutionTime = System.nanoTime();
        Map<State<?>, CDSNode> _states = new HashMap<>();
        activeFinalStates = new HashSet<>();

        // TODO
        // This solves the bug that new states where not checked after the first iteration.
        // But may have introduce a bug, be careful.
        startNewRun();

        BitSet bitSet = bitSetGenerator.getBitSetFromEvent(event);

        for (State<?> currentState : states.keySet()) {
            @SuppressWarnings("unchecked")
            StateTuple nextStates = traverser.nextState(currentState, bitSet);
            State<?> blackState = nextStates.getBlackState();
            State<?> whiteState = nextStates.getWhiteState();

            // This rejectionState is similar to the empty set.
            if (!blackState.isRejectionState()) {
                // Algorithm 1. Lines 15-18
                CDSOutputNode newNode = new CDSOutputNode(states.get(currentState), Transition.TransitionType.BLACK, event);
                updateStates(_states, newNode, blackState);
                // Why only black transitions active the final state?
                if (blackState.isFinalState()) {
                    activeFinalStates.add(blackState);
                }
            }

            if (!whiteState.isRejectionState()) {
                CDSNode newNode = states.get(currentState);
                updateStates(_states, newNode, whiteState);
            }
        }

        states = _states;

        Profiler.addExecutionTime(System.nanoTime() - startExecutionTime);

        if (!activeFinalStates.isEmpty()) {
            long startEnumerationTime = System.nanoTime();
            enumerate(event);
            Profiler.addEnumerationTime(System.nanoTime() - startEnumerationTime);
            return true;
        }
        return false;
    }

    // This is Algorithm 1. procedure ADD
    // But we don't use union-lists, so it is a bit different.
    private void updateStates(Map<State<?>, CDSNode> _states, CDSNode newNode, State<?> state) {
        CDSNode stateCDS = _states.get(state);
        if (stateCDS == null) {
            _states.put(state, newNode);
        } else {
            _states.put(state, new CDSUnionNode(newNode, stateCDS));
        }
    }
}
