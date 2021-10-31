package edu.puc.core.runtime.predicates;

import edu.puc.core.parser.plan.values.ValueType;
import edu.puc.core.parser.plan.values.operations.*;
import edu.puc.core.runtime.events.Event;

import java.util.function.Function;

public abstract class OperationEvaluator extends ValueEvaluator {
    protected Operation operation;

    private OperationEvaluator(Operation operation){
        this.operation = operation;
    }

    /**
     * Gets the appropriate evaluator function for the provided {@link Operation},
     * builds the {@link OperationEvaluator} and returns it after overriding
     * its {@link #eval(Event) eval} function, defining it to use the beforementioned
     * evaluator function
     * 
     * @param operation {@link Operation} for which to build the 
     * {@link OperationEvaluator}.
     * @return The {@link OperationEvaluator}.
     */
    static OperationEvaluator fromOperation(Operation operation){
        Function<Event, Object> evaluator = getOperationFunction(operation);
        return new OperationEvaluator(operation) {
            @Override
            public Object eval(Event event) {
                return evaluator.apply(event);
            }
        };
    }

    private static Function<Event, Object> getOperationFunction(Operation operation){
        if (operation instanceof Addition){
            Addition op = (Addition)operation;
            ValueEvaluator leftEvaluator = ValueEvaluator.getEvaluatorForValue(op.getLhs());
            ValueEvaluator rightEvaluator = ValueEvaluator.getEvaluatorForValue(op.getRhs());
            if (op.interoperableWith(ValueType.NUMERIC)){
                return event -> (Double)leftEvaluator.eval(event) + (Double)rightEvaluator.eval(event);
            }
            else {
                return event -> (String)leftEvaluator.eval(event) + rightEvaluator.eval(event);
            }
        }
        if (operation instanceof Subtraction){
            Subtraction op = (Subtraction)operation;
            ValueEvaluator leftEvaluator = ValueEvaluator.getEvaluatorForValue(op.getLhs());
            ValueEvaluator rightEvaluator = ValueEvaluator.getEvaluatorForValue(op.getRhs());
            if (op.interoperableWith(ValueType.NUMERIC)){
                return event -> (Double)leftEvaluator.eval(event) - (Double)rightEvaluator.eval(event);
            }
            else {
                throw new Error("This is invalid");
            }
        }
        if (operation instanceof Division){
            Division op = (Division)operation;
            ValueEvaluator leftEvaluator = ValueEvaluator.getEvaluatorForValue(op.getLhs());
            ValueEvaluator rightEvaluator = ValueEvaluator.getEvaluatorForValue(op.getRhs());
            if (op.interoperableWith(ValueType.NUMERIC)){
                return event -> (Double)leftEvaluator.eval(event) / (Double)rightEvaluator.eval(event);
            }
            else {
                throw new Error("This is invalid");
            }
        }
        if (operation instanceof Multiplication){
            Multiplication op = (Multiplication)operation;
            ValueEvaluator leftEvaluator = ValueEvaluator.getEvaluatorForValue(op.getLhs());
            ValueEvaluator rightEvaluator = ValueEvaluator.getEvaluatorForValue(op.getRhs());
            if (op.interoperableWith(ValueType.NUMERIC)){
                return event -> (Double)leftEvaluator.eval(event) * (Double)rightEvaluator.eval(event);
            }
            else {
                throw new Error("This is invalid");
            }
        }
        if (operation instanceof Modulo){
            Modulo op = (Modulo)operation;
            ValueEvaluator leftEvaluator = ValueEvaluator.getEvaluatorForValue(op.getLhs());
            ValueEvaluator rightEvaluator = ValueEvaluator.getEvaluatorForValue(op.getRhs());
            if (op.interoperableWith(ValueType.NUMERIC)){
                return event -> (Double)leftEvaluator.eval(event) % (Double)rightEvaluator.eval(event);
            }
            else {
                throw new Error("This is invalid");
            }
        }
        if (operation instanceof Negation){
            Negation op = (Negation)operation;
            ValueEvaluator valueEvaluator = ValueEvaluator.getEvaluatorForValue(op.getInner());
            if (op.interoperableWith(ValueType.NUMERIC)){
                return event -> -(Double)valueEvaluator.eval(event);
            }
            else {
                throw new Error("This is invalid");
            }
        }
        throw new Error("Unknown operation!");
    }

}

