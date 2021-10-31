package edu.puc.core.parser.plan.predicate;


public enum LogicalOperation {
    EQUALS("=="),
    GREATER(">"),
    GREATER_EQUALS(">="),
    IN("IN"),
    LESS_EQUALS("<="),
    LESS("<"),
    LIKE("LIKE"),
    NOT_LIKE("NOT LIKE"),
    NOT_EQUALS("!="),
    NOT_IN("NOT IN"),
    OR("||");

    private String symbol;

    LogicalOperation(String symbol) {
        this.symbol = symbol;
    }

    boolean isEqualityOperation(){
        return this == EQUALS || this == NOT_EQUALS;
    }

    boolean isContainmentOperation(){
        return this == IN || this == NOT_IN;
    }

    boolean isInequalityOperation(){
        return this == LESS || this == LESS_EQUALS || this == GREATER || this == GREATER_EQUALS;
    }

    LogicalOperation negate() {
        switch (this) {
            case EQUALS:
                return NOT_EQUALS;
            case NOT_EQUALS:
                return EQUALS;
            case GREATER:
                return LESS_EQUALS;
            case GREATER_EQUALS:
                return LESS;
            case LESS:
                return GREATER_EQUALS;
            case LESS_EQUALS:
                return GREATER;
            case IN:
                return NOT_IN;
            case NOT_IN:
                return IN;
            case LIKE:
                return NOT_LIKE;
            case NOT_LIKE:
                return LIKE;
            default:
                throw new Error("Impossible to negate logical operation " + this.name());
        }
    }

    LogicalOperation flip() {
        switch (this) {
            case EQUALS:
                return EQUALS;
            case NOT_EQUALS:
                return NOT_EQUALS;
            case GREATER:
                return LESS;
            case GREATER_EQUALS:
                return LESS_EQUALS;
            case LESS:
                return GREATER;
            case LESS_EQUALS:
                return GREATER_EQUALS;
            default:
                throw new Error("Impossible to flip logical operation " + this.name());
        }
    }

    @Override
    public String toString() {
        return symbol;
    }
}
