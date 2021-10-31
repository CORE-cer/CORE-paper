package edu.puc.core.parser.plan.filter;

import edu.puc.core.parser.plan.Label;
import edu.puc.core.parser.plan.cea.CEA;
import edu.puc.core.parser.plan.cea.PredicateFactory;
import edu.puc.core.parser.plan.predicate.AtomicPredicate;

public class AtomicFilter extends Filter {
    private Label label;
    private AtomicPredicate predicate;


    public AtomicFilter(Label label, AtomicPredicate predicate){
        this.label = label;
        this.predicate = predicate;
    }

    public Label getLabel() {
        return label;
    }

    public AtomicPredicate getPredicate() {
        return predicate;
    }

    @Override
    public CEA applyToCEA(CEA cea) {
        return cea.addPredicate(PredicateFactory.getInstance().from(predicate), label);
    }
}
