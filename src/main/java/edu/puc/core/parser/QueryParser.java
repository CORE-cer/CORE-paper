package edu.puc.core.parser;

import edu.puc.core.parser.exceptions.DuplicateNameException;
import edu.puc.core.parser.exceptions.MissingValueException;
import edu.puc.core.parser.exceptions.UnknownNameException;
import edu.puc.core.parser.exceptions.UnknownStatementException;
import edu.puc.core.parser.plan.Event;
import edu.puc.core.parser.plan.Label;
import edu.puc.core.parser.plan.LogicalPlan;
import edu.puc.core.parser.plan.Stream;
import edu.puc.core.parser.plan.cea.CEA;
import edu.puc.core.parser.plan.cea.PredicateFactory;
import edu.puc.core.parser.plan.cea.ProjectionCEA;
import edu.puc.core.parser.plan.exceptions.NoSuchLabelException;
import edu.puc.core.parser.plan.query.*;
import edu.puc.core.parser.visitors.FirstPassVisitor;
import edu.puc.core.parser.visitors.PatternVisitor;
import edu.puc.core.parser.visitors.TimeSpanVisitor;
import edu.puc.core.util.StringUtils;
import javafx.util.Pair;
import org.antlr.v4.runtime.misc.ParseCancellationException;

import java.util.*;
import java.util.stream.Collectors;

public class QueryParser extends BaseParser<LogicalPlan, COREParser.Core_queryContext> {

    /**
     * Parses the source query and returns the Logical Plan.
     *
     * @param query String containing the CORE query.
     * @return The Logical Plans for the given query.
     * @throws ParseCancellationException
     */
    @Override
    public LogicalPlan parse(String query) throws ParseCancellationException {
        COREParser coreParser = getParserForSource(query);
        // a declaration can only be valid if it parses on this parser rule
        COREParser.Core_queryContext tree = coreParser.core_query();

        /*TODO*/
        return compileContext(tree);
    }

    /**
     * Builds the Logical Plan from the given Query Context.
     * Retrieves the Selection Strategy, Projections, Streams, Pattern,
     * Partitions, Time Window & Consumption Policy, and builds the
     * Logical Plan with them.
     *
     * @param queryContext Query Context built from a query string.
     * @return The Logical Plan for the given Query.
     * @throws UnknownStatementException
     */
    @Override
    LogicalPlan compileContext(COREParser.Core_queryContext queryContext) throws UnknownStatementException {

        // initiate bit predicate factory
        FirstPassVisitor firstPassVisitor = new FirstPassVisitor();
        firstPassVisitor.visit(queryContext);

        PredicateFactory.init(Stream.getAllStreams().values(), Event.getAllEvents().values(), firstPassVisitor.getAtomicPredicateCount());

        // selection strategy
        SelectionStrategy selectionStrategy = parseSelectionStrategy(queryContext.selection_strategy());

        // projected values
        ProjectionList projectionList = parseProjectionList(queryContext.result_values());

        // streams to use
        Map<String, Stream> definedStreams = parseStreamList(queryContext.stream_name());
        Collection<String> definedEvents = getEventsForStreams(definedStreams.keySet());

        // pattern
        CEA patternCEA = queryContext.core_pattern().accept(new PatternVisitor(definedStreams.keySet(), definedEvents)).getCleanCea();
        patternCEA = new ProjectionCEA(patternCEA, projectionList);

        // partitions
        Collection<Partition> partitions = parsePartitionList(queryContext.partition_list(), firstPassVisitor.getEvents());

        // time window
        TimeWindow timeWindow = parseTimeWindow(queryContext.time_window());

        // consumption policy
        ConsumptionPolicy consumptionPolicy = parseConsumptionPolicy(queryContext.consumption_policy());


        return new LogicalPlan(
                selectionStrategy,
                projectionList,
                definedStreams,
                patternCEA,
                partitions,
                timeWindow,
                consumptionPolicy);
    }

    private SelectionStrategy parseSelectionStrategy(COREParser.Selection_strategyContext ctx) throws UnknownStatementException {
        if (ctx == null) {
            return SelectionStrategy.getDefault();
        }
        if (ctx instanceof COREParser.Ss_allContext) {
            return SelectionStrategy.ALL;
        } else if (ctx instanceof COREParser.Ss_lastContext) {
            return SelectionStrategy.LAST;
        } else if (ctx instanceof COREParser.Ss_maxContext) {
            return SelectionStrategy.MAX;
        } else if (ctx instanceof COREParser.Ss_nextContext) {
            return SelectionStrategy.NEXT;
        } else {
            throw new UnknownStatementException("This kind of selection strategy has not been implemented", ctx);
        }
    }

    private ProjectionList parseProjectionList(COREParser.Result_valuesContext ctx) {
        if (ctx.STAR() != null) {
            return ProjectionList.ALL_EVENTS;
        } else {
            Collection<Label> projectedLabels = ctx.s_event_name()
                    .stream()
                    .map(event_nameContext -> {
                        try {
                            return Label.get(event_nameContext.getText());
                        } catch (NoSuchLabelException exc) {
                            throw new UnknownNameException("No label or event `" + event_nameContext.getText()
                                    + "` is ever defined on capturing query pattern", event_nameContext);
                        }
                    })
                    .collect(Collectors.toList());

            return new ProjectionList(projectedLabels);
        }
    }

    private Collection<Partition> parsePartitionList(COREParser.Partition_listContext ctx, Set<Event> eventSet) {
        if (ctx == null) {
            return new ArrayList<>();
        }

        ArrayList<Partition> partitionList = new ArrayList<>();
        Set<String> definedAttributes = eventSet.stream()
                .map(Event::getAttributes)
                .flatMap(List::stream)
                .map(Pair::getKey)
                .collect(Collectors.toSet());

        for (COREParser.Attribute_listContext partitionCtx : ctx.attribute_list()) {
            HashSet<String> attributeNames = new HashSet<>();
            for (COREParser.Attribute_nameContext attrNameCtx : partitionCtx.attribute_name()) {
                String attrName = StringUtils.tryRemoveQuotes(attrNameCtx.getText());

                if (!definedAttributes.contains(attrName)) {
                    throw new UnknownNameException("Attribute `" + attrName +
                            "` is not defined on any event named on query", attrNameCtx);
                }

                if (attributeNames.contains(attrName)) {
                    throw new DuplicateNameException("Attribute `" + attrName +
                            "` is defined more than once on partition list", attrNameCtx);
                }
                attributeNames.add(attrName);
            }

            for (Event event : eventSet) {
                Set<String> eventAttributes = event.getAttributes().stream().map(Pair::getKey).collect(Collectors.toSet());
                eventAttributes.retainAll(attributeNames);
                if (eventAttributes.size() == 0) {
                    throw new MissingValueException("Partition list does not contain any attribute for events of type "
                            + event.getName(), partitionCtx);
                }
            }
            partitionList.add(new Partition(attributeNames));
        }
        return partitionList;
    }

    private Map<String, Stream> parseStreamList(List<COREParser.Stream_nameContext> ctx) {
        if (ctx == null) {
            return Stream.getAllStreams();
        }

        Map<String, Stream> streamMap = new HashMap<>();

        for (COREParser.Stream_nameContext nameContext : ctx) {
            String streamName = nameContext.getText();
            Stream streamSchema = Stream.getSchemaFor(streamName);
            if (streamSchema == null) {
                throw new UnknownNameException("Unknown stream of name `" + streamName + "`", nameContext);
            }
            if (streamMap.containsKey(streamName)) {
                throw new DuplicateNameException("Stream `" + streamName + "` declared more than once on query", nameContext);
            }
            streamMap.put(streamName, streamSchema);
        }

        return streamMap;
    }

    private Collection<String> getEventsForStreams(Collection<String> streamNames) {
        return streamNames.stream()
                .map(streamName -> Stream.getSchemaFor(streamName).getEvents())
                .flatMap(Collection::stream)
                .map(Event::getName)
                .collect(Collectors.toSet());
    }

    private TimeWindow parseTimeWindow(COREParser.Time_windowContext ctx) throws UnknownStatementException {
        if (ctx == null)
            return TimeWindow.NONE;
        if (ctx.event_span() != null) {
            long nEvents = Long.parseLong(ctx.event_span().integer().getText());
            return new TimeWindow(TimeWindow.Kind.EVENTS, nEvents);
        } else if (ctx.time_span() != null) {
            long seconds = new TimeSpanVisitor().visit(ctx.time_span());
            return new TimeWindow(TimeWindow.Kind.TIME, seconds);
        } else if (ctx.custom_span() != null) {
            long nCustom = Long.parseLong(ctx.custom_span().integer().getText());
            String attr = ctx.custom_span().any_name().getText();
            return new TimeWindow(TimeWindow.Kind.CUSTOM, nCustom, attr);
        } else {
            throw new UnknownStatementException("Can't parse this kind of time window", ctx);
        }
    }

    private ConsumptionPolicy parseConsumptionPolicy(COREParser.Consumption_policyContext ctx) throws UnknownStatementException {
        if (ctx == null) {
            return ConsumptionPolicy.getDefault();
        }
        if (ctx instanceof COREParser.Cp_anyContext) {
            return ConsumptionPolicy.ANY;
        } else if (ctx instanceof COREParser.Cp_noneContext) {
            return ConsumptionPolicy.NONE;
        } else if (ctx instanceof COREParser.Cp_partitionContext) {
            return ConsumptionPolicy.PARTITION;
        } else {
            throw new UnknownStatementException("This type of consumption policy has not been implemented", ctx);
        }
    }
}
