package edu.puc.core.parser.plan;

import edu.puc.core.parser.plan.exceptions.StreamException;
import org.json.JSONObject;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.stream.Collectors;

public class Stream {

    private static Map<String, Stream> allStreams = new HashMap<>();

    // This stream represents all streams
    private static Stream _ANY = new Stream();

    public static Stream ANY() {
        return _ANY;
    }

    public boolean isAny() {
        return _ANY.equals(this);
    }

    /**
     * Returns the Stream instance for the given stream name.
     *
     * @param streamName String specifying a stream name.
     * @return The Stream instance.
     */
    public static Stream getSchemaFor(String streamName) {
        // may return null
        return allStreams.get(streamName);
    }

    public static void invalidateStreamsSchema(){
        allStreams = new HashMap<>();
    }


    public static Map<String, Stream> getAllStreams() {
        return allStreams;
    }

    public static int count() {
        // we take into account that there is one stream representing the "ANY" stream
        // Objection: ANY stream does not add itself to allStreams.
        return allStreams.size() - 1;
    }

    private String name;
    private Collection<Event> events;
    private int streamID;

    private void ensureUnique(String name) throws StreamException {
        if (allStreams.containsKey(name)) {
            throw new StreamException("Stream of name `" + name + "` has already been declared");
        }
    }

    private Stream() {
        this.name = "ANY";
        this.events = java.util.stream.Stream.of(Event.ANY()).collect(Collectors.toList());
//        allStreams.put(name, this);
        streamID = -1;
    }

    public Stream(String name, Collection<Event> events) throws StreamException {
        ensureUnique(name);
        this.name = name;
        this.events = events;
        streamID = allStreams.size();
        allStreams.put(name, this);
    }

    public Stream(String name) throws StreamException {
        this(name, new HashSet<>());
    }


    public boolean containsEvent(String eventName) {
        for (Event ev : events) {
            if (ev.getName().equals(eventName)) {
                return true;
            }
        }
        return false;
    }

    public boolean containsEvent(Event e) {
        return containsEvent(e.getName());
    }

    public Collection<Event> getEvents() {
        return events;
    }

    public String getName() {
        return name;
    }

    public int getStreamID() {
        return streamID;
    }

    public JSONObject toJSON() {
        JSONObject out = new JSONObject();
        out.put("name", name);
        out.put("ID", streamID);
        out.put("events", events.stream().map(Event::toJSON).collect(Collectors.toList()));

        return out;
    }
}
