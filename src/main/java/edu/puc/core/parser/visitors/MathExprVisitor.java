package edu.puc.core.parser.visitors;


import edu.puc.core.parser.COREBaseVisitor;
import edu.puc.core.parser.COREParser;
import edu.puc.core.parser.exceptions.UnknownNameException;
import edu.puc.core.parser.exceptions.UnknownStatementException;
import edu.puc.core.parser.exceptions.ValueException;
import edu.puc.core.parser.plan.Label;
import edu.puc.core.parser.plan.exceptions.IncompatibleValueException;
import edu.puc.core.parser.plan.values.Attribute;
import edu.puc.core.parser.plan.values.NumberLiteral;
import edu.puc.core.parser.plan.values.Value;
import edu.puc.core.parser.plan.values.ValueType;
import edu.puc.core.parser.plan.values.operations.*;
import edu.puc.core.util.StringUtils;
import org.antlr.v4.runtime.ParserRuleContext;

import java.util.Set;

class MathExprVisitor extends COREBaseVisitor<Value> {

    private Label label;
    private final boolean checkValidity;

    MathExprVisitor(){
        checkValidity = false;
    }

    MathExprVisitor(Label label) {
        checkValidity = true;
        this.label = label;
    }

    private void ensureTypes(Value value, ParserRuleContext context) {
        if (!value.interoperableWith(ValueType.NUMERIC)) {
            throw new ValueException("Can only perform math operations over numeric values", context);
        }
        ensureValidity(value, context);
    }

    private void ensureValidity(Value value, ParserRuleContext context) {
        if (!checkValidity) return;
        for (Attribute attribute : value.getAttributes()) {
            Set<String> attributeNames = label.getAttributes().keySet();
            if (!attributeNames.contains(attribute.getName())) {
                throw new UnknownNameException("Attribute `" + attribute.getName() +
                        "` is undefined for label " + label.getName(), context);
            }
        }
    }

    @Override
    public Value visitMul_math_expr(COREParser.Mul_math_exprContext ctx) {
        Value leftValue = ctx.math_expr(0).accept(this);
        if (leftValue == null) return null;
        ensureTypes(leftValue, ctx.math_expr(0));

        Value rightValue = ctx.math_expr(1).accept(this);
        if (rightValue == null) return null;
        ensureTypes(rightValue, ctx.math_expr(1));

        try {
            if (ctx.STAR() != null) {  // multiplication
                return new Multiplication(leftValue, rightValue);
            } else if (ctx.SLASH() != null) {  // division
                return new Division(leftValue, rightValue);
            } else if (ctx.PERCENT() != null) {  // modulo
                return new Modulo(leftValue, rightValue);
            }
            throw new UnknownStatementException("Unknown math operation", ctx);
        } catch (IncompatibleValueException err) {
            return null;
        }
    }


    @Override
    public Value visitSum_math_expr(COREParser.Sum_math_exprContext ctx) {
        Value leftValue = ctx.math_expr(0).accept(this);
        if (leftValue == null) return null;
        ensureTypes(leftValue, ctx.math_expr(0));

        Value rightValue = ctx.math_expr(1).accept(this);
        if (rightValue == null) return null;
        ensureTypes(rightValue, ctx.math_expr(1));

        try {
            if (ctx.MINUS() != null) {  // multiplication
                return new Subtraction(leftValue, rightValue);
            } else if (ctx.PLUS() != null) {  // division
                return new Addition(leftValue, rightValue);
            }
            throw new UnknownStatementException("Unknown math operation", ctx);
        } catch (IncompatibleValueException err) {
            return null;
        }
    }


    @Override
    public Value visitUnary_math_expr(COREParser.Unary_math_exprContext ctx) {
        Value value = ctx.math_expr().accept(this);
        if (value == null) return null;
        ensureTypes(value, ctx.math_expr());

        try {
            if (ctx.MINUS() != null) {
                return new Negation(value);
            }
        } catch (IncompatibleValueException err) {
            return null;
        }
        return value;
    }


    @Override
    public Value visitAttribute_math_expr(COREParser.Attribute_math_exprContext ctx) {
        String attributeName = StringUtils.tryRemoveQuotes(ctx.attribute_name().getText());
        if (!label.getAttributes().containsKey(attributeName)) {
            throw new UnknownNameException("Label " + label.getName() + " has no attribute of name `" +
                    attributeName + "`", ctx);
        }
        Value value = new Attribute(attributeName, label);
        ensureValidity(value, ctx);
        return value;
    }


    @Override
    public Value visitPar_math_expr(COREParser.Par_math_exprContext ctx) {

        // Just ignore the parenthesis

        return ctx.math_expr().accept(this);
    }

    @Override
    public Value visitNumber_math_expr(COREParser.Number_math_exprContext ctx) {
        return new NumberLiteral(ctx.number().getText());
    }
}
