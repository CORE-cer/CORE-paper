package edu.puc.core;

import edu.puc.core.engine.BaseEngine;
import edu.puc.core.engine.Engine;
import edu.puc.core.engine.executors.ExecutorManager;
import edu.puc.core.engine.streams.StreamManager;
import edu.puc.core.runtime.events.Event;

import edu.puc.core.util.StringUtils;
import org.apache.commons.cli.*;

import java.io.BufferedReader;
import java.util.logging.Level;


public class Main {
    static Engine engine;

    public static void main(String[] args) throws Exception {
        CommandLine cmd = parseArgs(args);

        BaseEngine.LOGGER.setLevel(cmd.hasOption("v") ? Level.INFO: Level.OFF);
        BaseEngine.LOGGER.info("Running on verbose mode");

        BufferedReader queryFile = StringUtils.getReader(cmd.getOptionValue("q"));
        BufferedReader streamsFile = StringUtils.getReader(cmd.getOptionValue("s"));

        /*
         * QUERYFILE
         * Just 2 lines:
         * Line 1: Event and Stream Declarations source
         * Line 2: Query source
         *
         * Syntax: SOURCE_TYPE[:SOURCE_ADDRESS]
         *
         * STREAMSFILE
         * Arbitrary number of lines:
         * Line n: Stream_n source
         *
         * Syntax: STREAM_NAME:SOURCE_TYPE:SOURCE_ADDRESS
         */

        /* Create executor manager */
        ExecutorManager executorManager = ExecutorManager.fromCOREFile(queryFile);

        /* Create stream manager */
        StreamManager streamManager = StreamManager.fromCOREFile(streamsFile);

        // Parse Event and Stream declarations, initialize Engine
        engine = BaseEngine.newEngine(
                executorManager,
                streamManager,
                cmd.hasOption("logmetrics"),
                cmd.hasOption("fastrun"),
                cmd.hasOption("offline")
        );

        Event e;

        /* Start initial queries and reading the streams */
        engine.start();

        /* Send events to the engine */
        while ((e = engine.nextEvent()) != null) {
            BaseEngine.LOGGER.info("Event sent: " + e.toString());
            engine.sendEvent(e);
        }

        BaseEngine.LOGGER.info("No more events. Exiting.");
    }

    private static CommandLine parseArgs(String[] args) {
        Options options = new Options();

        Option fastrunOption = new Option("f", "fastrun", false, "ignore timestamps read from CSV");
        options.addOption(fastrunOption);

        Option logmetricsOption = new Option("l", "logmetrics", false, "log time and memory usage metrics");
        options.addOption(logmetricsOption);

        Option verboseOption = new Option("v", "verbose", false, "verbose mode");
        options.addOption(verboseOption);

        Option offlineOption = new Option("o", "offline", false, "offline mode, don't start RMI server");
        options.addOption(offlineOption);

        Option queryfileOption = new Option("q", "queryfile", true, "queryfile file path");
        queryfileOption.setRequired(true);
        options.addOption(queryfileOption);

        Option streamsfileOption = new Option("s", "streamsfile", true, "streamsfile file path");
        streamsfileOption.setRequired(true);
        options.addOption(streamsfileOption);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;

        try {
            cmd = parser.parse(options, args);
            return cmd;
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("./start_server.sh [-vflo] -q <queryfile path> -s <streamsfile path>", options);

            System.exit(1);
            return null;
        }
    }
}