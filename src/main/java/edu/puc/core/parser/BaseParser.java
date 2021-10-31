package edu.puc.core.parser;

import edu.puc.core.parser.exceptions.ParserException;
import edu.puc.core.parser.exceptions.UnknownStatementException;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.ParseCancellationException;

public abstract class BaseParser<T, P extends ParserRuleContext> {

    private static String input;

    public static String getLatestInput() {
        if (input == null) return "";
        return input;
    }

    public static String getLatestInput(int lineNumber) {
        if (input == null)
            return "";

        String[] lines = input.split("\n\r?");

        if (lineNumber > lines.length)
            return "";

        return lines[lineNumber - 1];
    }

    /**
     * Builds and returns the parser for the given source string.
     *
     * @param source String containing the CORE statement.
     * @return The parser for the given statement.
     */
    protected COREParser getParserForSource(String source) {
        input = source;
        CharStream charStream = CharStreams.fromString(source);
        CORELexer coreLexer = new CORELexer(charStream);
        CommonTokenStream commonTokenStream = new CommonTokenStream(coreLexer);
        COREParser coreParser = new COREParser(commonTokenStream);

        coreParser.removeErrorListeners();
        coreParser.addErrorListener(new BaseErrorListener() {
            @Override
            public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
                throw new ParserException(msg, line, charPositionInLine);
            }
        });

        return coreParser;
    }

    public abstract T parse(String source) throws ParseCancellationException;

    T compileContext(P context) throws UnknownStatementException {
        throw new Error("Must implement method");
    }
}
