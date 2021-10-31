package edu.puc.core.parser.visitors;

import edu.puc.core.parser.BaseParser;
import edu.puc.core.parser.COREParser;
import edu.puc.core.parser.exceptions.DuplicateNameException;
import edu.puc.core.parser.exceptions.MissingValueException;
import edu.puc.core.parser.exceptions.UnknownNameException;
import edu.puc.core.parser.plan.Event;
import edu.puc.core.parser.plan.Stream;
import edu.puc.core.parser.plan.exceptions.EventException;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class StreamDeclarationVisitorTest {
    private static BaseParser<COREParser.Stream_declarationContext, ParserRuleContext> parser;
    private static StreamDeclarationVisitor visitor;
    private static Collection<Event> events;

    @BeforeClass
    public static void createParserAndVisitor() throws EventException {
        parser = new BaseParser<COREParser.Stream_declarationContext, ParserRuleContext>() {
            @Override
            public COREParser.Stream_declarationContext parse(String source) throws ParseCancellationException {
                return getParserForSource(source).stream_declaration();
            }
        };
        visitor = new StreamDeclarationVisitor();
        events = java.util.stream.Stream.of(new Event("E1"), new Event("E2")).collect(Collectors.toList());
    }

    @Test
    public void AllEventsStream() {
        COREParser.Stream_declarationContext context = parser.parse("DECLARE STREAM AllEvents(E1, E2)");
        Stream s = visitor.visitStream_declaration(context);

        String expectedName = "AllEvents";
        Collection<Event> expectedEvents = events;

        assertEquals("Name is parsed correctly", expectedName, s.getName());
        assertEquals("Events are parsed correctly", expectedEvents, new ArrayList<>(s.getEvents()));
    }

    @Test
    public void NoEventStream() {
        COREParser.Stream_declarationContext context = parser.parse("DECLARE STREAM NoEvents()");
        try{
            visitor.visitStream_declaration(context);
        }
        catch(MissingValueException exc){
            if (exc.getMessage().contains("No events declared")) return;
        }
        fail("Stream with no events is declared");
    }

    @Test
    public void doubleStreamDeclaration() {
        COREParser.Stream_declarationContext context = parser.parse("DECLARE Stream DoubleDeclaration(E1)");
        visitor.visitStream_declaration(context);
        try {
            visitor.visitStream_declaration(context);
        }
        catch (DuplicateNameException exc){
            if (exc.getMessage().contains("`DoubleDeclaration`")) return;
        }
        fail("Stream is declared twice");
    }

    @Test
    public void doubleEventDeclaration() {
        COREParser.Stream_declarationContext context = parser.parse("DECLARE Stream DoubleEvents(E1, E1)");
        try {
            visitor.visitStream_declaration(context);
        }
        catch (DuplicateNameException exc){
            if (exc.getMessage().contains("`E1`")) return;
        }
        fail("Event is declared twice");
    }

    @Test
    public void unknownEventDeclared() {
        COREParser.Stream_declarationContext context = parser.parse("DECLARE Stream FailStream(WrongEvent)");
        try {
            visitor.visitStream_declaration(context);
        }
        catch (UnknownNameException exc){
            if (exc.getMessage().contains("`WrongEvent`")) return;
        }
        fail("Unknown event is accepted on stream declaration");
    }

}