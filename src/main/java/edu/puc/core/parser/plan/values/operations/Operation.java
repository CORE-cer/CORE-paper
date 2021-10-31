package edu.puc.core.parser.plan.values.operations;


import edu.puc.core.parser.plan.values.Value;

public abstract class Operation extends Value {
    Operation(Value lhs, Value rhs) {
        super();
        valueTypes.retainAll(lhs.getTypes());
        valueTypes.retainAll(rhs.getTypes());
        attributes.addAll(lhs.getAttributes());
        attributes.addAll(rhs.getAttributes());
    }

    Operation(Value inner) {
        super();
        valueTypes.retainAll(inner.getTypes());
        attributes.addAll(inner.getAttributes());
    }

    public abstract String toString();

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj;
    }

    @Override
    public boolean lessThan(Value otherValue) {
        return false;
    }

    @Override
    public boolean greaterThan(Value otherValue) {
        return false;
    }
}
