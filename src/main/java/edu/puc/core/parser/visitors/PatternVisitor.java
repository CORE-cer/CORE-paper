package edu.puc.core.parser.visitors;

import edu.puc.core.parser.COREBaseVisitor;
import edu.puc.core.parser.COREParser;
import edu.puc.core.parser.exceptions.UnknownNameException;
import edu.puc.core.parser.exceptions.UnknownStatementException;
import edu.puc.core.parser.plan.Event;
import edu.puc.core.parser.plan.Label;
import edu.puc.core.parser.plan.Stream;
import edu.puc.core.parser.plan.cea.*;
import edu.puc.core.parser.plan.filter.Filter;
import edu.puc.core.util.StringUtils;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class PatternVisitor extends COREBaseVisitor<CEA> {
    private final Collection<String> streamNames;
    private final Collection<String> eventNames;

    private final Set<Event> eventSet;


    public PatternVisitor(Collection<String> streamNames, Collection<String> eventNames){
        this.streamNames = streamNames;
        this.eventNames = eventNames;
        eventSet = new HashSet<>();
    }

    @Override
    public CEA visitPar_core_pattern(COREParser.Par_core_patternContext ctx) {
        // The context remains the same, we just ignore the parenthesis.
        // Get the child and visit without the need to instantiate a new visitor.
        return visit(ctx.core_pattern());
    }

    @Override
    public CEA visitAssign_core_pattern(COREParser.Assign_core_patternContext ctx) {
        // the context remains the same, no need for creating a new visitor
        CEA inner = visit(ctx.core_pattern());

        String newLabel = StringUtils.tryRemoveQuotes(ctx.event_name().getText());
        Label label = Label.forName(newLabel, eventSet);

        return new AssignCEA(inner, label);
    }

    @Override
    public CEA visitBinary_core_pattern(COREParser.Binary_core_patternContext ctx) {
        PatternVisitor visitorLeft = new PatternVisitor(streamNames, eventNames);
        PatternVisitor visitorRight = new PatternVisitor(streamNames, eventNames);

        // get the left and right patterns

        CEA left = visitorLeft.visit(ctx.core_pattern(0));
        CEA right = visitorLeft.visit(ctx.core_pattern(1));

        // add the newly found event schemas

        eventSet.addAll(visitorLeft.eventSet);
        eventSet.addAll(visitorRight.eventSet);

        // binary could be either `OR` or `;`.

        if (ctx.K_OR() != null) {
            return new OrCEA(left, right);
        } else if (ctx.SEMICOLON() != null) {
            return new SequenceCEA(left, right);
        } else {
            throw new UnknownStatementException("Binary operator unknown", ctx);
        }
    }

    @Override
    public CEA visitKleene_core_pattern(COREParser.Kleene_core_patternContext ctx) {
        // the context remains the same, no need for creating a new visitor
        CEA inner = visit(ctx.core_pattern());
        return new KleeneCEA(inner);
    }

    @Override
    public CEA visitEvent_core_pattern(COREParser.Event_core_patternContext ctx) {

        // event can either be named by itself or within the scope of a stream.
        // First check which is the case and deal with each of them separately
        String eventName = StringUtils.tryRemoveQuotes(ctx.s_event_name().event_name().getText());

        // Check if a stream is defined
        if (ctx.s_event_name().stream_name() != null) {
            String streamName = StringUtils.tryRemoveQuotes(ctx.s_event_name().stream_name().getText());

            Stream stream = Stream.getSchemaFor(streamName);
            if (stream == null)
                throw new UnknownNameException("Stream `" + streamName + "` is not defined", ctx.s_event_name().stream_name());

            if (!stream.containsEvent(eventName))
                throw new UnknownNameException("event `" + eventName + "` is not defined within stream `" + streamName + "`",
                        ctx.s_event_name().event_name());

            Event event = addEventToSet(eventName, ctx);

            return new SelectionCEA(stream, event);
        } else {
            // no stream is defined, just check that the event is declared within the scope of the
            // query
            if (!eventNames.contains(eventName)) {
                throw new UnknownNameException("event `" + eventName + "` is not defined within any of the query streams",
                        ctx.s_event_name());
            }

            Event event = addEventToSet(eventName, ctx);

            return new SelectionCEA(event);
        }
    }

    private Event addEventToSet(String eventName, COREParser.Event_core_patternContext ctx) throws UnknownNameException{
        // Create a selection ExecutableCEA that filters for the given stream
        Event event = Event.getSchemaFor(eventName);

        if (event == null)
            throw new UnknownNameException("event `" + eventName + "` is not defined", ctx.s_event_name().event_name());

        // Add the newly found schema to the set of visited schemas
        eventSet.add(event);
        return event;
    }

    @Override
    public CEA visitFilter_core_pattern(COREParser.Filter_core_patternContext ctx) {
        // First visit cel_pattern and extract variables. Then visit old_filter and
        // extract variables. Ensure that the variables defined within the old_filter are applied
        // over a subset of the variables defined in the pattern.
        // Push down the old_filter over the cel_pattern node

        CEA cea = visit(ctx.core_pattern());
        Filter patternFilter = new FilterVisitor().visit(ctx.filter());
        return patternFilter.applyToCEA(cea);
    }
}
