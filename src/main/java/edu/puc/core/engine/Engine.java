package edu.puc.core.engine;

import edu.puc.core.engine.executors.ExecutorManager;
import edu.puc.core.engine.streams.StreamManager;
import edu.puc.core.execution.BaseExecutor;
import edu.puc.core.execution.structures.output.CDSComplexEventGrouping;
import edu.puc.core.runtime.events.Event;

import java.util.Map;
import java.util.function.Consumer;


public class Engine extends BaseEngine {

    public Engine(ExecutorManager executorManager, StreamManager streamManager) {
        this.executorManager = executorManager;
        this.streamManager = streamManager;
        // Use the default callback
    }

    public Engine(ExecutorManager executorManager, StreamManager streamManager, Consumer<CDSComplexEventGrouping> matchCallback) {
        this(executorManager, streamManager);
        setMatchCallback(matchCallback);
    }

    @Override
    public void sendEvent(Event e) {
        for (BaseExecutor executor : getExecutors().values()) {
            executor.sendEvent(e);
        }
    }

    @Override
    public void setMatchCallback(Consumer<CDSComplexEventGrouping> callback) {
        executorManager.setDefaultMatchCallback(callback);
    }

    @Override
    public void start() {
        executorManager.start();
        streamManager.start();
    }

    @Override
    public Event nextEvent() throws InterruptedException {
        return streamManager.nextEvent();
    }

    @Override
    public Map<String, BaseExecutor> getExecutors() {
        return executorManager.getExecutors();
    }

}
