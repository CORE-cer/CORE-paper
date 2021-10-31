package edu.puc.core.parser.plan.cea;

import edu.puc.core.parser.plan.query.ProjectionList;

public class ProjectionCEA extends CEA {
    public ProjectionCEA(CEA inner, ProjectionList projectionList) {
        super(inner);

        for (Transition transition : transitions) {
            if (!projectionList.containsAny(transition.getLabels())) {
                if (transition.isBlack()) {
                    transition.setType(Transition.TransitionType.WHITE);
                }
            }
        }
    }
}
