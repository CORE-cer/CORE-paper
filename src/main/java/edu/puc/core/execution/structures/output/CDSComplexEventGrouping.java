package edu.puc.core.execution.structures.output;


import edu.puc.core.execution.structures.CDS.CDSNode;
import edu.puc.core.execution.structures.CDS.CDSOutputNode;
import edu.puc.core.execution.structures.CDS.CDSUnionNode;
import edu.puc.core.runtime.events.Event;

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

    public Iterator<ComplexEvent> iterator(){
        return new Iterator<ComplexEvent>() {

            final Iterator<CDSNode> CDSNodeIterator = CDSNodes.iterator();
            CDSNode current = CDSNodeIterator.next();

            final Stack<CDSUnionNode> DFSStack = new Stack<>();
            final Stack<ComplexEventNode> jumpStack = new Stack<>();

            ComplexEvent complexEvent = new ComplexEvent();
            ComplexEventNode lastEvent;
            boolean unionNodeLast = false;
            boolean unionNodeLast2 = false;

            @Override
            public boolean hasNext() {
                if (!current.isBottom()) {
                    return true;
                }
                if (!DFSStack.isEmpty()) {
                    return true;
                }
                return CDSNodeIterator.hasNext();
            }

            // This is the same enumeration algorithm from CORE paper (without time windows).
            // But the implementation is not the same as the one from the paper (it can be translated though).
            // It is weird how they implemented this... there must be a reason.
            @Override
            public ComplexEvent next() {
                // We always start from a bottom except from the first call to next().
                // DFSStack contains the union node where we picked the left path, and now we need to pick the right path.
                //
                // This is the case where we need to start enumerating from another final state node.
                if (current.isBottom() && DFSStack.isEmpty()) {
                    complexEvent = new ComplexEvent();
                    current = CDSNodeIterator.next();
                }
                while (true) {
                    if (current.isBottom()) { // Restart after output node found a next(n) = bottom.
                        // DFSStack contains a union node for the right choice.
                        current = DFSStack.pop().getRight();
                        // jumpStack contains the complex event in the previous state.
                        // Surprisingly, they do not push to the jump on the union case but on the output case.
                        ComplexEventNode temp = jumpStack.pop();
                        // This actually calls 'head = node.getNext();'
                        // As you see on line 93 and 96, they are pushing lastEvent AFTER adding an output node
                        // this forces them to call node.getNext(); to pop it...
                        complexEvent.popUntil(temp);
                    } else if (current instanceof CDSOutputNode) {
                        CDSOutputNode temp = (CDSOutputNode) current;
                        if (temp.getTransitionType().isBlack()) {
                            lastEvent = complexEvent.push(temp.getEvent(), temp.getTransitionType());
                        } else {
                            // TODO Does this ever happens?
                            System.out.println("CDSComplexEventGrouping.java white transition.");
                            Event toAdd = new Event(temp.getEvent().getIndex()); // Everything to null except the event index
                            lastEvent = complexEvent.push(toAdd, temp.getTransitionType());
                        }
                        if (unionNodeLast) {
                            jumpStack.push(lastEvent);
                            unionNodeLast = false;
                        }
                        if (unionNodeLast2) {
                            jumpStack.push(lastEvent);
                            unionNodeLast2 = false;
                        }
                        current = temp.getChild();
                        if (current.isBottom()) {
                            return complexEvent;
                        }
                    } else if (current instanceof CDSUnionNode) {
                        if (unionNodeLast) {
                            unionNodeLast2 = true;
                        }
                        unionNodeLast = true;
                        CDSUnionNode temp = (CDSUnionNode) current;
                        DFSStack.push(temp);
                        current = temp.getLeft();
                    }
                }
            }
        };
    }
}