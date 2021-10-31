package edu.puc.core.engine.streams;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class SocketStreamReader extends StreamReader {

    /* TODO: DETECT DISCONNECTION */

    private Socket socket;

    SocketStreamReader(String source, String streamName) {
        super(source, streamName);
        type = StreamType.SOCKET;
        try {
            String host = source.split(":")[0];
            int port = Integer.parseInt(source.split(":")[1]);
            this.socket = new Socket(host, port);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("Caught exception" + e.toString() + "while attempting to read from stream" + streamName);
            System.out.println("Reading from" + streamName + "aborted");
        }
    }

    @Override
    public void run() {
        try {
            BufferedReader stream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            readFromBufferedReader(stream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
