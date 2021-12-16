package edu.puc.core.execution.structures.output;

import edu.puc.core.execution.structures.CDS.time.CDSTimeBottomNode;
import edu.puc.core.execution.structures.CDS.time.CDSTimeNode;
import edu.puc.core.execution.structures.CDS.time.CDSTimeOutputNode;
import edu.puc.core.execution.structures.CDS.time.CDSTimeUnionNode;
import edu.puc.core.runtime.events.Event;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

public class CDSTimeComplexEventGrouping extends CDSComplexEventGrouping implements Iterable<ComplexEvent> {
    private long totalMatches;
    private Event lastEvent;
    private long limit;
    private long windowDelta;
    private List<CDSTimeNode> CDSTimeNodes;
    private long currentTime;

    public CDSTimeComplexEventGrouping(Event currentEvent, long limit, long windowDelta, long currentTime){
        super();
        this.lastEvent = currentEvent;
        this.limit = limit;
        this.windowDelta = windowDelta;
        this.currentTime = currentTime;
        totalMatches = 0;
        CDSTimeNodes = new ArrayList<>();
    }

    public CDSTimeComplexEventGrouping(Event currentEvent, long windowDelta, long currentTime){
        this(currentEvent, 0, windowDelta, currentTime);
    }

//    private CDSTimeComplexEventGrouping(CDSTimeNode rootNode){
//        CDSTimeNodes = new ArrayList<>();
//        CDSTimeNodes.add(rootNode);
//    }
//
//    private CDSTimeComplexEventGrouping(CDSTimeNode rootNode, long limit){
//        CDSTimeNodes = new ArrayList<>();
//        CDSTimeNodes.add(rootNode);
//        this.limit = limit;
//    }

    public void addCDSNode(CDSTimeNode rootNode) {
        CDSTimeNodes.add(rootNode);
    }

    public long size(){
        return totalMatches;
    }

    public Event getLastEvent(){
        return lastEvent;
    }

    public Iterator<ComplexEvent> iterator(){
        return new Iterator<ComplexEvent>() {

            Iterator<CDSTimeNode> CDSNodeIterator = CDSTimeNodes.iterator();
            CDSTimeNode current = CDSNodeIterator.next();
            Stack<CDSTimeUnionNode> DFSStack = new Stack<>();
            Stack<ComplexEventNode> jumpStack = new Stack<>();
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
                    if (current == null || current.isBottom()) {
                        if (DFSStack.isEmpty()) {
                            return null;
                        }
                        current = DFSStack.pop().getRight();
                        ComplexEventNode temp = jumpStack.pop();
                        complexEvent.popUntil(temp);
                    } else if (current instanceof CDSTimeOutputNode) {
                        CDSTimeOutputNode temp = (CDSTimeOutputNode) current;
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
                        if (current == null) {
                            current = CDSTimeBottomNode.BOTTOM;
                            continue;
                        }
                        if (current.isBottom() && currentTime - temp.getMm() < windowDelta) {
                            complexEvent.setStart(current.getMm());
                            return complexEvent;
                        }
                        if (currentTime - current.getMm() >= windowDelta) {
                            current = CDSTimeBottomNode.BOTTOM;
                        }
                    } else if (current instanceof CDSTimeUnionNode) {
                        if (currentTime - current.getMm() >= windowDelta) {
                            current = CDSTimeBottomNode.BOTTOM;
                            continue;
                        }
                        if (unionNodeLast) {
                            unionNodeLast2 = true;
                        }
                        unionNodeLast = true;
                        CDSTimeUnionNode temp = (CDSTimeUnionNode) current;
                        DFSStack.push(temp);
                        current = temp.getLeft();
                    }
                }
            }
        };
    }
}
