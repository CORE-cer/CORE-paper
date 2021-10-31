package edu.puc.core.parser.exceptions;

import edu.puc.core.parser.BaseParser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.ParseCancellationException;

public class ParserException extends ParseCancellationException {
    private String positionInfo;

    public ParserException(String msg, int lineNumber, int columnNumber) {
        super(msg);
        setPositionInfo(lineNumber, columnNumber);
    }

    public ParserException(String msg, ParserRuleContext context) {
        super(msg);
        if (context == null) {
            positionInfo = "";
        } else {
            int lineNumber = context.start.getLine();
            int columnNumber = context.start.getCharPositionInLine();
            setPositionInfo(lineNumber, columnNumber);
        }
    }

    private void setPositionInfo(int lineNumber, int columnNumber) {
        StringBuilder stringBuilder = new StringBuilder("line ")
                .append(lineNumber)
                .append(",\n  ")
                .append(BaseParser.getLatestInput(lineNumber))
                .append("\n  ");

        for (int c = 0; c < columnNumber; c++) {
            stringBuilder.append(' ');
        }
        stringBuilder.append("^\n");
        positionInfo = stringBuilder.toString();
    }

    @Override
    public String toString() {
        return positionInfo + this.getClass().getSimpleName() + ": " + getMessage();
    }

}
