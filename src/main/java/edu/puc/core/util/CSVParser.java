package edu.puc.core.util;

import edu.puc.core.exceptions.EventException;
import edu.puc.core.parser.plan.values.ValueType;
import edu.puc.core.runtime.events.Event;
import javafx.util.Pair;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class CSVParser {
    private final String fileRoute;
    private String separator = ",";
    private BufferedReader br;
    private List<String> headers;

    public CSVParser(String csvFileRoute) {
        fileRoute = csvFileRoute;
        try {
            br = new BufferedReader(new FileReader(fileRoute));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        this.headers = parseLine();
    }

    public CSVParser(String csvFileRoute, String separator) {
        this(csvFileRoute);
        this.separator = separator;
    }

    public List<String> parseLine() {
        String[] csvLine = null;
        try {
            String line = br.readLine();
            if (line != null) csvLine = line.split(separator);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return csvLine == null ? null : Arrays.asList(csvLine);
    }

    public Event getEventFromCSVLine(List<String> line, String streamName) {
        if (line == null) { return null; }

        // Schema = (EvName, Time, Fields...)
        CSVEvent ev = new CSVEvent(streamName, line.get(0), line.get(1), line.subList(2, line.size()));

        edu.puc.core.parser.plan.Event eventSchema = edu.puc.core.parser.plan.Event.getSchemaFor(ev.getEvent());

        // If event not in the query, let it pass
        if (eventSchema == null) { return null; }

        List<Pair<String, ValueType>> attrDescriptions = eventSchema.getAttributes();
        List<Object> typedArgs = new ArrayList<>();
        List<String> attrs = ev.getArgs();
        for (int i=0; i < attrDescriptions.size(); i++){
            ValueType type = attrDescriptions.get(i).getValue();
            typedArgs.add(typeCast(attrs.get(i), type));
        }

        try {
            return Event.EventWithTimestamp(ev.getStream(), ev.getEvent(), ev.getTimestamp(), typedArgs.toArray());
        } catch (EventException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean ready() throws IOException {
        return br.ready();
    }

    public void close() {
        try {
            this.br.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void closeAndErase() {
        try {
            close();
            // Delete file contents
            new PrintWriter(fileRoute).close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Object typeCast(String s, ValueType type) {
        switch (type) {
            case INTEGER:
                return Integer.parseInt(s);
            case DOUBLE:
                return Double.parseDouble(s.replaceAll(",","."));
            case LONG:
                return Long.parseLong(s);
            case BOOLEAN:
                return Boolean.parseBoolean(s);
        }
        return (Object) s;

    }
}

class CSVEvent {
    private final String stream;
    private final String event;
    private final List<String> args;
    private long timestamp;

    public CSVEvent(String stream, String event, String timestamp, List<String> args) {
        this.stream = stream;
        this.event = event;
        this.args = args;

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = sdf.parse(timestamp);
            this.timestamp = date.getTime();
        }
        catch (ParseException e) {
            e.printStackTrace();
            this.timestamp = 0;
        }
    }

    public String getStream() { return this.stream; }
    public String getEvent() { return this.event; }
    public long getTimestamp() { return this.timestamp; }
    public List<String> getArgs() { return this.args; }
}

