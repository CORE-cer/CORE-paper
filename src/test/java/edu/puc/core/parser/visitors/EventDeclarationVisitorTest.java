package edu.puc.core.parser.visitors;

import edu.puc.core.parser.BaseParser;
import edu.puc.core.parser.COREParser;
import edu.puc.core.parser.exceptions.DuplicateNameException;
import edu.puc.core.parser.exceptions.UnknownDataTypeException;
import edu.puc.core.parser.plan.Event;
import edu.puc.core.parser.plan.values.ValueType;
import javafx.util.Pair;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class EventDeclarationVisitorTest {

    private static BaseParser<COREParser.Event_declarationContext, ParserRuleContext> parser;
    private static EventDeclarationVisitor visitor;

    @BeforeClass
    public static void createParserAndVisitor(){
        parser = new BaseParser<COREParser.Event_declarationContext, ParserRuleContext>() {
            @Override
            public COREParser.Event_declarationContext parse(String source) throws ParseCancellationException {
                return getParserForSource(source).event_declaration();
            }
        };
        visitor = new EventDeclarationVisitor();
    }

    @Test
    public void allValueTypeDeclaration() {
        COREParser.Event_declarationContext context = parser.parse("DECLARE EVENT AllValues(strVal string, intVal int, doubleVal double, longVal long, boolVal boolean)");
        Event e = visitor.visitEvent_declaration(context);

        String expectedName = "AllValues";
        List<Pair<String, ValueType>> expectedAttributes = new ArrayList<Pair<String, ValueType>>(){{
            add(new Pair<>("strVal", ValueType.STRING));
            add(new Pair<>("intVal", ValueType.INTEGER));
            add(new Pair<>("doubleVal", ValueType.DOUBLE));
            add(new Pair<>("longVal", ValueType.LONG));
            add(new Pair<>("boolVal", ValueType.BOOLEAN));
        }};

        assertEquals("Name should be parsed correctly", expectedName, e.getName());
        assertEquals("Attributes should be parsed correctly", expectedAttributes, e.getAttributes());
    }

    @Test
    public void noAttributeDeclaration() {
        COREParser.Event_declarationContext context = parser.parse("DECLARE EVENT NoValues()");
        Event e = visitor.visitEvent_declaration(context);

        String expectedName = "NoValues";

        assertEquals("Name should be parsed correctly", expectedName, e.getName());
        assertEquals("Attributes should be parsed correctly", 0, e.getAttributes().size());
    }

    @Test
    public void doubleEventDeclaration() {
        COREParser.Event_declarationContext context = parser.parse("DECLARE EVENT DoubleDeclaration()");
        visitor.visitEvent_declaration(context);
        try {
            visitor.visitEvent_declaration(context);
        }
        catch (DuplicateNameException exc){
            if (exc.getMessage().contains("`DoubleDeclaration`")) return;
        }
        fail("Event is declared twice");
    }

    @Test
    public void doubleAttributeDeclaration() {
        COREParser.Event_declarationContext context = parser.parse("DECLARE EVENT DoubleAttributes(attr int, attr string)");
        try {
            visitor.visitEvent_declaration(context);
        }
        catch (DuplicateNameException exc){
            if (exc.getMessage().contains("`attr`")) return;
        }
        fail("Attribute is declared twice");
    }

    @Test
    public void invalidDataTypeThrows() {
        COREParser.Event_declarationContext context = parser.parse("DECLARE EVENT InvalidDataType(notValid bool)");
        try {
            visitor.visitEvent_declaration(context);
        }
        catch (UnknownDataTypeException exc){
            if (exc.getMessage().contains("`bool`")) return;
        }
        fail("Invalid data type throws wrong error");
    }
}