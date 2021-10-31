package edu.puc.core.parser.visitors;


import edu.puc.core.parser.COREBaseVisitor;
import edu.puc.core.parser.COREParser;
import edu.puc.core.parser.exceptions.DuplicateNameException;
import edu.puc.core.parser.exceptions.UnknownDataTypeException;
import edu.puc.core.parser.plan.exceptions.ValueException;
import edu.puc.core.parser.plan.values.ValueType;
import edu.puc.core.util.StringUtils;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.List;

class AttributeDeclarationVisitor extends COREBaseVisitor<List<Pair<String, ValueType>>> {
    public List<Pair<String, ValueType>> visitAttribute_dec_list(COREParser.Attribute_dec_listContext ctx) {
        List<COREParser.Attribute_declarationContext> attributes = ctx.attribute_declaration();

        List<Pair<String, ValueType>> attributeMap = new ArrayList<>();

        for (COREParser.Attribute_declarationContext attributeContext : attributes) {
            String attributeName = StringUtils.tryRemoveQuotes(attributeContext.attribute_name().getText());
            String dataType = attributeContext.datatype().getText();

            if (attributeMap.stream().anyMatch(pair -> pair.getKey().equals(attributeName))) {
                throw new DuplicateNameException("Attribute `" + attributeName + "` is declared more than once", ctx);
            }

            ValueType attrType;
            try {
                attrType = ValueType.getValueFor(dataType);
            }
            catch (ValueException exc){
                throw new UnknownDataTypeException("`" + dataType + "` is not a valid data type", attributeContext.datatype());
            }
            attributeMap.add(new Pair<>(attributeName, attrType));
        }

        return attributeMap;
    }
}
