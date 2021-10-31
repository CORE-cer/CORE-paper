package edu.puc.core.parser.plan.values.operations;


import edu.puc.core.parser.plan.values.Value;

public abstract class BinaryOperation extends Operation {

    Value lhs, rhs;

    BinaryOperation(Value lhs, Value rhs) {
        super(lhs, rhs);
        this.lhs = lhs;
        this.rhs = rhs;
    }

    public Value getRhs() {
        return rhs;
    }

    public Value getLhs() {
        return lhs;
    }
}
