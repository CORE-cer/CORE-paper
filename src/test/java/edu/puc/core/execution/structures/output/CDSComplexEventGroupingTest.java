package edu.puc.core.execution.structures.output;

import edu.puc.core.execution.structures.CDS.CDSNode;
import edu.puc.core.execution.structures.CDS.CDSOutputNode;
import edu.puc.core.execution.structures.CDS.CDSUnionNode;
import edu.puc.core.parser.plan.cea.Transition;
import edu.puc.core.runtime.events.Event;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.apache.commons.collections.IteratorUtils;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class CDSComplexEventGroupingTest {

    private static CDSComplexEventGrouping complexEventGrouping;

    private static final Event event0 = new Event(0);
    private static final Event event1 = new Event(1);
    private static final Event event2 = new Event(2);
    private static final Event event3 = new Event(3);

    private static ComplexEvent complexEvent1;
    private static ComplexEvent complexEvent2;
    private static ComplexEvent complexEvent3;

    /*
             3
             |
         ____V
     (l)/    |
       2  __ V
       | /  /(l)
       1  2'
       | /
       0
       |
     bottom
    */
    public static CDSNode getCDS() {
        CDSNode zero = new CDSOutputNode(CDSNode.BOTTOM, Transition.TransitionType.BLACK, event0);
        CDSNode one = new CDSOutputNode(zero, Transition.TransitionType.BLACK, event1);
        CDSNode two = new CDSOutputNode(one, Transition.TransitionType.BLACK, event2);
        CDSNode two2 = new CDSOutputNode(zero, Transition.TransitionType.BLACK, event2);
        CDSNode v1 = new CDSUnionNode(two2, one);
        CDSNode v2 = new CDSUnionNode(two, v1);
        CDSNode three = new CDSOutputNode(v2, Transition.TransitionType.BLACK, event3);

        return three;
    }

    @BeforeClass
    public static void setUp() {
        complexEventGrouping = new CDSComplexEventGrouping(event3, 0, Optional.empty());
        complexEventGrouping.addCDSNode(getCDS());

        complexEvent1 = new ComplexEvent();
        complexEvent1.push(event3, null);
        complexEvent1.push(event2, null);
        complexEvent1.push(event1, null);
        complexEvent1.push(event0, null);

        complexEvent2 = new ComplexEvent();
        complexEvent2.push(event3, null);
        complexEvent2.push(event2, null);
        complexEvent2.push(event0, null);

        complexEvent3 = new ComplexEvent();
        complexEvent3.push(event3, null);
        complexEvent3.push(event1, null);
        complexEvent3.push(event0, null);
    }

    @Test
    public void testIterator() {
        // Collecting all events in a list will not work (see CDSComplexEventGrouping.iterator())
        int i = 0;
        ComplexEvent[] expectedComplexEvents = {complexEvent1, complexEvent2, complexEvent3};
        for(ComplexEvent ce: complexEventGrouping) {
            assertEquals("ComplexEvent" + i, expectedComplexEvents[i], ce);
            ++i;
        }
        assertEquals("Number of complex events", 3, i);
    }
}