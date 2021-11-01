package edu.puc.core.execution.structures.output;

import edu.puc.core.parser.plan.cea.Transition;
import edu.puc.core.runtime.events.Event;

import java.util.Iterator;

public class ComplexEvent implements Iterable<Event> {
    private ComplexEventNode head;
    private long end = -1;
    private long start = -1;

    ComplexEventNode push(Event event, Transition.TransitionType transitionType) {
        start = event.getIndex();

        if (end == -1) {
            end = event.getIndex();
        }

        if (head != null){
            head = new ComplexEventNode(event, head, transitionType);
        }
        else {
            head = new ComplexEventNode(event, transitionType);
        }
        return head;
    }

    void popUntil(ComplexEventNode node) {
        head = node.getNext();
    }

    public Iterator<Event> iterator(){
        return new Iterator<Event>() {
            ComplexEventNode current = head;

            @Override
            public boolean hasNext() {
                return current != null;
            }

            @Override
            public Event next() {
                Event event = current.getEvent();
                current = current.getNext();
                return event;
            }
        };
    }

    public long size(){
        return head != null ? head.getIndex() : 0;
    }

    public boolean isEmpty() {
        return head == null;
    }

    public long getStart() {
        return start;
    }

    public void setStart(long newStart) {
        start = newStart;
    }

    public long getEnd() {
        return end;
    }
}
