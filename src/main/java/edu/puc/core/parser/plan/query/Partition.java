package edu.puc.core.parser.plan.query;

import edu.puc.core.runtime.events.Event;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class Partition {

    private Set<String> attributes;

    public Partition(Collection<String> attributes) {
        this.attributes = new HashSet<>(attributes);
        // TODO : check that partition attributes cover all events on query
    }

    public Set<String> getAttributes() {
        return attributes;
    }

    public String getAttributeFor(Event e){
        return attributes.iterator().next();
    }

}
