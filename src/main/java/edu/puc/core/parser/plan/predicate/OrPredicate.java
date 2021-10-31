package edu.puc.core.parser.plan.predicate;


import edu.puc.core.parser.plan.exceptions.PredicateException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

public class OrPredicate extends AtomicPredicate {

    private final Collection<AtomicPredicate> predicates;

    public Collection<AtomicPredicate> getPredicates() {
        return predicates;
    }

    public OrPredicate(AtomicPredicate... predicates) {
        this.predicates = Arrays.stream(predicates).collect(Collectors.toList());
    }

    public OrPredicate(Collection<AtomicPredicate> predicates) {
        this.predicates = predicates;
    }

    @Override
    public AtomicPredicate negate() throws PredicateException {
        ArrayList<AtomicPredicate> negated = new ArrayList<>();
        for (AtomicPredicate predicate : predicates) {
            negated.add(predicate.negate());
        }
        return new AndPredicate(negated);
    }

    @Override
    public boolean isConstant() {
        // TODO: this check should be better. It is also constant if any one is constant and always true
        return predicates.stream().allMatch(AtomicPredicate::isConstant);
    }

    @Override
    public String toString() {
        return predicates.stream()
                .map(predicate -> "(" + predicate.toString() + ")")
                .collect(Collectors.joining(" or "));
    }
}
