package edu.puc.core.parser.plan.values.operations;


import edu.puc.core.parser.plan.exceptions.IncompatibleValueException;
import edu.puc.core.parser.plan.values.Value;
import edu.puc.core.parser.plan.values.ValueType;

public class Addition extends BinaryOperation {

    public Addition(Value lhs, Value rhs) throws IncompatibleValueException {
        super(lhs, rhs);
        // additions is compatible with strings and numbers
        if (!interoperableWith(ValueType.NUMERIC) && !interoperableWith(ValueType.STRING)) {
            throw new IncompatibleValueException();
        }
    }

    @Override
    public String toString() {
        return "(" + lhs.toString() + " + " + rhs.toString() + ")";
    }
}
