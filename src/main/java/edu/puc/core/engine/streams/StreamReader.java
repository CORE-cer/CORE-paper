package edu.puc.core.engine.streams;


import edu.puc.core.engine.BaseEngine;
import edu.puc.core.runtime.events.Event;
import edu.puc.core.runtime.events.EventParser;
import edu.puc.core.util.CSVParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

abstract class StreamReader extends Thread {
    String source;
    String streamName;
    protected StreamType type;
    private boolean stop = false;
    protected boolean ready = false;

    StreamReader(String source, String streamName) {
        this.source = source;
        this.streamName = streamName;
    }

    void readFromBufferedReader(BufferedReader stream) throws IOException {
        String line;
        Event e;

        while ((line = stream.readLine()) != null) {
            if (stop) {
                break;
            }
            /* We need the lock here to ensure that timestamps in the events
             * queue are non-decreasing. */
            synchronized (events) {
                try {
                    e = EventParser.parseEvent(line, streamName);
                    if (e != null) {
                            events.add(e);
                        }
                } catch (NumberFormatException exp) {
                    BaseEngine.LOGGER.warning("Failed to parse event" + line);
                }
            }
        }
    }

    void readFromCSVParser(CSVParser stream) throws IOException {
        List<String> line;
        Event e;
        long timeout;
        long lastTimestamp = -1;

        while ((line = stream.parseLine()) != null) {
            if (stop) {
                break;
            }
            try {
                e = stream.getEventFromCSVLine(line, streamName);

                if (e == null) {
                    continue;
                }

                if (! BaseEngine.fastRun) {
                    // Wait for next event
                    timeout = lastTimestamp == -1 ? 0 : e.getTimestamp() - lastTimestamp;
                    lastTimestamp = e.getTimestamp();

                    Thread.sleep(timeout);
                } else {
                    Thread.sleep(30);
                }

                synchronized (events) {
                    events.add(e);
                }

            } catch (NumberFormatException | InterruptedException exp) {
                BaseEngine.LOGGER.warning("Failed to parse event" + line);
            }
        }
    }

    protected void stopReader() {
        stop = true;
    }

    public boolean isReady() {
        return ready;
    }

    static final BlockingDeque<Event> events = new LinkedBlockingDeque<>();
}
