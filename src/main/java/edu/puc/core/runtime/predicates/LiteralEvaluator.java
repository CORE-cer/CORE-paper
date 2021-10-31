package edu.puc.core.runtime.predicates;

import edu.puc.core.parser.plan.values.Literal;
import edu.puc.core.runtime.events.Event;

public class LiteralEvaluator extends ValueEvaluator {

    private final Object value;

    public LiteralEvaluator(Literal literal){
        value = literal.getValue();
    }

    @Override
    public Object eval(Event event){
        return value;
    }
}
