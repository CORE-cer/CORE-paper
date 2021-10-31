package edu.puc.core.parser;

import edu.puc.core.parser.exceptions.UnknownStatementException;
import edu.puc.core.parser.visitors.EventDeclarationVisitor;
import edu.puc.core.parser.visitors.StreamDeclarationVisitor;
import org.antlr.v4.runtime.misc.ParseCancellationException;

public class DeclarationParser extends BaseParser<Void, COREParser.Core_declarationContext> {

    /**
     * Parses the source declaration and visits the generated Declaration Context tree.
     *
     * @param declaration String containing the CORE declaration.
     * @throws ParseCancellationException
     */
    @Override
    public Void parse(String declaration) throws ParseCancellationException {
        COREParser coreParser = getParserForSource(declaration);
        // a declaration can only be valid if it parses on this parser rule
        COREParser.Core_declarationContext tree = coreParser.core_declaration();

        compileContext(tree);

        return null;
    }

    /**
     * Visits the Declaration Context with the corresponding Visitor whether
     * it is an Event or Stream declaration.
     *
     * @param declarationContext Declaration Context built from a declaration string.
     * @throws UnknownStatementException
     */
    @Override
    Void compileContext(COREParser.Core_declarationContext declarationContext) throws UnknownStatementException {
        // check what kind of declaration it is and compile it
        if (declarationContext.event_declaration() != null){
            compileEventDeclaration(declarationContext.event_declaration());
        }
        else if (declarationContext.stream_declaration() != null){
            compileStreamDeclaration(declarationContext.stream_declaration());
        }
        else {
            // This means someone has modified the grammar to implement another kind of declaration.
            // This method should be modified to include that kind of declaration.
            throw new UnknownStatementException("No knowledge on how to compile given declaration statement.", declarationContext);
        }
        return null;
    }

    private void compileEventDeclaration(COREParser.Event_declarationContext tree){
        new EventDeclarationVisitor().visit(tree);
    }

    private void compileStreamDeclaration(COREParser.Stream_declarationContext tree){
        new StreamDeclarationVisitor().visit(tree);
    }
}
