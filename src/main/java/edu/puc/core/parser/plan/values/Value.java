package edu.puc.core.parser.plan.values;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

public abstract class Value {
    protected EnumSet<ValueType> valueTypes;
    protected Set<Attribute> attributes;

    public Value() {
        valueTypes = ValueType.ANY();
        attributes = new HashSet<>();
    }

    public Value(ValueType valueType) {
        attributes = new HashSet<>();
        valueTypes = valueType.getEnumSet();
    }

    public Value(EnumSet<ValueType> valueTypes) {
        attributes = new HashSet<>();
        this.valueTypes = EnumSet.noneOf(ValueType.class);
        for (ValueType valueType : valueTypes) {
            this.valueTypes.addAll(valueType.getEnumSet());
        }
    }

    public EnumSet<ValueType> getTypes() {
        return valueTypes.clone();
    }

    public Set<Attribute> getAttributes() {
        return attributes;
    }

    public abstract boolean equals(Object obj);

    public abstract boolean lessThan(Value otherValue);

    public abstract boolean greaterThan(Value otherValue);

    public abstract int hashCode();

    public boolean interoperableWith(ValueType valueType) {
        for (ValueType myValueType : valueTypes) {
            if (myValueType.interoperableWith(valueType)) return true;
        }
        return false;
    }
}
