package edu.puc.core.engine.streams;

import edu.puc.core.engine.BaseEngine;
import edu.puc.core.parser.plan.Stream;
import edu.puc.core.runtime.events.Event;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.TimeUnit;

public class StreamManager {

    private final Map<String, StreamReader> streamReaders;
    private final BlockingDeque<Event> events;

    public StreamManager() {
        streamReaders = new HashMap<>();
        events = StreamReader.events;
        for (String s : Stream.getAllStreams().keySet()) {
            streamReaders.put(s, null);
        }
    }

    public static StreamManager fromCOREFile(BufferedReader streamFile) throws IOException {
        StreamManager sm = new StreamManager();
        String line;
        String[] streamData;
        StreamType type;
        while ((line = streamFile.readLine()) != null) {
            /*
             * Here we assume each line of the stream data file is of the form
             * NAME:TYPE:SOURCE
             */
            streamData = line.split(":", 3);
            switch (streamData[1]) {
                case "FILE":
                    type = StreamType.FILE;
                    break;
                case "CSV":
                    type = StreamType.CSV;
                    break;
                case "SOCK":
                    type = StreamType.SOCKET;
                    break;
                case "APICSV":
                    type = StreamType.APICSV;
                    break;
                default:
                    throw new Error("Stream type " + streamData[1] + " not supported.");
            }
            sm.add(streamData[2], type, Stream.getSchemaFor(streamData[0]));
        }
        return sm;
    }

    public void add(String source, StreamType type, Stream metadata) {
        String streamName = metadata.getName();
        if (!streamReaders.containsKey(streamName)) {
            throw new Error("Stream" + streamName + "is not defined in query.");
        }
        if (streamReaders.get(streamName) != null) {
            throw new Error("Source for stream" + streamName + "already declared.");
        }
        StreamReader sr;
        switch (type) {
            case FILE:
                sr = new FileStreamReader(source, streamName);
                break;
            case CSV:
                sr = new CSVStreamReader(source, streamName);
                break;
            case SOCKET:
                sr = new SocketStreamReader(source, streamName);
                break;
            case APICSV:
                sr = new APICSVStreamReader(source, streamName);
                break;
            default:
                throw new Error("Stream type " + type.toString() + " not supported.");
        }
        streamReaders.put(streamName, sr);
    }

    public Event nextEvent() throws InterruptedException {
        if (!events.isEmpty()) {
            return events.poll();
        }

        for (StreamReader sr : streamReaders.values()) {
            if (sr.isAlive()) {
                if (sr.type == StreamType.SOCKET) {
                    return events.poll(1, TimeUnit.HOURS);
                } else {
                    return events.poll(10000, TimeUnit.MICROSECONDS);
                }
            }
        }
        return null;
    }

    public void start() {
        for (Map.Entry<String, StreamReader> entry : streamReaders.entrySet()) {
            if (entry.getValue() == null) {
                throw new Error("Missing stream source for stream " + entry.getKey());
            }
        }

        BaseEngine.LOGGER.info("Starting up stream readers");
        for (StreamReader sr : streamReaders.values()) {
            sr.start();
        }
    }

    public void interrupt() {
        for (StreamReader sr : streamReaders.values()) {
            sr.interrupt();
        }
    }

    public boolean isReady() {
        for (StreamReader sr : streamReaders.values()) {
            if (!sr.isReady()) {
                return false;
            }
        }
        return true;
    }

    public void stopReaders() {
        for (StreamReader sr : streamReaders.values()) {
            sr.stopReader();
        }
    }
}