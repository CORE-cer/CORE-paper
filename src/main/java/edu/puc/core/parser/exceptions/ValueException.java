package edu.puc.core.parser.exceptions;

import org.antlr.v4.runtime.ParserRuleContext;

public class ValueException extends ParserException {
    public ValueException(String msg, ParserRuleContext context) {
        super(msg, context);
    }
}