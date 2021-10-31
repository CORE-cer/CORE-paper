package edu.puc.core.parser.plan.values;


public class NumberLiteral extends Literal {

    private double value;

    private NumberLiteral(double value) {
        super(ValueType.NUMERIC);
        this.value = value;
    }

    public NumberLiteral(String number) {
        this(Double.parseDouble(number));
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof NumberLiteral)) return false;
        return ((NumberLiteral) obj).value == value;
    }

    @Override
    public boolean lessThan(Value otherValue) {
        if (this == otherValue) return false;
        if (!(otherValue instanceof NumberLiteral)) return false;
        return ((NumberLiteral) otherValue).value < value;
    }

    @Override
    public boolean greaterThan(Value otherValue) {
        if (this == otherValue) return false;
        if (!(otherValue instanceof NumberLiteral)) return false;
        return ((NumberLiteral) otherValue).value > value;
    }

    @Override
    public int hashCode() {
        return Double.hashCode(value);
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    public Double getValue() {
        return value;
    }
}
