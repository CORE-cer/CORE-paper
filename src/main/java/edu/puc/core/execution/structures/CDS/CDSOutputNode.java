package edu.puc.core.execution.structures.CDS;

import edu.puc.core.parser.plan.cea.Transition;
import edu.puc.core.runtime.events.Event;

public class CDSOutputNode extends CDSNode {

    private final Event event;
    private final Transition.TransitionType transitionType;
    private final CDSNode child;

    // We could always compute paths from getPaths but this would need
    // to traverse the whole structure, and we want to do it in constant time.
    private final int paths;

    public CDSOutputNode(CDSNode child, Transition.TransitionType transitionType, Event event) {
        this.child = child;
        this.transitionType = transitionType;
        this.event = event;
        this.paths = child.getPaths();
    }

    public Event getEvent() {
        return event;
    }

    public Transition.TransitionType getTransitionType() {
        return transitionType;
    }

    @Override
    public boolean isBottom() {
        return false;
    }

    public CDSNode getChild() {
        return child;
    }

    @Override
    public int getPaths() {
        return this.paths;
    }
}
