package edu.puc.core.runtime.predicates;

import edu.puc.core.parser.plan.values.Attribute;
import edu.puc.core.parser.plan.values.Literal;
import edu.puc.core.parser.plan.values.Value;
import edu.puc.core.parser.plan.values.operations.Operation;
import edu.puc.core.runtime.events.Event;

public abstract class ValueEvaluator {

    /**
     * Analizes which kind of {@link Value} is to be evaluated and returns the
     * corresponding subclass of {@link ValueEvaluator}.
     * 
     * @param value {@link Value} to be evaluated.
     * @return The corresponding subtype of {@link ValueEvaluator} built
     * for the subtype of {@link Value} provided.
     */
    public static ValueEvaluator getEvaluatorForValue(Value value){
        if (value instanceof Attribute){
            return new AttributeEvaluator((Attribute) value);
        }
        else if (value instanceof Literal){
            return new LiteralEvaluator((Literal) value);
        }
        else if (value instanceof Operation){
            return OperationEvaluator.fromOperation((Operation) value);
        }
        else {
            throw new Error("Unknown value type");
        }
    }


    public abstract Object eval(Event event);
}
