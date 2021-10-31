package edu.puc.core.parser.exceptions;

import org.antlr.v4.runtime.ParserRuleContext;

public class MissingValueException extends ParserException {
    public MissingValueException(String msg, ParserRuleContext context) {
        super(msg, context);
    }
}