package edu.puc.core.parser.plan.query;

public enum ConsumptionPolicy {
    ANY,
    PARTITION,
    NONE;

    public static ConsumptionPolicy getDefault() {
        return ANY;
    }
}
