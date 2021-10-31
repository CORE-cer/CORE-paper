package edu.puc.core.runtime.events;

import edu.puc.core.parser.plan.values.ValueType;

public class EventParser {

//    private static AtomicInteger idx = new AtomicInteger(0);
    public static long parseTime = 0;
    public static long otherTime = 0;

    public static Event parseEvent(String line, String streamName) {
//        long t = System.nanoTime();
        String[] values = line.substring(0, line.length() - 1).split("\\(");
        String name = values[0];
        values = values[1].split(",");

        Event e;
        edu.puc.core.parser.plan.Event ev = edu.puc.core.parser.plan.Event.getAllEvents().get(name);

//        otherTime += System.nanoTime() - t;
//        t = System.nanoTime();

        Object[] attrs = new Object[ev.getAttributes().size()];
        for (int i = 0; i < ev.getAttributes().size(); i++) {
            String[] temp = values[i].split("=");
            String n = temp[0];
            String val = temp[1];
            ValueType valueType = ev.getAttributes().stream().filter(pair -> pair.getKey().equals(n)).findAny().get().getValue();
            if (valueType == ValueType.INTEGER) {
                attrs[i] = Integer.parseInt(val);
            } else if (valueType == ValueType.DOUBLE) {
                attrs[i] = Double.parseDouble(val);
            } else if (valueType == ValueType.LONG) {
                attrs[i] = Long.parseLong(val);
            } else if (valueType == ValueType.BOOLEAN) {
                attrs[i] = Boolean.parseBoolean(val);
            } else {
                attrs[i] = val;
            }
        }
        try {
            e = new Event(streamName, name, attrs);
//            parseTime += System.nanoTime() - t;
            return e;
        } catch (Exception err) {
            err.printStackTrace();
        }
        return null;
    }
}
