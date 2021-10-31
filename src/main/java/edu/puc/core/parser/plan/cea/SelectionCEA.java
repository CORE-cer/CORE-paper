package edu.puc.core.parser.plan.cea;

import edu.puc.core.parser.plan.Event;
import edu.puc.core.parser.plan.Label;
import edu.puc.core.parser.plan.Stream;

/**
 * Selection {@link CEA} that passes every {@link Event} until one matches the given
 * predicate (or the given {@link Event}'s predicate).
 */
public class SelectionCEA extends CEA {
    public SelectionCEA(Event event) {
        this(PredicateFactory.getInstance().from(event), event.getNameLabel());
    }

    public SelectionCEA(Stream stream, Event event) {
        this(PredicateFactory.getInstance().from(stream, event), event.getNameLabel());
    }

    private SelectionCEA(BitVector bitVector, Label label) {
        super(2);

        transitions.add(new Transition(0, 1, bitVector, label, Transition.TransitionType.BLACK));

        labelSet.add(label);
    }
}
