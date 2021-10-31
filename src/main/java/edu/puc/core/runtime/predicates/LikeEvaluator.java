package edu.puc.core.runtime.predicates;


import edu.puc.core.parser.plan.predicate.LikePredicate;
import edu.puc.core.runtime.events.Event;

public class LikeEvaluator  extends PredicateEvaluator {

    public LikeEvaluator(LikePredicate predicate){

    }

    @Override
    public boolean eval(Event event) {
        return false;
    }
}
