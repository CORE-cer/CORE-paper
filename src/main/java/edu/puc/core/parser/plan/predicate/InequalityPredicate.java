package edu.puc.core.parser.plan.predicate;


import edu.puc.core.parser.plan.exceptions.PredicateException;
import edu.puc.core.parser.plan.values.Attribute;
import edu.puc.core.parser.plan.values.Value;

public class InequalityPredicate extends AtomicPredicate{
    public LogicalOperation getLogicalOperation() {
        return logicalOperation;
    }

    public Value getLeft() {
        return left;
    }

    public Value getRight() {
        return right;
    }

    private final LogicalOperation logicalOperation;
    private final Value left;
    private final Value right;

    public InequalityPredicate(Value left, LogicalOperation logicalOperation, Value right) {
        if (!logicalOperation.isInequalityOperation())
            throw new Error("Unexpected logical operation " + logicalOperation.toString());
        this.logicalOperation = logicalOperation;
        this.left = left;
        this.right = right;
    }

    @Override
    public AtomicPredicate negate() throws PredicateException {
        return new EqualityPredicate(left, logicalOperation.negate(), right);
    }

    @Override
    public boolean isConstant() {
        return !(left instanceof Attribute || right instanceof Attribute);
    }

    @Override
    public String toString() {
        return left.toString() + " " + logicalOperation.toString() + " " + right.toString();
    }
}
