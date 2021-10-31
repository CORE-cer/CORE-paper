package edu.puc.core.execution;

import edu.puc.core.execution.cea.*;
import edu.puc.core.parser.plan.LogicalPlan;
import edu.puc.core.parser.plan.cea.PredicateFactory;
import edu.puc.core.parser.plan.query.ConsumptionPolicy;
import edu.puc.core.parser.plan.query.TimeWindow;
import edu.puc.core.runtime.predicates.BitSetGenerator;

class ExecutorFactory {
    private final LogicalPlan plan;
    private final Traverser traverser;
    private final BitSetGenerator bitSetGenerator;
    private final boolean discardPartials;

    /**
     * The base class factory method sets the {@link Traverser} depending on the
     * {@link LogicalPlan}'s {@link edu.puc.core.parser.plan.query.SelectionStrategy}.
     *
     * @param plan
     */
    ExecutorFactory(LogicalPlan plan){
        this.plan = plan;
        ExecutableCEA executableCEA = ExecutableCEAFactory.executorFor(plan.getPatternCEA());

        switch (plan.getSelectionStrategy()){
            case ALL:
                traverser = AllTraverser.getInstance(executableCEA);
                break;
            case MAX:
                traverser = MaxTraverser.getInstance(executableCEA);
                break;
            case LAST:
                traverser = LastTraverser.getInstance(executableCEA);
                break;
            case NEXT:
                traverser = NextTraverser.getInstance(executableCEA);
                break;
            case STRICT:
            default:
                throw new Error("Not implemented Yet");
        }
        discardPartials = plan.getConsumptionPolicy() == ConsumptionPolicy.ANY || plan.getConsumptionPolicy() == ConsumptionPolicy.PARTITION;
        bitSetGenerator = new BitSetGenerator(PredicateFactory.getInstance());
    }

    SimpleExecutor newSimpleExecutor(){
        return new SimpleExecutor(traverser, bitSetGenerator, discardPartials);
    }

    TimeWindowsExecutor newTimeWindowExecutor(TimeWindow timeWindow){
        return new TimeWindowsExecutor(traverser, bitSetGenerator, discardPartials, timeWindow);
    }

    PartitionExecutor newPartitionExecutor(LogicalPlan plan) {
        return new PartitionExecutor(plan, traverser, bitSetGenerator, discardPartials);
    }
}
