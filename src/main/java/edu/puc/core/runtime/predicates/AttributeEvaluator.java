package edu.puc.core.runtime.predicates;

import edu.puc.core.parser.plan.values.Attribute;
import edu.puc.core.parser.plan.values.ValueType;
import edu.puc.core.runtime.events.Event;
import javafx.util.Pair;

import java.util.Collection;
import java.util.List;

public class AttributeEvaluator extends ValueEvaluator {

    private final int[] eventToIdxArray;

    public AttributeEvaluator(Attribute attribute){

//        Set<edu.puc.core.parser.plan.Event> possibleEvents = attribute.getLabel().getEvents();
        Collection<edu.puc.core.parser.plan.Event> possibleEvents = edu.puc.core.parser.plan.Event.getAllEvents().values();
        eventToIdxArray = new int[edu.puc.core.parser.plan.Event.count()];

        possibleEvents.forEach(event -> {
            List<Pair<String, ValueType>> attributes = event.getAttributes();
            eventToIdxArray[event.getEventType()] = -1;
            for (int idx = 0; idx < attributes.size(); idx++) {
                if (attributes.get(idx).getKey().equals(attribute.getName())){
                    eventToIdxArray[event.getEventType()] = idx;
                    break;
                }
            }
        });
    }

    @Override
    public Object eval(Event event){
        int idx = eventToIdxArray[event.getType()];
        if (idx == -1) {
            return null;
        }
        return event.getValue(eventToIdxArray[event.getType()]);
    }
}
