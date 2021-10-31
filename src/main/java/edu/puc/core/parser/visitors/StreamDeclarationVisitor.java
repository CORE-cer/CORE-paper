package edu.puc.core.parser.visitors;

import edu.puc.core.parser.COREBaseVisitor;
import edu.puc.core.parser.COREParser;
import edu.puc.core.parser.exceptions.DuplicateNameException;
import edu.puc.core.parser.exceptions.MissingValueException;
import edu.puc.core.parser.exceptions.UnknownNameException;
import edu.puc.core.parser.plan.Event;
import edu.puc.core.parser.plan.Stream;
import edu.puc.core.parser.plan.exceptions.StreamException;
import edu.puc.core.util.StringUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class StreamDeclarationVisitor extends COREBaseVisitor<Stream> {

    public Stream visitStream_declaration(COREParser.Stream_declarationContext ctx) {
        String streamName = StringUtils.tryRemoveQuotes(ctx.stream_name().getText());

        try {
            Collection<Event> eventSchemas = ctx.event_list().accept(new EventListVisitor());
            return new Stream(streamName, eventSchemas);
        } catch (NullPointerException exc) {
            throw new MissingValueException("No events declared for stream " + ctx.stream_name().getText(), ctx.stream_name());
        } catch (StreamException exc) {
            throw new DuplicateNameException(exc.getMessage(), ctx);
        }
    }
}

class EventListVisitor extends COREBaseVisitor<Collection<Event>> {

    public Collection<Event> visitEvent_list(COREParser.Event_listContext ctx) {

        Map<String, Event> eventNames = new HashMap<>();

        for (COREParser.Event_nameContext eventNameContext : ctx.event_name()) {
            String eventName = StringUtils.tryRemoveQuotes(eventNameContext.getText());

            // event is declared more than once in the stream declaration
            if (eventNames.containsKey(eventName)) {
                throw new DuplicateNameException("event `" + eventName +
                        "` is referenced more than once within stream declaration", eventNameContext);
            }

            Event event = Event.getSchemaFor(eventName);

            // event has not been declared
            if (event == null) {
                throw new UnknownNameException("event `" + eventName + "` is not defined", eventNameContext);
            }
            eventNames.put(eventName, event);
        }

        return eventNames.values();
    }
}


