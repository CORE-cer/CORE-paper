package edu.puc.core.parser.plan.predicate;


import edu.puc.core.parser.plan.exceptions.PredicateException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

public class AndPredicate extends AtomicPredicate {

    private final Collection<AtomicPredicate> predicates;

    public Collection<AtomicPredicate> getPredicates() {
        return predicates;
    }

    public AndPredicate(AtomicPredicate... predicates) {
        this.predicates = Arrays.stream(predicates).collect(Collectors.toList());
    }

    public AndPredicate(Collection<AtomicPredicate> predicates) {
        this.predicates = predicates;
    }

    @Override
    public AtomicPredicate negate() throws PredicateException {
        ArrayList<AtomicPredicate> negated = new ArrayList<>();
        for (AtomicPredicate predicate : predicates) {
            negated.add(predicate.negate());
        }
        return new OrPredicate(negated);
    }

    @Override
    public boolean isConstant() {
        return predicates.stream().allMatch(AtomicPredicate::isConstant);
    }

    @Override
    public String toString() {
        return predicates.stream()
                .map(predicate -> "(" + predicate.toString() + ")")
                .collect(Collectors.joining(" and "));    }

}
