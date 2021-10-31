package edu.puc.core.execution.cea;

import edu.puc.core.execution.bitsets.BitSetMatcher;
import edu.puc.core.parser.plan.cea.Transition;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

public class ExecutableCEAFactory {

    public static ExecutableCEA executorFor(edu.puc.core.parser.plan.cea.CEA cea){

        @SuppressWarnings("unchecked") Collection<Transition>[] blackTransitions = new Collection[cea.getStateCount()];
        @SuppressWarnings("unchecked") Collection<Transition>[] whiteTransitions = new Collection[cea.getStateCount()];

        for (int i = 0; i < cea.getStateCount(); i++) {
            blackTransitions[i] = new ArrayList<>();
            whiteTransitions[i] = new ArrayList<>();
        }

        cea.getTransitions().forEach(transition -> {
            int fromState = transition.getFromState();
            if (transition.isBlack()){
                blackTransitions[fromState].add(transition);
            }
            else {
                whiteTransitions[fromState].add(transition);
            }
        });

        return new ExecutableCEA() {

            @Override
            public Integer getInitialState() {
                return cea.getInitState();
            }

            @Override
            public int getFinalState() {
                return cea.getFinalState();
            }

            @Override
            public boolean isFinal(Integer state) {
                return cea.getFinalState() == state;
            }

            @Override
            public int getNStates() {
                return cea.getStateCount();
            }

            private Set<Integer> getSetForTransitions(Collection<Transition> transitions, BitSet b){
                return transitions.stream()
                        .filter(transition -> BitSetMatcher.bitSetSatisfiesVector(b, transition.getBitVector()))
                        .map(Transition::getToState)
                        .collect(Collectors.toSet());
            }

            @Override
            public Set<Integer> blackTransition(Integer state, BitSet b) {
                return getSetForTransitions(blackTransitions[state], b);
            }

            @Override
            public Set<Integer> whiteTransition(Integer state, BitSet b) {
                return getSetForTransitions(whiteTransitions[state], b);
            }
        };
    }
}
