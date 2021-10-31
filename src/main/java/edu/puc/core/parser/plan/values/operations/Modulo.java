package edu.puc.core.parser.plan.values.operations;


import edu.puc.core.parser.plan.exceptions.IncompatibleValueException;
import edu.puc.core.parser.plan.values.Value;
import edu.puc.core.parser.plan.values.ValueType;

public class Modulo extends BinaryOperation {

    public Modulo(Value lhs, Value rhs) throws IncompatibleValueException {
        super(lhs, rhs);
        // modulo is only valid over numeric types
        if (!interoperableWith(ValueType.NUMERIC)) {
            throw new IncompatibleValueException();
        }
    }

    @Override
    public String toString() {
        return "(" + lhs.toString() + " % " + rhs.toString() + ")";
    }
}
