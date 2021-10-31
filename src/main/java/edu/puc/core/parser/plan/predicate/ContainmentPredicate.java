package edu.puc.core.parser.plan.predicate;


import edu.puc.core.parser.plan.exceptions.PredicateException;
import edu.puc.core.parser.plan.values.Attribute;
import edu.puc.core.parser.plan.values.Literal;
import edu.puc.core.parser.plan.values.Value;

import java.util.ArrayList;
import java.util.Collection;

public class ContainmentPredicate extends AtomicPredicate {

    private final Value value;
    private final Collection<Literal> valueCollection;

    public Value getValue() {
        return value;
    }

    public Collection<Literal> getValueCollection() {
        return valueCollection;
    }

    public LogicalOperation getLogicalOperation() {
        return logicalOperation;
    }

    private final LogicalOperation logicalOperation;


    public ContainmentPredicate(Value value, LogicalOperation logicalOperation, Collection<Literal> values) throws PredicateException{
        if (!logicalOperation.isContainmentOperation())
            throw new PredicateException("Unexpected logical operation " + logicalOperation.toString());
        valueCollection = new ArrayList<>(values);
        this.logicalOperation = logicalOperation;
        this.value = value;
    }

    @Override
    public AtomicPredicate negate() throws PredicateException {
        return new ContainmentPredicate(value, logicalOperation.negate(), valueCollection);
    }

    @Override
    public boolean isConstant() {
        return !(value instanceof Attribute);
    }

    @Override
    public String toString() {
        return null;
    }
}
