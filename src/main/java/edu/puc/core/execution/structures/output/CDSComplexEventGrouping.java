package edu.puc.core.execution.structures.output;


import edu.puc.core.execution.structures.CDS.CDSNode;
import edu.puc.core.execution.structures.CDS.CDSOutputNode;
import edu.puc.core.execution.structures.CDS.CDSUnionNode;
import edu.puc.core.runtime.events.Event;
import edu.puc.core.util.DistributionConfiguration;
import org.antlr.v4.runtime.misc.Pair;
import org.antlr.v4.runtime.misc.Triple;

import java.util.*;

// This is basically an iterator from final state nodes.
// The iterator will give you a list of Complex Events using the enumeration Algorithm 2.
public class CDSComplexEventGrouping implements Iterable<ComplexEvent> {
    private long totalMatches;
    private Event lastEvent;
    private long limit;
    private List<CDSNode> CDSNodes;
    private Optional<DistributionConfiguration> distributionConfiguration;

    public CDSComplexEventGrouping(Event currentEvent, long limit, Optional<DistributionConfiguration> distributionConfiguration){
        this.lastEvent = currentEvent;
        this.limit = limit;
        this.distributionConfiguration = distributionConfiguration;
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

    // 1) This iterator will modify the complex events after each iteration.
    //    Be careful, if you try to collect them in a stream, each CE will change as soon as you call next().
    //    In order to avoid this, you need to do a lot of copies so we decided not to do that change.
    // 2) The distributed enumeration may return 'null' between different CDS.
    //    Just skip the null and the next 'next()' will return a proper complex event if there are remaining CDS.
    public Iterator<ComplexEvent> iterator(){
        return new Iterator<>() {
            final Iterator<CDSNode> CDSNodeIterator = CDSNodes.iterator();
            CDSNode current = CDSNodeIterator.next();
            final Stack<Triple<CDSNode, ComplexEventNode, Pair<Integer, Integer>>> stack = new Stack<>();
            ComplexEvent complexEvent = new ComplexEvent();
            final Pair<Integer, Integer> enumerationParameters = getEnumerationParameters(current.getPaths());
            int a1 = enumerationParameters.a;
            int s1 = enumerationParameters.b;

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
                    final Pair<Integer, Integer> enumerationParameters = getEnumerationParameters(current.getPaths());
                    a1 = enumerationParameters.a;
                    s1 = enumerationParameters.b;
                }
                while (true) {
                    if (current.isBottom()) {
                        Triple<CDSNode, ComplexEventNode, Pair<Integer, Integer>> triplet = stack.pop();
                        current = triplet.a;
                        complexEvent.popUntil2(triplet.b);
                        s1 = triplet.c.a;
                        a1 = triplet.c.b;
                    } else if (current instanceof CDSOutputNode) {
                        CDSOutputNode temp = (CDSOutputNode) current;
                        if (temp.getTransitionType().isBlack()) {
                            complexEvent.push(temp.getEvent(), temp.getTransitionType());
                        } else {
                            throw new RuntimeException("Does this ever happen? Yes! Check this else-case.");
//                            Event toAdd = new Event(temp.getEvent().getIndex()); // Everything to null except the event index
//                            complexEvent.push(toAdd, temp.getTransitionType());
                        }
                        current = temp.getChild();
                        if (current.isBottom()) {
                            return complexEvent;
                        }
                    } else if (current instanceof CDSUnionNode) {
                        CDSUnionNode temp = (CDSUnionNode) current;
                        CDSNode left = temp.getLeft();
                        CDSNode right = temp.getRight();

                        // Right branch
                        {
                            int a2 = (left.getPaths() > s1) ? (a1 - Math.max(0, left.getPaths() - s1)) : a1;
                            int s2 = Math.max(0, s1 - left.getPaths());
                            if (right.getPaths() > s2 && a2 > 0) {
                                stack.push(new Triple<>(right, complexEvent.getHead(), new Pair<>(s2, a2)));
                            }
                        }

                        // Left branch
                        if (left.getPaths() > s1) {
                            current = left;
                        } else {
                            if (!stack.isEmpty()) {
                                // Workaround to force the stack case (first 'if')
                                current = CDSNode.BOTTOM;
                            } else {
                                // Nothing left to do in this CDS.
                                return null;
                            }
                        }
                    }
                }
            }

            // Returns a Pair<a, s> corresponding to the parameters from the paper.
            private Pair<Integer, Integer> getEnumerationParameters(int paths) {
                DistributionConfiguration tmp = distributionConfiguration.orElse(DistributionConfiguration.DEFAULT);
                int a = (int) Math.ceil((double) paths / (double) tmp.processes);
                int s = a * tmp.process;
                return new Pair<>(a, s);
            }
        };
    }
}