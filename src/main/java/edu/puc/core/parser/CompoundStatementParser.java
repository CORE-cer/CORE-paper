package edu.puc.core.parser;

import edu.puc.core.parser.exceptions.UnknownStatementException;
import edu.puc.core.parser.plan.LogicalPlan;
import org.antlr.v4.runtime.misc.ParseCancellationException;

import java.util.ArrayList;
import java.util.List;

public class CompoundStatementParser extends BaseParser<List<LogicalPlan>, COREParser.ParseContext> {
    private final DeclarationParser declarationCompiler;
    private final QueryParser queryCompiler;

    public CompoundStatementParser(DeclarationParser declarationCompiler, QueryParser queryCompiler) {
        this.declarationCompiler = declarationCompiler;
        this.queryCompiler = queryCompiler;
    }

    /**
     * Parses the source statement and returns the list of Logical Plans.
     *
     * @param source String containing the CORE statement.
     * @return The list of Logical Plans for the given statement.
     * @throws ParseCancellationException
     */
    @Override
    public List<LogicalPlan> parse(String source) throws ParseCancellationException {
        COREParser coreParser = getParserForSource(source);
        // a declaration can only be valid if it parses on this parser rule
        COREParser.ParseContext tree = coreParser.parse();

        return compileContext(tree);
    }

    /**
     * Builds the list of Logical Plans from the given Parse Context tree.
     * For all queries in the tree, it builds the corresponding Logical Plan
     * and for each declaration, it just visits the tree with the corresponding
     * Visitor.
     *
     * @param parseContext Parse Context tree built from a source string.
     * @return The list of Logical Plans for the given tree.
     * @throws UnknownStatementException
     */
    @Override
    List<LogicalPlan> compileContext(COREParser.ParseContext parseContext) throws UnknownStatementException {

        List<LogicalPlan> plans = new ArrayList<>();

        for (COREParser.Core_stmtContext statementCtx : parseContext.core_stmt()) {
            if (statementCtx.core_declaration() != null) {
                declarationCompiler.compileContext(statementCtx.core_declaration());
            } else if (statementCtx.core_query() != null) {
                plans.add(queryCompiler.compileContext(statementCtx.core_query()));
            } else {
                throw new UnknownStatementException("Unknown first level statement on CORE source string", statementCtx);
            }
        }
        return plans;
    }
}
