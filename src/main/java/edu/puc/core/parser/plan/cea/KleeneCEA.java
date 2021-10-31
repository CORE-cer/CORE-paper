package edu.puc.core.parser.plan.cea;


import static java.util.stream.Collectors.toList;

/**
 * Kleene {@link CEA} that supports Kleene's closure.
 */

public class KleeneCEA extends CEA {
    public KleeneCEA(CEA inner) {
        // copying inner automaton
        super();

        stateCount = inner.stateCount;
        labelSet.addAll(inner.labelSet);

        int oldFinal = stateCount - 1;

        // add a new final state
        stateCount++;


        // copy all inner transitions
        transitions.addAll(inner.transitions);

        // copy all transitions that went to the old final state
        // and make them go to the new final state
        transitions.addAll(
                inner.transitions
                        .stream()
                        .filter(transition -> transition.getToState() == oldFinal)
                        .map(transition -> transition.replaceToState(getFinalState()))
                        .collect(toList())
        );

        // copy all transitions that come from the start state and change their
        // source to the old final state
        transitions.addAll(
                inner.transitions
                        .stream()
                        .filter(transition -> transition.getFromState() == initState)
                        .map(transition -> transition.replaceFromState(oldFinal))
                        .collect(toList())
        );

        // when the inner automaton has only one transition, we need to add another case
        // as only the initial state would have a transition to the new final, and since
        // you cannot return to the initial it would make the kleene state a dead end
        if (inner.transitions.size() == 1) {
            transitions.addAll(
                    inner.transitions
                            .stream()
                            .map(transition -> transition.displaceTransition(1))
                            .collect(toList())
            );
        }

        // add white loop to oldFinal state
        transitions.add(new Transition(oldFinal, oldFinal, BitVector.getTrueBitVector(), Transition.TransitionType.WHITE));
    }
}
