package edu.puc.core.parser.plan.query;

public enum SelectionStrategy {
    ALL,
    MAX,
    NEXT,
    LAST,
    STRICT;

    public static SelectionStrategy getDefault() {
        return ALL;
    }
}
