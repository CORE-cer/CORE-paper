package edu.puc.core.execution.structures.CDS;

import edu.puc.core.parser.plan.cea.Transition;
import edu.puc.core.runtime.events.Event;

public class CDSOutputNode extends CDSNode {

    public static final CDSNode BOTTOM = new CDSOutputNode();

    private Event event;
    private Transition.TransitionType transitionType;
    private CDSNode child;

    private CDSOutputNode() {}

    public CDSOutputNode(CDSNode child, Transition.TransitionType transitionType, Event event) {
        this.child = child;
        this.transitionType = transitionType;
        this.event = event;
    }

    public Event getEvent() {
        return event;
    }

    public Transition.TransitionType getTransitionType() {
        return transitionType;
    }

    public boolean isBottom() {
        return this == BOTTOM;
    }

    public CDSNode getChild() {
        return child;
    }
}
