package edu.puc.core.engine;

import edu.puc.core.engine.executors.ExecutorManager;
import edu.puc.core.engine.streams.StreamManager;
import edu.puc.core.execution.structures.output.CDSComplexEventGrouping;
import edu.puc.core.parser.plan.Event;
import edu.puc.core.parser.plan.Stream;
import edu.puc.core.util.DistributionConfiguration;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.junit.runners.Parameterized.Parameter;
import java.util.Arrays;
import java.util.Collection;


@RunWith(value = Parameterized.class)
public class EngineDeclaration {

    @Parameter(value = 0)
    public int numberOfTests; //This parameter depends on number of tests

    static Engine engine; //engine we will use on the test
    static List<String> outputs = new ArrayList<String>(); //outputs that will be test
    public static List<CDSComplexEventGrouping> allMatches = new ArrayList<>(); //match of engine or 'outputs' of the query
    public static long countTests; //How many tests are there

    static {
        try {
            countTests = Files.find(
                        Paths.get("./src/test/java/edu/puc/core/engine/test_files/"),
                        1,  // how deep do we want to descend
                        (path, attributes) -> attributes.isDirectory()
                        ).count() - 1; // '-1' because '/tmp' is also counted in
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Parameters(name = "number of folders of tests")
    public static Collection<Object[]> data() {
//        return Arrays.asList(new Object[][]{{1}}); // use if you would rather use only test 1
        Object[][] tests = new Object[(int) countTests][];
        for (int i=0; i < countTests; i++) {
            tests[i] = new Object[]{i+1};
        }
        return Arrays.asList(tests);
    }

    @Before
    public void createManagers() throws Exception {
        System.out.println("Test"+numberOfTests);
        /* First, we clear everything */
        Event.invalidateEventSchema();
        Stream.invalidateStreamsSchema();
        allMatches.clear();
        outputs.clear();

        /*
         * QUERYFILE
         * Just 2 lines:
         * Line 1: Event and Stream Declarations source of test
         * Line 2: Query source of test
         *
         * Syntax: SOURCE_TYPE[:SOURCE_ADDRESS]
         *
         * STREAMSFILE
         * Arbitrary number of lines with events:
         * Line n: Stream_n source
         *
         * Syntax: STREAM_NAME:SOURCE_TYPE:SOURCE_ADDRESS
         *
         * OUTPUTFILE
         * Arbitrary number of lines with:
         * Line 1: Number of events given on the streamFile
         * Line 2 and on: ouput of the query, not in a particular order
         */

        String queryFile = "./src/test/java/edu/puc/core/engine/test_files/Test" + numberOfTests + "/query_test.data";
        String streamFile = "./src/test/java/edu/puc/core/engine/test_files/Test" + numberOfTests + "/stream_test.data";
        ExecutorManager executorManager = ExecutorManager.fromCOREFile(getReader(queryFile), Optional.empty());
        StreamManager streamManager = StreamManager.fromCOREFile(getReader(streamFile));
        engine = BaseEngine.newEngine(executorManager, streamManager, false, true, true);

        BufferedReader outputFile = getReader("./src/test/java/edu/puc/core/engine/test_files/Test" + numberOfTests + "/output.txt");
        String output;
        while ((output = outputFile.readLine()) != null) {
            outputs.add(output);
        }
    }


    @Test
    public void allNumberOfEvents() throws InterruptedException, IOException, FileNotFoundException {
        int numberEvents = 0; // number of events given
        int realEvents = Integer.parseInt(outputs.get(0)); //number of events on outputFile line 1
        edu.puc.core.runtime.events.Event e;

        /* Start reading the streams */
        engine.start();
        Thread.sleep(500); // 500 ms is good enough to load the whole csv
        while ((e = engine.nextEvent()) != null) {
            BaseEngine.LOGGER.info("Event sent: " + e.toString());
            engine.sendEvent(e);
            numberEvents++; // we add one when an event is given to the engine
        }
        assertEquals("Not all events given to Engine for Test " + Integer.toString(numberOfTests) +
                ", should be " + Integer.toString(realEvents)  + "\n", realEvents, numberEvents);
        //checks that the events given to the engine are the same that it's supposed to receive given the outputFile
    }

    // FIXME
    // Test15 fails both in SimpleExecutor and TimeWindowsExecutor
    @Test
    public void correctOutput() throws InterruptedException {
        engine.setMatchCallback( matches -> {
            allMatches.add(matches);
        });

        engine.start();
        Thread.sleep(500); // wait until all queries have been parsed and all events have been read.
        edu.puc.core.runtime.events.Event e;
        while ((e = engine.nextEvent()) != null) {
            BaseEngine.LOGGER.info("Event sent: " + e.toString());
            engine.sendEvent(e);
        }

        // TODO
        // Parse a whole Event and compare the event
        Set<Double> expectedValues = new HashSet<>();
        for (int i = 1; i < outputs.size(); i++){
            String str = outputs.get(i);
            Double value = Double.parseDouble(str.split("=")[1]); // e.g. value=4.12
            expectedValues.add(value);
        }

        Set<Double> values = new HashSet<>();
        for(CDSComplexEventGrouping match : EngineDeclaration.allMatches) {
            match.forEach(m -> {
                m.forEach(event ->
                        values.add((Double) event.getValue("value"))
                );
            });
        }
        assertEquals("Not correct output for Test" + numberOfTests, expectedValues, values);
    }

    private static BufferedReader getReader(String filePath) throws FileNotFoundException {
        FileReader fr = new FileReader(filePath);
        return new BufferedReader(fr);
    }

}
