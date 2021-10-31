package edu.puc.core.parser.exceptions;

import org.antlr.v4.runtime.ParserRuleContext;

public class DuplicateNameException extends ParserException {
    public DuplicateNameException(String msg, ParserRuleContext context) {
        super(msg, context);
    }
}
