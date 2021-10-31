package edu.puc.core.parser.plan.filter;

import edu.puc.core.parser.plan.cea.CEA;

public abstract class Filter {
    /**
     * Applies this Filter to the given CEA, returning the updated CEA.
     * @param cea CEA over which to apply the Filter.
     * @return CEA after having applied the Filter.
     */
    public abstract CEA applyToCEA(CEA cea);
}
