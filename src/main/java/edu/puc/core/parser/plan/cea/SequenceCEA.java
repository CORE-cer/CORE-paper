package edu.puc.core.parser.plan.cea;

import edu.puc.core.parser.plan.Event;

import java.util.stream.Collectors;

/**
 * Sequence {@link CEA} that supports sequencing between {@link Event}s.
 */
public class SequenceCEA extends CEA {
    public SequenceCEA(CEA first, CEA second) {
        stateCount = first.stateCount + second.stateCount - 1;

        // add all first ExecutableCEA transitions
        transitions.addAll(first.transitions);

        // displace the second ExecutableCEA transitions and add them
        int toDisplace = first.stateCount - 1;

        transitions.addAll(
                second.transitions
                        .stream()
                        .map(transition -> transition.displaceTransition(toDisplace))
                        .collect(Collectors.toList())
        );

        // add all the corresponding labels
        labelSet.addAll(first.labelSet);
        labelSet.addAll(second.labelSet);

        int firstFinal = first.getFinalState();
        transitions.add(new Transition(firstFinal, firstFinal, BitVector.getTrueBitVector(), Transition.TransitionType.WHITE));
    }
}
