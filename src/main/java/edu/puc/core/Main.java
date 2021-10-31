package edu.puc.core;

import edu.puc.core.engine.BaseEngine;
import edu.puc.core.engine.Engine;
import edu.puc.core.engine.executors.ExecutorManager;
import edu.puc.core.engine.streams.StreamManager;
import edu.puc.core.execution.callback.MatchCallback;
import edu.puc.core.runtime.events.Event;
import edu.puc.core.runtime.profiling.Profiler;
import edu.puc.core.util.StringUtils;
import org.apache.commons.cli.*;

import java.io.BufferedReader;
import java.util.logging.Level;


public class Main {
    static Engine engine;
    static boolean memoryTest = false;

    public static void main(String[] args) throws Exception {
        long maxMemTotal = 0;
        long avgMemTotal = 0;
        long maxMemUsed = 0;
        long avgMemUsed = 0;
        int count = 0;
        long total = Runtime.getRuntime().totalMemory();
//        maxMemTotal = avgMemTotal = total;
//        System.gc();
        long used = total - Runtime.getRuntime().freeMemory();
//        maxMemUsed = avgMemUsed = used;

        CommandLine cmd = parseArgs(args);

        BaseEngine.LOGGER.setLevel(cmd.hasOption("v") ? Level.INFO: Level.OFF);
        BaseEngine.LOGGER.info("Running on verbose mode");

        BufferedReader queryFile = StringUtils.getReader(cmd.getOptionValue("q"));
        BufferedReader streamsFile = StringUtils.getReader(cmd.getOptionValue("s"));

        if (cmd.hasOption("m")) {
            memoryTest = Boolean.parseBoolean(cmd.getOptionValue("m"));
        }

        long numEvents = 0;
        if (cmd.hasOption("n")) {
            numEvents = Integer.parseInt(cmd.getOptionValue("n"));
        }

        long timeout = 0;
        if (cmd.hasOption("t")) {
            timeout = Integer.parseInt(cmd.getOptionValue("t"));
        }

        if (cmd.hasOption("i")) {
            MatchCallback.limit = Integer.parseInt(cmd.getOptionValue("i"));
        }

        boolean execTime = false;
        if (cmd.hasOption("e")) {
            execTime = Boolean.parseBoolean(cmd.getOptionValue("e"));
        }

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
        long compileStartTime = System.nanoTime();
        /* Create executor manager */
        ExecutorManager executorManager = ExecutorManager.fromCOREFile(queryFile);

        /* Create stream manager */
        StreamManager streamManager = StreamManager.fromCOREFile(streamsFile);

//        BufferedReader queryFile2 = StringUtils.getReader(cmd.getOptionValue("q"));
//        queryFile2.readLine();
//        String line = queryFile2.readLine();
//        String query = StringUtils.readFile(line.split(":")[1]);
//        BaseExecutor testExecutor = executorManager.newExecutor(query);

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
        Profiler.addCompileTime(System.nanoTime() - compileStartTime);
        if (!memoryTest) {
            while (!streamManager.isReady()) {
                Thread.sleep(1000);
            }
        }

        long events = 0;
        long totalTime = 0;
        /* Send events to the engine */
        long start = System.nanoTime();
        while (numEvents == 0 || events <= numEvents) {
            e = engine.nextEvent();
            if (e == null) {
                break;
            }
//            BaseEngine.LOGGER.info("Event sent: " + e.toString());
            engine.sendEvent(e);
            events++;
            if (memoryTest && events % 10000 == 0) {
                total = Runtime.getRuntime().totalMemory();
                avgMemTotal += total;
                if (total > maxMemTotal) {
                    maxMemTotal = total;
                }
                System.gc();
                used = total - Runtime.getRuntime().freeMemory();
                avgMemUsed += used;
                if (used > maxMemUsed) {
                    maxMemUsed = used;
                }
                count++;
            }
            if (timeout != 0 && (System.nanoTime() - start >= 1000000000 * timeout)) {
                totalTime = System.nanoTime() - start;
                break;
            }
        }

        streamManager.stopReaders();

        if (!memoryTest) {
            if (execTime) {
                System.out.print((double)(System.nanoTime() - start)/1000000000 + ",");
            }
            System.out.print(events + ",");
            Profiler.print();
            System.out.println();
        } else {
            System.out.print(maxMemTotal + ",");
            System.out.print(avgMemTotal/count + ",");
            System.out.print(maxMemUsed + ",");
            System.out.println(avgMemUsed/count);
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

        Option memoryTestOption = new Option("m", "memtest", true, "if memtest is enabled");
        options.addOption(memoryTestOption);

        Option numEventsOption = new Option("n", "numevents", true, "number of events to process");
        options.addOption(numEventsOption);

        Option timeOutOption = new Option("t", "timeout", true, "timeout in seconds");
        options.addOption(timeOutOption);

        Option limitOption = new Option("i", "limit", true, "output limit");
        options.addOption(limitOption);

        Option execOption = new Option("e", "exectime", true, "log execution time");
        options.addOption(execOption);

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
