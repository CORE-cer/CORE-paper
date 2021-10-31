package edu.puc.core.parser.visitors;

import edu.puc.core.parser.BaseParser;
import edu.puc.core.parser.COREParser;
import edu.puc.core.parser.QueryParser;
import edu.puc.core.parser.plan.Event;
import edu.puc.core.parser.plan.Label;
import edu.puc.core.parser.plan.LogicalPlan;
import edu.puc.core.parser.plan.Stream;
import edu.puc.core.parser.plan.values.ValueType;
import edu.puc.core.parser.plan.exceptions.EventException;
import edu.puc.core.parser.plan.exceptions.NoSuchLabelException;
import edu.puc.core.parser.plan.exceptions.StreamException;
import edu.puc.core.parser.plan.cea.CEA;
import edu.puc.core.parser.plan.cea.Transition;
import edu.puc.core.parser.plan.cea.BitVector;
import edu.puc.core.parser.plan.cea.PredicateFactory;
import javafx.util.Pair;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class PatternVisitorTest {
    private static BaseParser<COREParser.Core_patternContext, ParserRuleContext> parser;
    private static PatternVisitor visitor;
    private static List<Stream> streams;
    private static List<Event> events;

    @BeforeClass
    public static void createParserAndVisitor() throws EventException, StreamException {
        parser = new BaseParser<COREParser.Core_patternContext, ParserRuleContext>() {
            @Override
            public COREParser.Core_patternContext parse(String source) throws ParseCancellationException {
                return getParserForSource(source).core_pattern();
            }
        };


        List<Pair<String, ValueType>> attrsA = new ArrayList<Pair<String, ValueType>>(){{
            add(new Pair<>("a_int", ValueType.INTEGER));
            add(new Pair<>("a_str", ValueType.STRING));
            add(new Pair<>("a_bool", ValueType.BOOLEAN));
            add(new Pair<>("double", ValueType.DOUBLE));
        }};

        List<Pair<String, ValueType>> attrsB = new ArrayList<Pair<String, ValueType>>(){{
            add(new Pair<>("b_int", ValueType.INTEGER));
            add(new Pair<>("b_str", ValueType.STRING));
            add(new Pair<>("b_bool", ValueType.BOOLEAN));
            add(new Pair<>("double", ValueType.DOUBLE));
        }};

        List<Pair<String, ValueType>> attrsC = new ArrayList<Pair<String, ValueType>>(){{
            add(new Pair<>("c_int", ValueType.INTEGER));
            add(new Pair<>("c_str", ValueType.STRING));
            add(new Pair<>("c_bool", ValueType.BOOLEAN));
            add(new Pair<>("double", ValueType.DOUBLE));
        }};

        List<Pair<String, ValueType>> attrsD = new ArrayList<Pair<String, ValueType>>(){{
            add(new Pair<>("d_int", ValueType.INTEGER));
            add(new Pair<>("d_str", ValueType.STRING));
            add(new Pair<>("d_bool", ValueType.BOOLEAN));
            add(new Pair<>("double", ValueType.DOUBLE));
        }};

        List<Pair<String, ValueType>> attrsE = new ArrayList<Pair<String, ValueType>>(){{
            add(new Pair<>("e_int", ValueType.INTEGER));
            add(new Pair<>("e_str", ValueType.STRING));
            add(new Pair<>("e_bool", ValueType.BOOLEAN));
            add(new Pair<>("double", ValueType.DOUBLE));
        }};

        events = java.util.stream.Stream.of(
                new Event("A", attrsA),
                new Event("B", attrsB),
                new Event("C", attrsC),
                new Event("D", attrsD),
                new Event("E", attrsE)
        ).collect(Collectors.toList());

        streams = java.util.stream.Stream.of(
                new Stream("S1", events.subList(0, 3)),
                new Stream("S2", events.subList(2, 4)),
                new Stream("S3", events)
        ).collect(Collectors.toList());
        visitor = new PatternVisitor(
                streams.stream().map(Stream::getName).collect(Collectors.toSet()),
                events.stream().map(Event::getName).collect(Collectors.toSet())
        );
    }


    @Test
    public void visitPar_core_pattern() {

    }

    @Test
    public void visitAssign_core_pattern() throws NoSuchLabelException {
        PredicateFactory.init(Stream.getAllStreams().values(), Event.getAllEvents().values(), 0);
        COREParser.Assign_core_patternContext context = (COREParser.Assign_core_patternContext) parser.parse("A AS my_a");
        CEA cea = visitor.visit(context);
        assertEquals("Correct number of states", 2, cea.getStateCount());
        assertEquals("Correct init state", 0, cea.getInitState());
        assertEquals("Correct final states", 1, cea.getFinalState());
        assertEquals("Correct transitions",
                java.util.stream.Stream.of(
                        new Transition(0, 1, PredicateFactory.getInstance().from(Event.getSchemaFor("A")), Transition.TransitionType.BLACK),
                        new Transition(0, 0, BitVector.getTrueBitVector(), Transition.TransitionType.WHITE)
                ).collect(Collectors.toSet()),
                new HashSet<>(cea.getTransitions()));
        Set<Label> labelSet = java.util.stream.Stream.of(Label.get("A"), Label.get("my_a")).collect(Collectors.toSet());
        cea.getTransitions().forEach(transition ->
            assertEquals("Correct labels", labelSet, transition.getLabels())
        );
    }

    @Test
    public void visitBinary_core_pattern() {
    }

    @Test
    public void visitKleene_core_pattern() {
    }

    @Test
    public void visitEvent_core_pattern() throws NoSuchLabelException {
        PredicateFactory.init(Stream.getAllStreams().values(), Event.getAllEvents().values(), 0);
        COREParser.Event_core_patternContext context = (COREParser.Event_core_patternContext) parser.parse("S1>A");
        CEA cea = visitor.visit(context);
        assertEquals("Correct number of states", 2, cea.getStateCount());
        assertEquals("Correct init state", 0, cea.getInitState());
        assertEquals("Correct final states", 1, cea.getFinalState());
        assertEquals("Correct transitions",
                java.util.stream.Stream.of(
                        new Transition(0, 1, PredicateFactory.getInstance().from(Stream.getSchemaFor("S1"), Event.getSchemaFor("A")), Transition.TransitionType.BLACK),
                        new Transition(0, 0, BitVector.getTrueBitVector(), Transition.TransitionType.WHITE)
                ).collect(Collectors.toSet()),
                new HashSet<>(cea.getTransitions()));
        Set<Label> labelSet = java.util.stream.Stream.of(Label.get("A")).collect(Collectors.toSet());
        cea.getTransitions().forEach(transition ->
                assertEquals("Correct labels", labelSet, transition.getLabels())
        );
    }

    @Test
    public void visitFilter_core_pattern() {
    }


    @Test
    public void simplePattern() throws EventException, StreamException{
        QueryParser parser = new QueryParser();
        LogicalPlan plan = parser.parse(
                "SELECT MAX *\n" +
                        "FROM S3\n" +
                        "WHERE ( A ; B ; C )"
        );

//        events = new edu.puc.core.runtime.events.Event[] {
//                new edu.puc.core.runtime.events.Event("S", "D"),
//                new edu.puc.core.runtime.events.Event("S", "A"),
//                new edu.puc.core.runtime.events.Event("S", "B"),
//                new edu.puc.core.runtime.events.Event("S", "C"),
//        };
    }
}