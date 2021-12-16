package edu.puc.core.execution.structures.output;

import edu.puc.core.execution.structures.CDS.CDSNode;
import edu.puc.core.execution.structures.CDS.CDSOutputNode;
import edu.puc.core.execution.structures.CDS.CDSUnionNode;
import edu.puc.core.parser.plan.cea.Transition;
import edu.puc.core.runtime.events.Event;
import edu.puc.core.util.DistributionConfiguration;
import org.antlr.v4.runtime.misc.Pair;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CDSComplexEventGroupingTest {

    private static final Event event0 = new Event(0);
    private static final Event event1 = new Event(1);
    private static final Event event2 = new Event(2);
    private static final Event event3 = new Event(3);
    private static final Event event4 = new Event(4);

    @BeforeClass
    public static void setUp() {}

    /*
             3
             |
         ___ V2
     (l)/    |
       2  __ V1
       | /  /(l)
       1  2'
       | /
       0
       |
     bottom
    */
    public static Pair<CDSNode, List<ComplexEvent>> getCDS1() {
        CDSNode zero = new CDSOutputNode(CDSNode.BOTTOM, Transition.TransitionType.BLACK, event0);
        CDSNode one = new CDSOutputNode(zero, Transition.TransitionType.BLACK, event1);
        CDSNode two = new CDSOutputNode(one, Transition.TransitionType.BLACK, event2);
        CDSNode two2 = new CDSOutputNode(zero, Transition.TransitionType.BLACK, event2);
        CDSNode v1 = new CDSUnionNode(two2, one);
        CDSNode v2 = new CDSUnionNode(two, v1);
        CDSNode three = new CDSOutputNode(v2, Transition.TransitionType.BLACK, event3);

        ComplexEvent complexEvent1 = new ComplexEvent();
        complexEvent1.push(event3, null);
        complexEvent1.push(event2, null);
        complexEvent1.push(event1, null);
        complexEvent1.push(event0, null);

        ComplexEvent complexEvent2 = new ComplexEvent();
        complexEvent2.push(event3, null);
        complexEvent2.push(event2, null);
        complexEvent2.push(event0, null);

        ComplexEvent complexEvent3 = new ComplexEvent();
        complexEvent3.push(event3, null);
        complexEvent3.push(event1, null);
        complexEvent3.push(event0, null);

        return new Pair<>(three, List.of(complexEvent1, complexEvent2, complexEvent3));
    }

    @Test
    public void testIterator() {
        Pair<CDSNode, List<ComplexEvent>> pair = getCDS1();
        List<ComplexEvent> expectedComplexEvents = pair.b;
        CDSComplexEventGrouping complexEventGrouping = new CDSComplexEventGrouping(event3, 0, Optional.empty());
        complexEventGrouping.addCDSNode(pair.a);
        int i = 0;
        for(ComplexEvent ce: complexEventGrouping) {
            assertEquals("ComplexEvent" + i, expectedComplexEvents.get(i), ce);
            ++i;
        }
        assertEquals("Number of complex events", 3, i);
    }

    @Test
    public void testDistributedIterator() {
        int processes = 2;
        int i = 0;
        Pair<CDSNode, List<ComplexEvent>> pair = getCDS1();
        List<ComplexEvent> expectedComplexEvents = pair.b;

        // Process 0
        CDSComplexEventGrouping complexEventGrouping = new CDSComplexEventGrouping(event3, 0, Optional.of(new DistributionConfiguration(0, processes)));
        complexEventGrouping.addCDSNode(pair.a);
        for(ComplexEvent ce: complexEventGrouping) {
            assertEquals("ComplexEvent" + i, expectedComplexEvents.get(i), ce);
            ++i;
        }
        assertEquals("Number of complex events", 2, i);

        // Process 1
        CDSComplexEventGrouping complexEventGrouping2 = new CDSComplexEventGrouping(event3, 0, Optional.of(new DistributionConfiguration(1, processes)));
        complexEventGrouping2.addCDSNode(pair.a);
        for(ComplexEvent ce: complexEventGrouping2) {
            assertEquals("ComplexEvent" + i, expectedComplexEvents.get(i), ce);
            ++i;
        }
        assertEquals("Number of complex events", 3, i);
    }

    /*
                       4
                       |
                       V4
                 (l) / | (l)
                3 - V3 |
                | /    |
              _ V2    /
        (l) /   /     |
       2 - V1  /(l)   |
       | /    /      /
       1 __ 2'   __ 3'    4'
       |/ ______/        /
       0/______________ /
       |
     bottom
    */
    public static Pair<List<CDSNode>, List<ComplexEvent>> getCDS2() {
        CDSNode zero = new CDSOutputNode(CDSNode.BOTTOM, Transition.TransitionType.BLACK, event0);
        CDSNode one = new CDSOutputNode(zero, Transition.TransitionType.BLACK, event1);
        CDSNode two = new CDSOutputNode(one, Transition.TransitionType.BLACK, event2);
        CDSNode two2 = new CDSOutputNode(zero, Transition.TransitionType.BLACK, event2);
        CDSNode v1 = new CDSUnionNode(two, one);
        CDSNode v2 = new CDSUnionNode(two2, v1);
        CDSNode three = new CDSOutputNode(v2, Transition.TransitionType.BLACK, event3);
        CDSNode three2 = new CDSOutputNode(zero, Transition.TransitionType.BLACK, event3);
        CDSNode v3 = new CDSUnionNode(three, v2);
        CDSNode v4 = new CDSUnionNode(three2, v3);
        CDSNode four = new CDSOutputNode(v4, Transition.TransitionType.BLACK, event4);
        CDSNode four2 = new CDSOutputNode(zero, Transition.TransitionType.BLACK, event4);

        ComplexEvent complexEvent1 = new ComplexEvent();
        complexEvent1.push(event4, null);
        complexEvent1.push(event3, null);
        complexEvent1.push(event0, null);

        ComplexEvent complexEvent2 = new ComplexEvent();
        complexEvent2.push(event4, null);
        complexEvent2.push(event3, null);
        complexEvent2.push(event2, null);
        complexEvent2.push(event0, null);

        ComplexEvent complexEvent3 = new ComplexEvent();
        complexEvent3.push(event4, null);
        complexEvent3.push(event3, null);
        complexEvent3.push(event2, null);
        complexEvent3.push(event1, null);
        complexEvent3.push(event0, null);

        ComplexEvent complexEvent4 = new ComplexEvent();
        complexEvent4.push(event4, null);
        complexEvent4.push(event3, null);
        complexEvent4.push(event1, null);
        complexEvent4.push(event0, null);

        ComplexEvent complexEvent5 = new ComplexEvent();
        complexEvent5.push(event4, null);
        complexEvent5.push(event2, null);
        complexEvent5.push(event0, null);

        ComplexEvent complexEvent6 = new ComplexEvent();
        complexEvent6.push(event4, null);
        complexEvent6.push(event2, null);
        complexEvent6.push(event1, null);
        complexEvent6.push(event0, null);

        ComplexEvent complexEvent7 = new ComplexEvent();
        complexEvent7.push(event4, null);
        complexEvent7.push(event1, null);
        complexEvent7.push(event0, null);

        ComplexEvent complexEvent8 = new ComplexEvent();
        complexEvent8.push(event4, null);
        complexEvent8.push(event0, null);

        return new Pair<>(
                List.of(four, four2),
                List.of(complexEvent1, complexEvent2, complexEvent3, complexEvent4,
                        complexEvent5, complexEvent6, complexEvent7, complexEvent8)
        );
    }


    @Test
    public void testIteratorMultipleFinalStates() {
        Pair<List<CDSNode>, List<ComplexEvent>> pair = getCDS2();
        List<ComplexEvent> expectedComplexEvents = pair.b;
        CDSComplexEventGrouping complexEventGrouping = new CDSComplexEventGrouping(event4, 0, Optional.empty());
        for(CDSNode cdsRoot : pair.a) {
            complexEventGrouping.addCDSNode(cdsRoot);
        }
        int i = 0;
        for(ComplexEvent ce: complexEventGrouping) {
            assertEquals("ComplexEvent" + i, expectedComplexEvents.get(i), ce);
            ++i;
        }
        assertEquals("Number of complex events", 8, i);
    }

    private List<Integer> zeroToN(int n) {
        List<Integer> r = new ArrayList<>(n);
        int i = 0;
        while (i < n) {
            r.add(i);
            i++;
        }
        return r;
    }

    @Test
    public void testDistributedIteratorMultipleFinalStates() {
        int processes = 3;
        Pair<List<CDSNode>, List<ComplexEvent>> pair = getCDS2();
        List<ComplexEvent> expectedComplexEvents = List.of(
                // Process 0 (notice that process 0 also process 2nd CDS complex events)
                pair.b.get(0),
                pair.b.get(1),
                pair.b.get(2),
                pair.b.get(7),
                // Process 1
                pair.b.get(3),
                pair.b.get(4),
                pair.b.get(5),
                // Process 2
                pair.b.get(6)
        );
        int[] expectedAmountByProcess = {4, 7, 8};

        int i = 0;
        for(int process : zeroToN(processes)) {
            CDSComplexEventGrouping complexEventGrouping = new CDSComplexEventGrouping(event4, 0, Optional.of(new DistributionConfiguration(process, processes)));
            for(CDSNode cdsRoot : pair.a) {
                complexEventGrouping.addCDSNode(cdsRoot);
            }
            for(ComplexEvent ce: complexEventGrouping) {
                if (ce != null) {
                    assertEquals("ComplexEvent" + i, expectedComplexEvents.get(i), ce);
                    ++i;
                }
            }
            assertEquals("Number of complex events", expectedAmountByProcess[process], i);
        }
    }

    /*
       2
       |
       1
       |
       0
       |
     bottom
    */
    public static Pair<CDSNode, ComplexEvent> getCDS3() {
        CDSNode zero = new CDSOutputNode(CDSNode.BOTTOM, Transition.TransitionType.BLACK, event0);
        CDSNode one = new CDSOutputNode(zero, Transition.TransitionType.BLACK, event1);
        CDSNode two = new CDSOutputNode(one, Transition.TransitionType.BLACK, event2);

        ComplexEvent complexEvent = new ComplexEvent();
        complexEvent.push(event2, null);
        complexEvent.push(event1, null);
        complexEvent.push(event0, null);

        return new Pair<>(two, complexEvent);
    }

    // Corner case: when the complex event doesn't have union nodes
    @Test
    public void testDistributedIteratorCornerCase() {
        int processes = 2;
        int i = 0;
        Pair<CDSNode, ComplexEvent> pair = getCDS3();
        ComplexEvent expectedComplexEvent = pair.b;

        // Process 0
        CDSComplexEventGrouping complexEventGrouping = new CDSComplexEventGrouping(event2, 0, Optional.of(new DistributionConfiguration(0, processes)));
        complexEventGrouping.addCDSNode(pair.a);
        for(ComplexEvent ce: complexEventGrouping) {
            assertEquals("ComplexEvent", expectedComplexEvent, ce);
            i++;
        }
        assertEquals("Number of complex events", 1, i);

        // Process 1
        CDSComplexEventGrouping complexEventGrouping2 = new CDSComplexEventGrouping(event3, 0, Optional.of(new DistributionConfiguration(1, processes)));
        complexEventGrouping2.addCDSNode(pair.a);
        for(ComplexEvent ce: complexEventGrouping2) {
            assertNull(ce);
            i++;
        }
        assertEquals("Number of complex events", 2, i);
    }
}