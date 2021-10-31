package edu.puc.core.engine.streams;

import edu.puc.core.engine.BaseEngine;
import edu.puc.core.util.CSVParser;


public class APICSVStreamReader extends StreamReader {
    APICSVStreamReader(String source, String streamName) {
        super(source, streamName);
        type = StreamType.APICSV;
    }

    @Override
    public void run() {
        while (true) {
            try {
                CSVParser csvParser = new CSVParser(source, ";");

                while (! csvParser.ready()) {
                    csvParser.close();
                    BaseEngine.LOGGER.info("No new events.");
                    Thread.sleep(10000);
                    csvParser = new CSVParser(source, ";");
                }

                readFromCSVParser(csvParser);

                csvParser.closeAndErase();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
