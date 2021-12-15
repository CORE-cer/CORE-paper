package edu.puc.core.execution;

import edu.puc.core.execution.callback.MatchCallback;
import edu.puc.core.execution.cea.Traverser;
import edu.puc.core.execution.structures.CDS.CDSNode;
import edu.puc.core.execution.structures.output.CDSComplexEventGrouping;
import edu.puc.core.execution.structures.states.State;
import edu.puc.core.execution.watcher.ExecutorWatcher;
import edu.puc.core.parser.plan.LogicalPlan;
import edu.puc.core.parser.plan.query.ConsumptionPolicy;
import edu.puc.core.parser.plan.query.TimeWindow;
import edu.puc.core.runtime.events.Event;
import edu.puc.core.runtime.predicates.BitSetGenerator;
import edu.puc.core.util.DistributionConfiguration;
import org.json.JSONObject;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public abstract class BaseExecutor {

    Consumer<CDSComplexEventGrouping> matchCallback = MatchCallback.getDefault();
    String query;

    final Traverser traverser;
    final BitSetGenerator bitSetGenerator;
    final boolean discardPartials; // ANY(default) and PARTITION will discard the previous events and complex events from the state.
    final ExecutorWatcher watcher = new ExecutorWatcher();
    Optional<DistributionConfiguration> distributionConfiguration = Optional.empty();

    // This corresponds to the hash table (states --> union-list) in Algorithm 1.
    // But union-lists are not implemented, they directly use CDSNode.
    Map<State<?>, CDSNode> states;
    Collection<State<?>> activeFinalStates;


    BaseExecutor(Traverser traverser, BitSetGenerator bitSetGenerator, boolean discardPartials) {
        this.traverser = traverser;
        this.bitSetGenerator = bitSetGenerator;
        this.discardPartials = discardPartials;
        setupCleanExecutor();
    }

    public static BaseExecutor fromPlan(LogicalPlan plan) {
        ExecutorFactory executorFactory = new ExecutorFactory(plan);
        if (plan.getPartitions().isEmpty()) {
            if (plan.getTimeWindow().getKind() == TimeWindow.Kind.NONE) {
                return executorFactory.newSimpleExecutor();
            } else {
                return executorFactory.newTimeWindowExecutor(plan.getTimeWindow());
            }
        }
        return executorFactory.newPartitionExecutor(plan);

    }

    /**
     * Use the set callback to enumerate the outputs generated with a triggering
     * {@link Event}. Clean the executor if the {@link ConsumptionPolicy} is ANY.
     */
    void enumerate(Event triggeringEvent) {
        enumerate(triggeringEvent, 0);
    }

    void enumerate(Event triggeringEvent, long limit) {
        watcher.update();
        if (matchCallback != null) {
            // CDSComplexEventGrouping implements the Algorithm 2 on an Iterator pattern.
            CDSComplexEventGrouping complexEventGrouping = new CDSComplexEventGrouping(triggeringEvent, limit, this.distributionConfiguration);
            for (State<?> state : activeFinalStates) {
                complexEventGrouping.addCDSNode(states.get(state));
            }
            matchCallback.accept(complexEventGrouping);
        }
        if (discardPartials) {
            setupCleanExecutor();
        }
    }

    /**
     * Clean the executor. This is called after triggering output and having the ANY
     * {@link edu.puc.core.parser.plan.query.ConsumptionPolicy ConsumptionPolicy}.
     *
     */
    abstract void setupCleanExecutor();

    /**
     * Receives a new {@link Event} and updates the states the CEA is currently
     * executing.
     *
     * @param event
     */
    public abstract boolean sendEvent(Event event);

    /**
     * Sets the given function as a callback for when a triggering {@link Event} is
     * found, for it to enumerate the output. This callback receives a
     * {@link CDSComplexEventGrouping} as argument.
     *
     * @param callback The callback function to use for enumeration.
     */
    public void setMatchCallback(Consumer<CDSComplexEventGrouping> callback) {
        matchCallback = callback;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public void setDistributionConfiguration(Optional<DistributionConfiguration> distributionConfiguration) {
        this.distributionConfiguration = distributionConfiguration;
    }

    public ExecutorWatcher getWatcher() {
        return watcher;
    }

    public JSONObject toJSON() {
        JSONObject out = new JSONObject();
        out.put("query", query);
        out.put("stats", watcher.toJSON());

        return out;
    }
}
