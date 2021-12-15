package edu.puc.core.execution.structures.output;


import edu.puc.core.execution.structures.CDS.CDSNode;
import edu.puc.core.execution.structures.CDS.CDSOutputNode;
import edu.puc.core.execution.structures.CDS.CDSUnionNode;
import edu.puc.core.runtime.events.Event;
import org.antlr.v4.runtime.misc.Pair;
import org.antlr.v4.runtime.misc.Triple;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

// This is basically an iterator from final state nodes.
// The iterator will give you a list of Complex Events using the enumeration Algorithm 2.
public class CDSComplexEventGrouping implements Iterable<ComplexEvent> {
    private long totalMatches;
    private Event lastEvent;
    private long limit;
    private List<CDSNode> CDSNodes;

    public CDSComplexEventGrouping(Event currentEvent, long limit){
        this.lastEvent = currentEvent;
        this.limit = limit;
        totalMatches = 0;
        CDSNodes = new ArrayList<>();
    }

    CDSComplexEventGrouping() {}

    public void addCDSNode(CDSNode rootNode) {
        CDSNodes.add(rootNode);
    }

    public long size(){
        return totalMatches;
    }

    public Event getLastEvent(){
        return lastEvent;
    }

    // TODO
    // This iterator will modify the complex events after each iteration.
    // Be careful, if you try to collect them in a stream, each CE will change as soon as you call next().
    // In order to avoid this, you need to do a lot of copies.
    public Iterator<ComplexEvent> iterator(){
        return new Iterator<>() {

            final Iterator<CDSNode> CDSNodeIterator = CDSNodes.iterator();
            CDSNode current = CDSNodeIterator.next();
            final Stack<Triple<CDSNode, ComplexEventNode, Pair<Integer, Integer>>> stack = new Stack<>();
            ComplexEvent complexEvent = new ComplexEvent();
            // TODO add as parameter
            int remaining = 2;
            int start = 0;

            @Override
            public boolean hasNext() {
                if (!current.isBottom()) {
                    return true;
                }
                if (!stack.isEmpty()) {
                    return true;
                }
                return CDSNodeIterator.hasNext();
            }

            @Override
            public ComplexEvent next() {
                if (current.isBottom() && stack.isEmpty()) {
                    complexEvent = new ComplexEvent();
                    current = CDSNodeIterator.next();
                }
                while (true) {
                    if (current.isBottom()) {
                        Triple<CDSNode, ComplexEventNode, Pair<Integer, Integer>> triplet = stack.pop();
                        current = triplet.a;
                        complexEvent.popUntil2(triplet.b);
                    } else if (current instanceof CDSOutputNode) {
                        CDSOutputNode temp = (CDSOutputNode) current;
                        if (temp.getTransitionType().isBlack()) {
                            complexEvent.push(temp.getEvent(), temp.getTransitionType());
                        } else {
                            // TODO Does this ever happens?
                            System.out.println("CDSComplexEventGrouping.java white transition.");
                            Event toAdd = new Event(temp.getEvent().getIndex()); // Everything to null except the event index
                            complexEvent.push(toAdd, temp.getTransitionType());
                        }
                        current = temp.getChild();
                        if (current.isBottom()) {
                            return complexEvent;
                        }
                    } else if (current instanceof CDSUnionNode) {
                        CDSUnionNode temp = (CDSUnionNode) current;
                        stack.push(new Triple<>(temp.getRight(), complexEvent.getHead(), null));
                        current = temp.getLeft();
                    }
                }
            }
        };
    }
}