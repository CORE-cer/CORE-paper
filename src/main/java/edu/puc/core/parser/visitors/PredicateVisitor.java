package edu.puc.core.parser.visitors;

import edu.puc.core.parser.COREBaseVisitor;
import edu.puc.core.parser.COREParser;
import edu.puc.core.parser.exceptions.*;
import edu.puc.core.parser.plan.Label;
import edu.puc.core.parser.plan.exceptions.PredicateException;
import edu.puc.core.parser.plan.predicate.*;
import edu.puc.core.parser.plan.values.*;
import edu.puc.core.util.StringUtils;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.Collection;
import java.util.stream.Collectors;


class PredicateVisitor extends COREBaseVisitor<AtomicPredicate> {

    private final Label label;

    PredicateVisitor(Label label) {
        this.label = label;
    }

    private void ensureValidity(AtomicPredicate atomicPredicate, ParserRuleContext context) {
        if (atomicPredicate.isConstant()) {
            throw new ValueException("Expression must have at least one attribute", context);
        }
    }

    @Override
    public AtomicPredicate visitNot_expr(COREParser.Not_exprContext ctx) {
        AtomicPredicate innerPredicate = ctx.bool_expr().accept(this);
        try{
            return innerPredicate.negate();
        }
         catch (PredicateException ex) {
            throw new InvalidStatementException("Can't negate this statement", ctx);
         }
    }


    @Override
    public AtomicPredicate visitAnd_expr(COREParser.And_exprContext ctx) {
        AtomicPredicate left = ctx.bool_expr(0).accept(this);
        AtomicPredicate right = ctx.bool_expr(1).accept(this);
        return new AndPredicate(left, right);
    }

    @Override
    public AtomicPredicate visitPar_bool_expr(COREParser.Par_bool_exprContext ctx) {
        // just ignore parenthesis
        return ctx.bool_expr().accept(this);
    }


    private Collection<Literal> parseNumberSeq(COREParser.Number_seqContext ctx) {
        if (ctx instanceof COREParser.Number_listContext) {
            // parse all numbers as number constants
            return ((COREParser.Number_listContext) ctx).number()
                    .stream()
                    .map(numberContext -> (Literal) new NumberLiteral(numberContext.getText()))
                    .collect(Collectors.toList());
        }
        // TODO: range containment filters
//        else if (ctx instanceof COREParser.Number_rangeContext){
//
//        }
        throw new UnknownStatementException("This type of number sequence has not been implemented yet", ctx);
    }

    private Collection<Literal> parseStringSeq(COREParser.String_seqContext ctx) {
        // parse all numbers as number constants
        return ctx.string()
                .stream()
                .map(stringContext -> (Literal) new StringLiteral(stringContext.getText()))
                .collect(Collectors.toList());
    }

    @Override
    public AtomicPredicate visitContainment_expr(COREParser.Containment_exprContext ctx) {
        Attribute attribute = getAttributeForName(ctx.attribute_name());
        LogicalOperation operation = ctx.K_NOT() != null ? LogicalOperation.NOT_IN : LogicalOperation.IN;
        Collection<Literal> literals;

        if (ctx.value_seq().number_seq() != null) {

            if (!attribute.getTypes().contains(ValueType.NUMERIC)) {
                throw new TypeException("Attribute `" + attribute.getName() +
                        "` is not comparable with numeric values", ctx);
            }

            literals = parseNumberSeq(ctx.value_seq().number_seq());

        } else if (ctx.value_seq().string_seq() != null) {
            if (!attribute.getTypes().contains(ValueType.STRING)) {
                throw new TypeException("Attribute `" + attribute.getName() +
                        "` is not comparable with string values", ctx);
            }

            literals = parseStringSeq(ctx.value_seq().string_seq());


        } else {
            throw new UnknownStatementException("Unknown sequence type", ctx);
        }

        AtomicPredicate predicate;

        try {
            predicate = new ContainmentPredicate(attribute, operation, literals);
        }
        catch (PredicateException err){
            throw new Error("FATAL ERROR: THIS SHOULDN'T HAPPEN");
        }

        ensureValidity(predicate, ctx);
        return predicate;
    }


    @Override
    public AtomicPredicate visitInequality_expr(COREParser.Inequality_exprContext ctx) {
        MathExprVisitor mathExprVisitor = new MathExprVisitor(label);

        Value lhs = ctx.math_expr(0).accept(mathExprVisitor);
        Value rhs = ctx.math_expr(1).accept(mathExprVisitor);
        LogicalOperation logicalOperation;

        if (ctx.GE() != null) {
            logicalOperation = LogicalOperation.GREATER;
        } else if (ctx.GEQ() != null) {
            logicalOperation = LogicalOperation.GREATER_EQUALS;
        } else if (ctx.LEQ() != null) {
            logicalOperation = LogicalOperation.LESS_EQUALS;
        } else if (ctx.LE() != null) {
            logicalOperation = LogicalOperation.LESS;
        } else {
            throw new UnknownStatementException("Unknown inequality type", ctx);
        }
        AtomicPredicate predicate = new InequalityPredicate(lhs, logicalOperation, rhs);
        ensureValidity(predicate, ctx);
        return predicate;
    }


    @Override
    public AtomicPredicate visitOr_expr(COREParser.Or_exprContext ctx) {
        AtomicPredicate left = ctx.bool_expr(0).accept(this);
        AtomicPredicate right = ctx.bool_expr(1).accept(this);
        return new OrPredicate(left, right);
    }


    private EqualityPredicate getEqualityPredicate(
            TerminalNode eq,
            TerminalNode neq,
            Value left,
            Value right,
            ParserRuleContext ctx) {
        try {
            if (eq != null) {
                return new EqualityPredicate(left, LogicalOperation.EQUALS, right);
            } else if (neq != null) {
                return new EqualityPredicate(left, LogicalOperation.NOT_EQUALS, right);
            } else {
                throw new UnknownStatementException("Unknown inequality type", ctx);
            }
        }
        catch (PredicateException err){
            throw new Error("FATAL ERROR: THIS SHOULDN'T HAPPEN");
        }
    }


    @Override
    public AtomicPredicate visitEquality_math_expr(COREParser.Equality_math_exprContext ctx) {

        MathExprVisitor visitor = new MathExprVisitor(label);

        Value left = ctx.math_expr(0).accept(visitor);
        Value right = ctx.math_expr(1).accept(visitor);

        AtomicPredicate predicate = getEqualityPredicate(ctx.EQ(), ctx.NEQ(), left, right, ctx);
        ensureValidity(predicate, ctx);
        return predicate;
    }

    @Override
    public AtomicPredicate visitEquality_string_expr(COREParser.Equality_string_exprContext ctx) {
        Attribute attribute;
        StringLiteral stringLiteral;

        COREParser.String_literalContext left = ctx.string_literal();
        COREParser.String_literal_or_regexpContext right = ctx.string_literal_or_regexp();

        if (right.REGEXP() != null) {
            throw new ParserException("Regexp matching not implemented yet", ctx);
        }

        if (left.attribute_name() != null) {
            attribute = getAttributeForName(left.attribute_name());
            stringLiteral = new StringLiteral(right.getText());
        } else {
            stringLiteral = new StringLiteral(left.getText());
            attribute = getAttributeForName(right.attribute_name());
        }

        AtomicPredicate predicate = getEqualityPredicate(ctx.EQ(), ctx.NEQ(), attribute, stringLiteral, ctx);
        ensureValidity(predicate, ctx);
        return predicate;
    }

    @Override
    public AtomicPredicate visitRegex_expr(COREParser.Regex_exprContext ctx) {
        Attribute attribute = getAttributeForName(ctx.attribute_name());
        StringLiteral regex = new StringLiteral(ctx.REGEXP().getText());
        return new LikePredicate(attribute, regex);
    }

    private Attribute getAttributeForName(COREParser.Attribute_nameContext ctx) {
        String attributeName = StringUtils.tryRemoveQuotes(ctx.getText());
        if (!label.getAttributes().containsKey(attributeName)) {
            throw new ValueException("Attribute `" + attributeName + "` is not defined on label " + label.getName(),
                    ctx);
        }
        return new Attribute(attributeName, label);
    }
}

