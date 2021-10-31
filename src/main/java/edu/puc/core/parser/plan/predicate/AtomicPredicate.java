package edu.puc.core.parser.plan.predicate;

import edu.puc.core.parser.plan.exceptions.PredicateException;

public abstract class AtomicPredicate {
    public abstract AtomicPredicate negate() throws PredicateException;

    public abstract boolean isConstant();

    public abstract String toString();
}
