package edu.puc.core.parser.plan.cea;

import edu.puc.core.parser.plan.Label;

import java.util.*;
import java.util.stream.Collectors;

public class CEA {

    final int initState = 0;
    int stateCount;
    Collection<Transition> transitions;
    Set<Label> labelSet;

    public int getInitState() {
        return initState;
    }

    public int getStateCount() {
        return stateCount;
    }

    CEA() {
        transitions = new ArrayList<>();
        labelSet = new HashSet<>();
    }

    CEA(CEA otherCea) {
        this();
        stateCount = otherCea.stateCount;

        transitions.addAll(
                otherCea.transitions
                        .stream()
                        .map(Transition::copy)
                        .collect(Collectors.toList())
        );

        labelSet.addAll(otherCea.labelSet);
    }

    CEA(int stateCount) {
        this();
        this.stateCount = stateCount;
    }

    public CEA copy() {
        return new CEA(this);
    }

    public int getFinalState() {
        return stateCount - 1;
    }

    public Collection<Transition> getTransitions() {
        return new ArrayList<>(transitions);
    }

    /**
     * Adds the given predicate to all this {@link CEA} {@link Transition}s that are of
     * type black and go over the given {@link Label}.
     * 
     * @param bitVector {@link BitVector} representing a predicate.
     * @param label {@link Label} to identify where to add the predicate.
     * @return The {@link CEA} after adding the predicate.
     */
    public CEA addPredicate(BitVector bitVector, Label label){
        CEA newCea = copy();

        if (!labelSet.contains(label)) return newCea;

        newCea.transitions = transitions.stream()
                .map(transition -> {
                    if (transition.isBlack() && transition.overLabel(label))
                        return transition.addPredicate(bitVector);
                    return transition.copy();
                })
                .collect(Collectors.toList());
        return newCea;
    }

    /**
     * Returns a new cea that matches the same patterns, but that contains no useless states
     * and transitions
     *
     * @return a clean ExecutableCEA
     */
    public CEA getCleanCea() {
        // calculate reachable states
        Map<Integer, Set<Integer>> reachableFrom = createStatesReachableMap(this);

        // useful states
        Set<Integer> reachableFromQ0 = reachableFrom.get(0);
        Set<Integer> usefulStates = new HashSet<>();

        reachableFromQ0.forEach(q -> {
            Set<Integer> reachableQ = new HashSet<>(reachableFrom.get(q));
            if (reachableQ.contains(this.getFinalState())) {
                usefulStates.add(q);
            }
        });

        // renumber all useful states
        int[] newNames = new int[stateCount];
        int newStateCount = 0;

        for (int q = 0; q < stateCount; q++) {
            if (usefulStates.contains(q)) {
                newNames[q] = newStateCount++;
            }
        }

        // rename useless transitions and rename the remaining ones

        ArrayList<Transition> newTransitions = new ArrayList<>();
        Set<Label> newLabelSet = new HashSet<>();


        for (Transition transition : transitions) {
            if (usefulStates.contains(transition.getFromState()) && usefulStates.contains(transition.getToState())) {

                int newFromState = newNames[transition.getFromState()];
                int newToState = newNames[transition.getToState()];
                Transition newTransition = transition
                        .replaceFromState(newFromState)
                        .replaceToState(newToState);
                newTransitions.add(newTransition);

                newLabelSet.addAll(transition.getLabels());
            }
        }
        // Sort transitions
        newTransitions.sort(Transition::compareTo);


        CEA newCea = new CEA(newStateCount);
        newCea.transitions = newTransitions;
        newCea.labelSet = newLabelSet;
        return newCea;
    }


    private static Map<Integer, Set<Integer>> createStatesReachableMap(CEA cea) {

        // initialize the map for every state
        Map<Integer, Set<Integer>> reachableFrom = new HashMap<>();
        for (int q = 0; q < cea.stateCount; q++) {
            Set<Integer> set = new HashSet<>();
            set.add(q);
            reachableFrom.put(q, set);
        }

        // add reachable states from direct transitions
        for (Transition t : cea.transitions)
            reachableFrom.get(t.getFromState()).add(t.getToState());

        boolean updated = true;

        while (updated) {
            // try to add new reachable states for each state
            updated = false;
            for (int p = 0; p < cea.stateCount; p++) {
                Set<Integer> reachableP = reachableFrom.get(p);
                Set<Integer> newReachable = new HashSet<>();
                for (int q : reachableP) {
                    newReachable.addAll(reachableFrom.get(q));
                }
                updated = updated || reachableP.addAll(newReachable);
            }
        }

        return reachableFrom;
    }

    @Override
    public String toString() {
        String finalStatesString = "" + getFinalState();
        StringBuilder stringBuilder = new StringBuilder("ExecutableCEA(\n")
                .append("  stateCount=").append(stateCount).append(",\n")
                .append("  initState=").append(initState).append(",\n")
                .append("  finalStates=[").append(finalStatesString).append("],\n")
                .append("  predicates=[");


        List<String> strings =  PredicateFactory.getInstance().getStringDescription();
        if (strings.size() > 0) {
            stringBuilder.append("\n    ");
        }
        int i;
        for (i = 0; i < strings.size() - 1; i++) {
            stringBuilder.append(strings.get(i)).append(",\n    ");
        }

        if (strings.size() > 0) {
            stringBuilder.append(strings.get(i)).append("\n  ");
        }

        stringBuilder.append("],\n  transitions=[");

        if (transitions.size() > 0) {
            stringBuilder.append("\n    ");
        }

        Transition[] transitionArray = transitions.toArray(new Transition[0]) ;
        Arrays.sort(transitionArray, Transition::compareTo);

        for (i = 0; i < transitions.size() - 1; i++) {
            Transition transition = transitionArray[i];
            stringBuilder.append(transition).append(",\n    ");
        }

        if (transitions.size() > 0) {
            stringBuilder.append(transitionArray[i]).append("\n  ");
        }

        stringBuilder.append("]\n)");
        return stringBuilder.toString();
    }

}
