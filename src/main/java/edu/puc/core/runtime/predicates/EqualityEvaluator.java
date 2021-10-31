package edu.puc.core.runtime.predicates;

import edu.puc.core.parser.plan.predicate.EqualityPredicate;
import edu.puc.core.parser.plan.predicate.LogicalOperation;
import edu.puc.core.runtime.events.Event;

public class EqualityEvaluator  extends PredicateEvaluator {

    private final ValueEvaluator left;
    private final ValueEvaluator right;
    private final boolean isNegated;


    public EqualityEvaluator(EqualityPredicate predicate){
        left = ValueEvaluator.getEvaluatorForValue(predicate.getLeft());
        right = ValueEvaluator.getEvaluatorForValue(predicate.getRight());
        isNegated = predicate.getLogicalOperation() == LogicalOperation.NOT_EQUALS;
    }

    @Override
    public boolean eval(Event event) {
        if (isNegated){
            return !left.eval(event).equals(right.eval(event));
        }
        return left.eval(event).equals(right.eval(event));
    }
}
