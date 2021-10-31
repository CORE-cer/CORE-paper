package edu.puc.core.parser.plan;

import edu.puc.core.parser.plan.cea.CEA;
import edu.puc.core.parser.plan.query.*;

import java.util.Collection;
import java.util.Map;

public class LogicalPlan {

    private CEA patternCEA;
    private ConsumptionPolicy consumptionPolicy;
    private SelectionStrategy selectionStrategy;
    private ProjectionList projectionList;
    private Collection<Partition> partitions;
    private TimeWindow timeWindow;
    private Map<String, Stream> definedStreams;

    public LogicalPlan(SelectionStrategy selectionStrategy,
                       ProjectionList projectionList,
                       Map<String, Stream> definedStreams,
                       CEA patternCEA,
                       Collection<Partition> partitions,
                       TimeWindow timeWindow,
                       ConsumptionPolicy consumptionPolicy) {
        this.selectionStrategy = selectionStrategy;
        this.projectionList = projectionList;
        this.patternCEA = patternCEA;
        this.partitions = partitions;
        this.timeWindow = timeWindow;
        this.consumptionPolicy = consumptionPolicy;
        this.definedStreams = definedStreams;
    }

    public CEA getPatternCEA() {
        return patternCEA;
    }

    public ConsumptionPolicy getConsumptionPolicy() {
        return consumptionPolicy;
    }

    public SelectionStrategy getSelectionStrategy() {
        return selectionStrategy;
    }

    public ProjectionList getProjectionList() {
        return projectionList;
    }

    public Collection<Partition> getPartitions() {
        return partitions;
    }

    public TimeWindow getTimeWindow() {
        return timeWindow;
    }

    public Map<String, Stream> getDefinedStreams() {
        return definedStreams;
    }
}
