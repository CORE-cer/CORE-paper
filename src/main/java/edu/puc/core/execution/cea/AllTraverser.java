package edu.puc.core.execution.cea;

import edu.puc.core.execution.structures.states.SimpleStateSet;
import edu.puc.core.execution.structures.states.State;
import edu.puc.core.execution.structures.states.StateTuple;

import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AllTraverser extends Traverser<SimpleStateSet> {

    private static final HashMap<ExecutableCEA, AllTraverser> traverserMap = new HashMap<>();

    private AllTraverser(ExecutableCEA cea) {
        super(cea);
    }

    @Override
    StateTuple calculateNextState(State<SimpleStateSet> fromState, BitSet vector) {
        Set<Integer> nextStatesBlack = new HashSet<>();
        Set<Integer> nextStatesWhite = new HashSet<>();

        for (Integer s : fromState.getStateSet().getStateSet()) {
            nextStatesBlack.addAll(cea.blackTransition(s, vector));
            nextStatesWhite.addAll(cea.whiteTransition(s, vector));
        }


        return new StateTuple(
                getStateFromStateSet(new SimpleStateSet(nextStatesBlack)),
                getStateFromStateSet(new SimpleStateSet(nextStatesWhite))
        );
    }

    /**
     * Get the {@link AllTraverser} associated to the given CEA.
     * @param cea {@link ExecutableCEA} to get the traverser for.
     * @return Previously (or newly) created {@link AllTraverser}.
     */
    public static AllTraverser getInstance(ExecutableCEA cea) {
        AllTraverser instance = traverserMap.get(cea);
        if (instance == null) {
            instance = new AllTraverser(cea);
            instance.init(new SimpleStateSet(Stream.of(instance.INITIAL).collect(Collectors.toSet())));
            traverserMap.put(cea, instance);
        }
        return instance;
    }
}

