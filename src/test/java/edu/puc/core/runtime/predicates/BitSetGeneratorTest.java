package edu.puc.core.runtime.predicates;

import edu.puc.core.parser.QueryParser;
import edu.puc.core.parser.plan.Event;
import edu.puc.core.parser.plan.Stream;
import edu.puc.core.parser.plan.cea.PredicateFactory;
import edu.puc.core.parser.plan.exceptions.EventException;
import edu.puc.core.parser.plan.exceptions.StreamException;
import edu.puc.core.parser.plan.values.ValueType;
import javafx.util.Pair;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class BitSetGeneratorTest {

    private static QueryParser parser;

    @BeforeClass
    public static void initChecker() throws EventException, StreamException{
        parser = new QueryParser();
        Event T = new Event("Temp1", new ArrayList<Pair<String, ValueType>>() {{
            add(new Pair<>("id", ValueType.LONG));
            add(new Pair<>("temp", ValueType.DOUBLE));
        }});
        Event T2 = new Event("Temp2", new ArrayList<Pair<String, ValueType>>() {{
            add(new Pair<>("id", ValueType.LONG));
        }});
        Event H = new Event("Hum1", new ArrayList<Pair<String, ValueType>>() {{
            add(new Pair<>("id", ValueType.LONG));
            add(new Pair<>("income", ValueType.DOUBLE));
            add(new Pair<>("cost", ValueType.DOUBLE));
        }});
        new Stream("SS1", java.util.stream.Stream.of(T, H).collect(Collectors.toSet()));
        new Stream("SS2", java.util.stream.Stream.of(T2, H).collect(Collectors.toSet()));
        parser.parse(
                "SELECT MAX *\n" +
                        "FROM SS1, SS2\n" +
                        "WHERE ( SS1>Temp1; Hum1 + ; Temp2 AS t2 ) AS all_events\n" +
                        "--WHERE H+ OR H+\n" +
                        "FILTER\n" +
                        "    all_events[id NOT IN { 123, 125 }]\n" +
                        "    AND\n" +
                        "    ( t2[temp > 50 or temp < 20] OR Hum1[income > cost] )\n" +
                        "PARTITION BY\n" +
                        "    [id]\n" +
                        "WITHIN 10 hours 30 minutes\n" +
                        "CONSUME BY PARTITION"
        );
    }

    @Test
    public void testChecker() throws edu.puc.core.exceptions.EventException {
        BitSetGenerator checker = new BitSetGenerator(PredicateFactory.getInstance());
        BitSet vector = checker.getBitSetFromEvent(new edu.puc.core.runtime.events.Event("SS1", "Temp1", 1L, 42.2));
        PredicateFactory factory = PredicateFactory.getInstance();
        assertTrue("Hey!", true);
    }
}