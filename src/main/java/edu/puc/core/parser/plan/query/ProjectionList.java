package edu.puc.core.parser.plan.query;

import edu.puc.core.parser.plan.Label;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class ProjectionList {
    private Set<Label> labels;
    private final boolean matchAll;

    private ProjectionList() {
        matchAll = true;
    }

    public ProjectionList(Collection<Label> labels) {
        matchAll = false;
        this.labels = new HashSet<>(labels);
    }

    public boolean contains(Label label) {
        if (matchAll) return true;
        return labels.contains(label);
    }

    public boolean containsAny(Collection<Label> labels) {
        if (matchAll) return true;
        for (Label label : labels) {
            if (this.labels.contains(label)) {
                return true;
            }
        }
        return false;
    }

    public static final ProjectionList ALL_EVENTS = new ProjectionList();

}
