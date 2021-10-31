package edu.puc.core.runtime.predicates;

import edu.puc.core.parser.plan.predicate.*;
import edu.puc.core.runtime.events.Event;

public abstract class PredicateEvaluator {
    public abstract boolean eval(Event event);

    /**
     * Analizes which kind of {@link AtomicPredicate} is to be evaluated and returns the
     * corresponding subclass of {@link PredicateEvaluator}.
     * 
     * @param predicate {@link AtomicPredicate} to be evaluated.
     * @return The corresponding subtype of {@link PredicateEvaluator} built
     * for the subtype of {@link AtomicPredicate} provided.
     */
    static PredicateEvaluator getEvaluatorForPredicate(AtomicPredicate predicate){
        if (predicate instanceof AndPredicate){
            return new AndEvaluator((AndPredicate) predicate);
        }
        else if (predicate instanceof ContainmentPredicate){
            return new ContainmentEvaluator((ContainmentPredicate) predicate);
        }
        else if (predicate instanceof EqualityPredicate){
            return new EqualityEvaluator((EqualityPredicate) predicate);
        }
        else if (predicate instanceof InequalityPredicate){
            return new InequalityEvaluator((InequalityPredicate) predicate);
        }
        else if (predicate instanceof LikePredicate){
            return new LikeEvaluator((LikePredicate) predicate);
        }
        else if (predicate instanceof OrPredicate){
            return new OrEvaluator((OrPredicate) predicate);
        }
        else {
            throw new Error("Unknown predicate type " + predicate.getClass().getName());
        }

    }
}
