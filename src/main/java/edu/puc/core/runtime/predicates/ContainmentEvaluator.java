package edu.puc.core.runtime.predicates;


import edu.puc.core.parser.plan.predicate.ContainmentPredicate;
import edu.puc.core.parser.plan.predicate.LogicalOperation;
import edu.puc.core.parser.plan.values.Attribute;
import edu.puc.core.parser.plan.values.Literal;
import edu.puc.core.parser.plan.values.ValueType;
import edu.puc.core.runtime.events.Event;
import javafx.util.Pair;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


public class ContainmentEvaluator extends PredicateEvaluator {

    private final int[] eventToIdxArray;
    private final Object[] values;
    private final boolean isExclusionPredicate;

    public ContainmentEvaluator(ContainmentPredicate predicate){
        Attribute attribute = (Attribute) predicate.getValue();
        Set<edu.puc.core.parser.plan.Event> possibleEvents = attribute.getLabel().getEvents();

        eventToIdxArray = new int[edu.puc.core.parser.plan.Event.count()];

        possibleEvents.forEach(event -> {
            List<Pair<String, ValueType>> attributes = event.getAttributes();

            for (int idx = 0; idx < attributes.size(); idx++) {
                if (attributes.get(idx).getKey().equals(attribute.getName())){
                    eventToIdxArray[event.getEventType()] = idx;
                    break;
                }
            }
        });

        isExclusionPredicate = predicate.getLogicalOperation() == LogicalOperation.NOT_IN;

        values = predicate.getValueCollection().stream().map(Literal::getValue).collect(Collectors.toList()).toArray(new Object[]{});
    }

    @Override
    public boolean eval(Event event) {
        for (Object value : values) {
            if (event.getValue(eventToIdxArray[event.getType()]).equals(value)) return !isExclusionPredicate;
        }
        return isExclusionPredicate;
    }
}
