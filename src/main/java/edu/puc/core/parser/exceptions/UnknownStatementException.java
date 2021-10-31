package edu.puc.core.parser.exceptions;

import org.antlr.v4.runtime.ParserRuleContext;

public class UnknownStatementException extends ParserException {
    public UnknownStatementException(String msg, ParserRuleContext context) {
        super(msg, context);
    }
}
