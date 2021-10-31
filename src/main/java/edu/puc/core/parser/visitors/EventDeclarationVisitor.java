package edu.puc.core.parser.visitors;


import edu.puc.core.parser.COREBaseVisitor;
import edu.puc.core.parser.COREParser;
import edu.puc.core.parser.exceptions.DuplicateNameException;
import edu.puc.core.parser.plan.Event;
import edu.puc.core.parser.plan.exceptions.EventException;
import edu.puc.core.parser.plan.values.ValueType;
import edu.puc.core.util.StringUtils;
import javafx.util.Pair;

import java.util.List;

public class EventDeclarationVisitor extends COREBaseVisitor<Event> {

    public Event visitEvent_declaration(COREParser.Event_declarationContext ctx) {
        String eventName = StringUtils.tryRemoveQuotes(ctx.event_name().getText());
        List<Pair<String, ValueType>> attributeMap = new AttributeDeclarationVisitor().visitAttribute_dec_list(ctx.attribute_dec_list());

        try {
            return new Event(eventName, attributeMap);
        } catch (EventException exc) {
            throw new DuplicateNameException(exc.getMessage(), ctx);
        }
    }
}

