package edu.puc.core.parser.plan.cea;

import static java.util.stream.Collectors.toList;

/**
 * Or {@link CEA} that supports the logical OR between filters.
 */
public class OrCEA extends CEA {

    public OrCEA(CEA left, CEA right){
        int displaceAmount = left.stateCount;
        stateCount = displaceAmount + right.stateCount + 1;

        // copy all transitions from left ExecutableCEA that dont go to a final state
        transitions.addAll(
                left.transitions
                        .stream()
                        .filter(transition -> transition.getToState() != left.stateCount - 1)
                        .map(Transition::copy)
                        .collect(toList())
        );

        // copy all transitions from left ExecutableCEA that go to a final state
        transitions.addAll(
                left.transitions
                        .stream()
                        .filter(transition -> transition.getToState() == left.stateCount - 1)
                        .map(transition -> transition.replaceToState(getFinalState()))
                        .collect(toList())
        );

        // copy all transitions from right ExecutableCEA that don't come from an initial state or go to a final state
        transitions.addAll(
                right.transitions
                        .stream()
                        .filter(transition -> transition.getFromState() != right.initState)
                        .filter(transition -> transition.getToState() != right.stateCount - 1)
                        .map(transition -> transition.displaceTransition(displaceAmount))
                        .collect(toList())
        );

        // copy all transitions from right ExecutableCEA that come from an initial state
        transitions.addAll(
                right.transitions
                        .stream()
                        .filter(transition -> transition.getFromState() == right.initState)
                        .map(transition -> transition.displaceTransition(displaceAmount))
                        .map(transition -> transition.replaceFromState(left.initState))
                        .collect(toList())
        );


        // copy all transitions from right ExecutableCEA that go to a final state
        transitions.addAll(
                right.transitions
                        .stream()
                        .filter(transition -> transition.getToState() == right.stateCount - 1)
                        .map(transition -> transition.displaceTransition(displaceAmount))
                        .map(transition -> transition.replaceToState(getFinalState()))
                        .collect(toList())
        );


        // copy all transitions from right ExecutableCEA that come from an initial state and go to a final state
        transitions.addAll(
                right.transitions
                        .stream()
                        .filter(transition -> transition.getToState() == right.stateCount - 1)
                        .filter(transition -> transition.getFromState() == right.initState)
                        .map(transition -> transition.replaceToState(getFinalState()))
                        .map(transition -> transition.replaceFromState(initState))
                        .collect(toList())
        );

        // add all the corresponding labels
        labelSet.addAll(left.labelSet);
        labelSet.addAll(right.labelSet);
    }
}
