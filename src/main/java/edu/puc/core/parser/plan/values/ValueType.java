package edu.puc.core.parser.plan.values;

import edu.puc.core.parser.plan.exceptions.ValueException;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public enum ValueType {
    NUMERIC(java.util.stream.Stream.of(int.class, double.class, long.class, Integer.class, Long.class, Double.class).collect(Collectors.toSet())),
    INTEGER(java.util.stream.Stream.of(int.class, Integer.class).collect(Collectors.toSet())),
    LONG(java.util.stream.Stream.of(long.class, Long.class).collect(Collectors.toSet())),
    DOUBLE(java.util.stream.Stream.of(double.class, Double.class).collect(Collectors.toSet())),
    STRING(java.util.stream.Stream.of(String.class).collect(Collectors.toSet())),
    BOOLEAN(java.util.stream.Stream.of(boolean.class, Boolean.class).collect(Collectors.toSet()));

    public static EnumSet<ValueType> ANY() {
        return EnumSet.allOf(ValueType.class);
    }

    private Set<Class> validDataTypes;

    ValueType(Set<Class> validDataTypes) {
        this.validDataTypes = validDataTypes;
    }

    public boolean validForDataType(Class dataType) {
        return validDataTypes.contains(dataType);
    }

    public Set<Class> getDataTypes() {
        return new HashSet<>(validDataTypes);
    }

    public static ValueType getValueFor(String dataType) throws ValueException {
        switch (dataType) {
            case "double":
                return DOUBLE;
            case "int":
                return INTEGER;
            case "long":
                return LONG;
            case "string":
                return STRING;
            case "boolean":
                return BOOLEAN;
            default:
                throw new ValueException("Unknown data type: " + dataType);
        }
    }

    public boolean interoperableWith(ValueType valueType) {
        Set<Class> dataTypes = new HashSet<>(validDataTypes);
        dataTypes.retainAll(valueType.validDataTypes);
        return dataTypes.size() > 0;
    }

    public EnumSet<ValueType> getEnumSet() {
        EnumSet<ValueType> enumSet = EnumSet.noneOf(ValueType.class);
        for (ValueType valueType : ValueType.values()) {
            if (this.interoperableWith(valueType)) {
                enumSet.add(valueType);
            }
        }
        return enumSet;
    }
}
