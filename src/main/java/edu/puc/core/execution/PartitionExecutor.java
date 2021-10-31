package edu.puc.core.execution;

import edu.puc.core.execution.cea.Traverser;
import edu.puc.core.parser.plan.LogicalPlan;
import edu.puc.core.parser.plan.query.ConsumptionPolicy;
import edu.puc.core.parser.plan.query.Partition;
import edu.puc.core.parser.plan.query.TimeWindow;
import edu.puc.core.runtime.events.Event;
import edu.puc.core.runtime.predicates.BitSetGenerator;

import java.util.Collection;
import java.util.HashMap;
import java.util.stream.Collectors;

public class PartitionExecutor extends BaseExecutor {
    private final Collection<Partition> partitions;
    private HashMap<Integer, BaseExecutor> executorHashMap;
    private final LogicalPlan plan;

    PartitionExecutor(LogicalPlan plan, Traverser traverser, BitSetGenerator bitSetGenerator, boolean discardPartials) {
        super(traverser, bitSetGenerator, discardPartials);
        this.partitions = plan.getPartitions();
        this.plan = plan;
    }

    @Override
    void setupCleanExecutor() {
        if (executorHashMap == null) {
            executorHashMap = new HashMap<>();
        }
        for (BaseExecutor ex: executorHashMap.values()) {
            ex.setupCleanExecutor();
        }
    }

    @Override
    public boolean sendEvent(Event event) {
        int hashCode = partitions.stream().map(p -> {
            String attributeName = p.getAttributeFor(event);
            return event.getValue(attributeName).hashCode();
        }).collect(Collectors.toList()).hashCode();

        if (!executorHashMap.containsKey(hashCode)){
            BaseExecutor newExecutor;
            if (plan.getTimeWindow().getKind() == TimeWindow.Kind.NONE) {
                newExecutor = new SimpleExecutor(traverser, bitSetGenerator, discardPartials);
            } else {
                newExecutor = new TimeWindowsExecutor(traverser, bitSetGenerator, discardPartials, plan.getTimeWindow());
            }
            newExecutor.setupCleanExecutor();
            executorHashMap.put(hashCode, newExecutor);
        }
        if (executorHashMap.get(hashCode).sendEvent(event)) {
            cleanExecutors();
            return true;
        }
        return false;
    }

    private void cleanExecutors() {
        if (plan.getConsumptionPolicy() == ConsumptionPolicy.ANY) {
            for (BaseExecutor ex: executorHashMap.values()) {
                ex.setupCleanExecutor();
            }
        }
    }
}
