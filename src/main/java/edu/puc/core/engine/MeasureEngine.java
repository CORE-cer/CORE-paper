package edu.puc.core.engine;


import edu.puc.core.engine.executors.ExecutorManager;
import edu.puc.core.engine.streams.StreamManager;
import edu.puc.core.execution.BaseExecutor;
import edu.puc.core.execution.structures.output.CDSComplexEventGrouping;
import edu.puc.core.runtime.events.Event;
import edu.puc.core.util.StringUtils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class MeasureEngine extends Engine {
    private final String baseDir = System.getProperty("user.dir") + "/files/measure/" + jvmId + "/";
    private final String timesFilePathTemplate = baseDir + "%s_times.csv";
    private final String memoryFilePath = baseDir + "memory.csv";
    private final String watcherFilePath = baseDir + "watcher.csv";

    private int eventsSent = 0;

    public MeasureEngine(ExecutorManager executorManager, StreamManager streamManager) {
        super(executorManager, streamManager);
    }

    public MeasureEngine(ExecutorManager executorManager, StreamManager streamManager, Consumer<CDSComplexEventGrouping> matchCallback) {
        super(executorManager, streamManager, matchCallback);
    }

    private void writeTimes(Map<String, Long> sendTimes) {
        // TODO: register times of occurrence too
        try {
            StringBuilder out = new StringBuilder();

            if (sendTimes.size() == 0) return;

            for (Map.Entry<String, Long> pair: sendTimes.entrySet()) {
                String name = pair.getKey();
                long time = pair.getValue();

                String timesFilePath = String.format(timesFilePathTemplate, name);
                BufferedWriter writer = StringUtils.getWriter(timesFilePath, true);
                // Each executor's update time
                out.append(time).append("\n");

                writer.write(String.valueOf(out));
                writer.close();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeMemory() {
        // TODO: register times of occurrence too
        try {
            BufferedWriter writer = StringUtils.getWriter(memoryFilePath, true);
            StringBuilder out = new StringBuilder();

            // Get the Java runtime
            Runtime runtime = Runtime.getRuntime();

            // Calculate the used memory
            long total = runtime.totalMemory() / 1024;
            long free = runtime.freeMemory() / 1024;

            out.append(total).append(",").append(total - free).append("\n");

            writer.write(String.valueOf(out));
            writer.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeWatchers() {
        try {
            BufferedWriter writer = StringUtils.getWriter(watcherFilePath);
            StringBuilder out = new StringBuilder("name,number,last_t,average_t,max_t\n");
            for (Map.Entry<String, BaseExecutor> pair : getExecutors().entrySet()) {
                String name = pair.getKey();
                BaseExecutor executor = pair.getValue();

                List<Long> stats = executor.getWatcher().getTriggerInfo();
                String statsString = stats.toString().replaceAll("[\\[\\] ]", "");
                out.append(name).append(",").append(statsString).append("\n");
            }
            writer.write(String.valueOf(out));
            writer.close();
        }
            catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendEvent(Event e) {
        Map<String, Long> sendTimes = new HashMap<>();
        long t0 = System.currentTimeMillis();
        long before;

        for (Map.Entry<String, BaseExecutor> pair : getExecutors().entrySet()) {
            String name = pair.getKey();
            BaseExecutor executor = pair.getValue();

            before = System.currentTimeMillis();
            executor.sendEvent(e);
            sendTimes.put(name, System.currentTimeMillis() - before);
        }

        sendTimes.put("total", System.currentTimeMillis() - t0);

        writeTimes(sendTimes);
        writeMemory();

        if (eventsSent++ % 25 == 0) writeWatchers();
    }
}
