package edu.puc.core.parser.plan;

import edu.puc.core.parser.plan.exceptions.EventException;
import edu.puc.core.parser.plan.exceptions.NoSuchLabelException;
import edu.puc.core.parser.plan.values.ValueType;
import javafx.util.Pair;
import org.json.JSONObject;

import java.util.*;
import java.util.stream.Collectors;

public class Event {

    private static Map<String, Event> allEvents = new HashMap<>();
    private static final String TIMESTAMP_ATTR = "__ts";
    private static final String STREAM_ID_ATTR = "__stream";
    private static final String INDEX_ATTR = "__idx";
    private static final String TYPE_ATTR = "__type";

    private static final Map<String, ValueType> restrictedAttributes;

    static {
        HashMap<String, ValueType> map = new HashMap<>();
        map.put(TIMESTAMP_ATTR, ValueType.LONG);                        // timestamp
        map.put(STREAM_ID_ATTR, ValueType.INTEGER);                     // stream from which the event is coming
        map.put(INDEX_ATTR, ValueType.LONG);                            // event position in event feed
        map.put(TYPE_ATTR, ValueType.INTEGER);                          // event type as int
        restrictedAttributes = Collections.unmodifiableMap(map);
    }

    public static Map<String, ValueType> getRestrictedAttributes(){
        return restrictedAttributes;
    }

    private static Event _ANY = new Event();

    public static void invalidateEventSchema(){
        allEvents = new HashMap<>();
    }

    public static Event ANY() {
        return _ANY;
    }

    public static int count() {
        return allEvents.size();
    }

    public boolean isAny() {
        return _ANY.equals(this);
    }

    /**
     * Returns the Event instance for the given event name.
     *
     * @param eventName String specifying a event name.
     * @return The Event instance.
     */
    public static Event getSchemaFor(String eventName) {
        // can return null
        return allEvents.get(eventName);
    }

    public static Map<String, Event> getAllEvents() {
        return new HashMap<>(allEvents);
    }

    private static void ensureUnique(String name) throws EventException {
        if (allEvents.containsKey(name)) {
            throw new EventException("event of name `" + name + "` has already been declared");
        }
    }

    private static void checkAttributes(Set<String> attrNames, String evName) throws EventException {
        Set<String> intersection = new HashSet<>(restrictedAttributes.keySet());
        intersection.retainAll(attrNames);

        if (intersection.size() > 0) {
            String restricted = String.join("`, `", intersection);
            throw new EventException("event of name `" + evName + "` " +
                    "declares attributes with restricted names " +
                    "(`" + restricted + "`)");
        }

    }

    private String name;
    private List<Pair<String, ValueType>> attributes;
    private int eventType;

    private Event() {
        this.name = "ANY_EVENT";

        attributes = Collections.emptyList();
        eventType = -1;
//        allEvents.put(name, this);
        Label.forName(name, java.util.stream.Stream.of(this).collect(Collectors.toSet()));
    }

    public Event(String name, List<Pair<String, ValueType>> attributes) throws EventException {

        // perform some needed checks
        ensureUnique(name);
        checkAttributes(attributes.stream().map(Pair::getKey).collect(Collectors.toSet()), name);

        this.name = name;

        // copy the map so that we can modify it
        this.attributes = new ArrayList<>(attributes);

        eventType = allEvents.size();
        allEvents.put(name, this);
        Label.forName(name, java.util.stream.Stream.of(this).collect(Collectors.toSet()));
    }

    public Event(String name) throws EventException {
        this(name, Collections.emptyList());
    }


    public String getName() {
        return name;
    }

    public Label getNameLabel() {
        try {
            return Label.get(name);
        } catch (NoSuchLabelException exc) {
            return Label.forName(name, java.util.stream.Stream.of(this).collect(Collectors.toSet()));
        }
    }

    public int getEventType() {
        return eventType;
    }

    public List<Pair<String, ValueType>> getAttributes() {
        return attributes;
    }

    @Override
    public String toString() {
        return getClass().getName() + "(" +
                "name='" + name + "', " +
                "label='" + getNameLabel() + "', " +
                "attributes=" + attributes +
                ')';
    }

    public JSONObject toJSON() {
        JSONObject out = new JSONObject();
        out.put("name", name);
        out.put("attributes", attributes);

        return out;
    }
}
