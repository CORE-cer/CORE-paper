package edu.puc.core.engine.streams;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class FileStreamReader extends StreamReader {


    FileStreamReader(String source, String streamName) {
        super(source, streamName);
        type = StreamType.FILE;
    }

    @Override
    public void run() {
        try {
            FileReader file = new FileReader(source);
            BufferedReader stream = new BufferedReader(file);

            readFromBufferedReader(stream);

        } catch (IOException e) {
            e.printStackTrace();
        }
        ready = true;
    }
}
