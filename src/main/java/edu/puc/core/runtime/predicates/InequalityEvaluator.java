package edu.puc.core.runtime.predicates;


import edu.puc.core.parser.plan.predicate.InequalityPredicate;
import edu.puc.core.parser.plan.predicate.LogicalOperation;
import edu.puc.core.parser.plan.values.ValueType;
import edu.puc.core.runtime.events.Event;

public class InequalityEvaluator extends PredicateEvaluator {

    private final ValueEvaluator left;
    private final ValueEvaluator right;
    private final LogicalOperation operation;
    private final ValueType valueType;


    public InequalityEvaluator(InequalityPredicate predicate){
        left = ValueEvaluator.getEvaluatorForValue(predicate.getLeft());
        if (predicate.getLeft().interoperableWith(ValueType.NUMERIC)){
            valueType = ValueType.NUMERIC;
        }
        else if (predicate.getLeft().interoperableWith(ValueType.STRING)){
            valueType = ValueType.STRING;
        }
        else {
            throw new Error("invalid operation for types " + predicate.getLeft().getTypes());
        }
        right = ValueEvaluator.getEvaluatorForValue(predicate.getRight());
        operation =  predicate.getLogicalOperation();
    }

    private double doubleFromObj(Object value){
        if (value instanceof Long){
            return (double)(Long)value;
        }
        if (value instanceof Integer){
            return (double)(Integer)value;
        }
        if (value instanceof Double){
            return (double)(Double)value;
        }
        return (double)value;
    }

    @Override
    public boolean eval(Event event) {
        if (valueType == ValueType.NUMERIC){
            Object leftObj = left.eval(event);
            Object rightObj = right.eval(event);
            if (leftObj == null || rightObj == null) {
                return false;
            }
            double leftValue = doubleFromObj(leftObj);
            double rightValue = doubleFromObj(rightObj);
            if (operation == LogicalOperation.LESS ){
                return leftValue < rightValue;
            }
            else if (operation == LogicalOperation.LESS_EQUALS) {
                return leftValue <= rightValue;

            }
            else if (operation == LogicalOperation.GREATER_EQUALS) {
                return leftValue >= rightValue;

            }
            else if (operation == LogicalOperation.GREATER) {
                return leftValue > rightValue;
            }
        }
        else if (valueType == ValueType.STRING){
            String leftValue = (String) left.eval(event);
            String rightValue = (String) right.eval(event);
            int comparisonValue = leftValue.compareTo(rightValue);

            if (operation == LogicalOperation.LESS ){
                return comparisonValue < 0;
            }
            else if (operation == LogicalOperation.LESS_EQUALS) {
                return comparisonValue <= 0;

            }
            else if (operation == LogicalOperation.GREATER_EQUALS) {
                return comparisonValue >= 0;
            }
            else if (operation == LogicalOperation.GREATER) {
                return comparisonValue > 0;
            }
        }

        throw new Error("unknown operation or type");

    }
}

