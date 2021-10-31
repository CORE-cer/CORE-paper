package edu.puc.core.engine.streams;

import edu.puc.core.util.CSVParser;

import java.io.IOException;

public class CSVStreamReader extends StreamReader {
    CSVStreamReader(String source, String streamName) {
        super(source, streamName);
        type = StreamType.CSV;
    }

    @Override
    public void run() {
        try {
            CSVParser csvParser = new CSVParser(source, ";");
            readFromCSVParser(csvParser);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
