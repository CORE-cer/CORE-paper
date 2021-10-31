package edu.puc.core.execution.structures.output;


import edu.puc.core.execution.structures.CDS.CDSNode;
import edu.puc.core.execution.structures.CDS.CDSOutputNode;
import edu.puc.core.execution.structures.CDS.CDSUnionNode;
import edu.puc.core.runtime.events.Event;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

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

            @Override
            public ComplexEvent next() {
                if (current.isBottom() && DFSStack.isEmpty()) {
                    complexEvent = new ComplexEvent();
                    current = CDSNodeIterator.next();
                }
                while (true) {
                    if (current.isBottom()) {
                        current = DFSStack.pop().getRight();
                        ComplexEventNode temp = jumpStack.pop();
                        complexEvent.popUntil(temp);
                    } else if (current instanceof CDSOutputNode) {
                        CDSOutputNode temp = (CDSOutputNode) current;
                        if (temp.getTransitionType().isBlack()) {
                            lastEvent = complexEvent.push(temp.getEvent(), temp.getTransitionType());
                        } else {
                            Event toAdd = new Event(temp.getEvent().getIndex());
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