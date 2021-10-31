package edu.puc.core.execution.watcher;


import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

public class ExecutorWatcher {
    private int triggers;
    private long lastTriggerTime = System.currentTimeMillis();
    private long totalTriggerTime;
    private long maxTriggerTime;

    public void update() {
        triggers++;
        long timeSinceLastTrigger = getTimeSinceLastTrigger();
        lastTriggerTime = System.currentTimeMillis();
        totalTriggerTime += timeSinceLastTrigger;
        maxTriggerTime = getMaxTriggerTime(timeSinceLastTrigger);
    }

    public int getTriggers() { return triggers; }

    public long getTimeSinceLastTrigger() { return System.currentTimeMillis() - lastTriggerTime; }

    public long getAvgTriggerTime(long timeSinceLastTrigger) {
        return (totalTriggerTime + timeSinceLastTrigger) / (triggers + 1);
    }

    public long getMaxTriggerTime(long timeSinceLastTrigger) { return Math.max(maxTriggerTime, timeSinceLastTrigger); }

    public List<Long> getTriggerInfo() {
        long timeSinceLastTrigger = getTimeSinceLastTrigger();
        Long[] stats = new Long[] {(long) triggers,
                timeSinceLastTrigger,
                getAvgTriggerTime(timeSinceLastTrigger),
                getMaxTriggerTime(timeSinceLastTrigger)};

        return Arrays.asList(stats);
    }

    public String toString() {
        return String.format("Triggers: %d\n", triggers)
                .concat(String.format("Time since last trigger: %d s\n", getTimeSinceLastTrigger() / 1000));
    }

    public JSONObject toJSON() {
        JSONObject out = new JSONObject();
        List<Long> stats = getTriggerInfo();

        out.put("triggers", stats.get(0));
        out.put("time_since_last_trigger", stats.get(1));
        out.put("avg_trigger_time", stats.get(2));
        out.put("max_trigger_time", stats.get(3));

        return out;
    }
}
