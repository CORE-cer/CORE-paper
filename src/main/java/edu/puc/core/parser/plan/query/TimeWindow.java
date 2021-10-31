package edu.puc.core.parser.plan.query;

public class TimeWindow {
    public enum Kind {
        EVENTS,
        TIME,
        CUSTOM,
        NONE
    }

    public Kind getKind() {
        return kind;
    }

    public long getNumberOfEvents() {
        if (kind != Kind.EVENTS) throw new Error("Invalid span for TimeWindow");
        return span;
    }

    public long getNumberOfMilis() {
        if (kind != Kind.TIME) throw new Error("Invalid span for TimeWindow");
        return span * 1000;
    }

    public long getCustomNumber() {
        if (kind != Kind.CUSTOM) throw new Error("Invalid span for TimeWindow");
        return span;
    }

    private final Kind kind;
    private long span;
    private String attr;

    private TimeWindow() {
        kind = Kind.NONE;
    }

    /**
     * Creates a new TimeWindow object.
     *
     * @param kind type of TimeWindow: either this window restricts on number of events or on number of seconds elapsed
     * @param span Depending of kind, either the maximum number of events or the maximum number of seconds that have occurred since the start of a match.
     */
    public TimeWindow(Kind kind, long span) {
        if (kind == Kind.NONE) {
            throw new Error("use `TimeWindow.NONE` instead");
        }
        this.span = span;
        this.kind = kind;
    }

    public TimeWindow(Kind kind, long span, String attr) {
        if (kind == Kind.NONE) {
            throw new Error("use `TimeWindow.NONE` instead");
        }
        this.span = span;
        this.kind = kind;
        this.attr = attr;
    }

    public String getAttr() {
        return attr;
    }

    public static final TimeWindow NONE = new TimeWindow();
}
