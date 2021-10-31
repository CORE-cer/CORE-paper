package edu.puc.core.parser.exceptions;

import org.antlr.v4.runtime.ParserRuleContext;

public class UnknownNameException extends ParserException {
    public UnknownNameException(String msg, ParserRuleContext context) {
        super(msg, context);
    }
}
