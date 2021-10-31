package edu.puc.core.parser.exceptions;

import org.antlr.v4.runtime.ParserRuleContext;

public class InvalidStatementException extends ParserException {
    public InvalidStatementException(String msg, ParserRuleContext context) {
        super(msg, context);
    }
}
