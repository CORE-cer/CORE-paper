package edu.puc.core.parser.plan.values.operations;


import edu.puc.core.parser.plan.values.Value;

public abstract class UnaryOperation extends Operation {

    Value inner;

    UnaryOperation(Value inner) {
        super(inner);
        this.inner = inner;
    }

    public Value getInner() {
        return inner;
    }
}
