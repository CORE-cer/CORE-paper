package edu.puc.core.runtime.events;

import edu.puc.core.exceptions.EventException;
import edu.puc.core.parser.plan.Stream;
import edu.puc.core.parser.plan.values.ValueType;
import javafx.util.Pair;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class Event {

    public static Event EMPTY = new Event();

    private static final AtomicInteger idx = new AtomicInteger(0);

    private long timestamp;
    private final int stream;
    private final int type;
    private final long index;

    public long getTimestamp() {
        return timestamp;
    }

    public int getStream() {
        return stream;
    }

    public long getIndex() {
        return index;
    }

    public int getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getStreamName() {
        return streamName;
    }

    private final String name;
    private final String streamName;

    private final Object[] fields;
    private final List<Pair<String, ValueType>> fieldDescriptions;

    public static Event EventWithTimestamp(String streamName, String eventName, long timestamp, Object... args) throws EventException {
        Event ev = new Event(streamName, eventName, args);
        ev.timestamp = timestamp;
        return ev;
    }

    public Event(long index) {
        timestamp = -1;
        stream = -1;
        type = -1;
        this.index = index;

        name = null;
        streamName = null;
        fields = null;
        fieldDescriptions = null;
    }

    private Event() {
        timestamp = -1;
        stream = -1;
        type = -1;
        index = -1;

        name = null;
        streamName = null;
        fields = null;
        fieldDescriptions = null;
    }

    public Event(String streamName, String eventName, Object... args) throws EventException {
        Stream stream = Stream.getSchemaFor(streamName);
        if (stream == null)
            throw new EventException("No stream of name " + streamName);

        edu.puc.core.parser.plan.Event eventSchema = edu.puc.core.parser.plan.Event.getSchemaFor(eventName);
        if (eventSchema == null)
            throw new EventException("No event of name " + eventName);

        if (!stream.containsEvent(eventName))
            throw new EventException("No event of name " + eventName + " on stream " + streamName);

        timestamp = System.currentTimeMillis();
        this.stream = stream.getStreamID();
        index = idx.getAndIncrement();
        type = eventSchema.getEventType();
        this.streamName = streamName;
        name = eventName;

        fields = args.clone();
        fieldDescriptions = eventSchema.getAttributes();

        if (args.length != fieldDescriptions.size())
            throw new EventException("Wrong number of field values given for event " + eventName);
        for (int i = 0; i < args.length; i++) {
            if (!fieldDescriptions.get(i).getValue().validForDataType(args[i].getClass()))
                throw new EventException("Invalid values given for field " + fieldDescriptions.get(i).getKey()
                        + " of event " + eventName);
            if (fieldDescriptions.get(i).getValue().interoperableWith(ValueType.NUMERIC)) {
                fields[i] = ((Number) fields[i]).doubleValue();
            }
        }

    }

    public Object getValue(String field) {
        int idx = fieldDescriptions.stream().map(Pair::getKey).collect(Collectors.toList()).indexOf(field);
        if (idx < 0)
            throw new Error("No such field " + field + " on event " + name);
        return fields[idx];
    }

    public boolean isEmpty() {
        return this == EMPTY;
    }

    public Object getValue(int idx) {
        return fields[idx];
    }

    @Override
    public String toString() {
        if (type == -1) {
            return "";
        }
//        StringBuilder s = new StringBuilder();
//        for (int i = 0; i < fieldDescriptions.size(); i++) {
//            s.append(", ");
//            s.append(fieldDescriptions.get(i).getKey());
//            s.append("=");
//            s.append(fields[i]);
//        }
        return name + "(id=" + fields[0] + ")";
    }
}
