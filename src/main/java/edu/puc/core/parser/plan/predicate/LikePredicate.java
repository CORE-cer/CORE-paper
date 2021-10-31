package edu.puc.core.parser.plan.predicate;


import edu.puc.core.parser.plan.exceptions.PredicateException;
import edu.puc.core.parser.plan.values.Attribute;
import edu.puc.core.parser.plan.values.StringLiteral;

public class LikePredicate extends AtomicPredicate {

    private final LogicalOperation logicalOperation;
    private final Attribute attribute;
    private final StringLiteral stringLiteral;

    private LikePredicate(Attribute attribute, LogicalOperation logicalOperation, StringLiteral stringLiteral) {
        this.attribute = attribute;
        this.logicalOperation = logicalOperation;
        this.stringLiteral = stringLiteral;
    }

    public LikePredicate(Attribute attribute, StringLiteral stringLiteral){
        this(attribute, LogicalOperation.LIKE, stringLiteral);
    }

    @Override
    public AtomicPredicate negate() throws PredicateException {
        return new LikePredicate(attribute, logicalOperation.negate(), stringLiteral);
    }

    @Override
    public boolean isConstant() {
        return false;
    }

    @Override
    public String toString() {
        return attribute.getName() + " " + logicalOperation + " " + stringLiteral.toString();
    }
}
