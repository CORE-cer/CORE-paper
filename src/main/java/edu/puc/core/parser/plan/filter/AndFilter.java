package edu.puc.core.parser.plan.filter;

import edu.puc.core.parser.plan.Label;
import edu.puc.core.parser.plan.cea.BitVector;
import edu.puc.core.parser.plan.cea.CEA;
import edu.puc.core.parser.plan.cea.PredicateFactory;

public class AndFilter extends Filter {
    private Filter left;
    private Filter right;

    public AndFilter(Filter left, Filter right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public CEA applyToCEA(CEA cea) {
        // In case they are over the same label, they can be pushed down together
        if (left instanceof AtomicFilter && right instanceof AtomicFilter) {
            if (((AtomicFilter) left).getLabel().equals(((AtomicFilter) right).getLabel())) {
                Label label = ((AtomicFilter) left).getLabel();

                BitVector leftBitVector = PredicateFactory.getInstance().from(((AtomicFilter) left).getPredicate());
                BitVector rightBitVector = PredicateFactory.getInstance().from(((AtomicFilter) right).getPredicate());

                BitVector cojoined = leftBitVector.cojoin(rightBitVector);
                return cea.addPredicate(cojoined, label);
            }
        }
        // implement both filters separately over the same cea
        return right.applyToCEA(left.applyToCEA(cea));
    }
}
