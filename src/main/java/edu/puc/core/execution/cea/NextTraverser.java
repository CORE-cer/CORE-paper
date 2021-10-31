package edu.puc.core.execution.cea;

import edu.puc.core.execution.structures.states.DoubleStateSet;
import edu.puc.core.execution.structures.states.State;
import edu.puc.core.execution.structures.states.StateTuple;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class NextTraverser extends Traverser<DoubleStateSet> {

    private static HashMap<ExecutableCEA, NextTraverser> traverserMap = new HashMap<>();

    private NextTraverser(ExecutableCEA cea) {
        super(cea);
    }

    @Override
    StateTuple calculateNextState(State<DoubleStateSet> fromState, BitSet vector) {
        StateTuple ret = new StateTuple();

        Set<Integer> blackSetR = new HashSet<>();
        Set<Integer> blackSetS = new HashSet<>();

        Set<Integer> whiteSetR = new HashSet<>();
        Set<Integer> whiteSetS = new HashSet<>();

        /* R set */
        getNextStates(fromState.getStateSet().getFirstStateSet(), vector, blackSetR, whiteSetR);

        /* S set */
        getNextStates(fromState.getStateSet().getSecondStateSet(), vector, blackSetS, whiteSetS);

        /* Black State */
        Set<Integer> newBlackR = new HashSet<>(blackSetR);
        Set<Integer> newBlackS = new HashSet<>(blackSetS);
        newBlackS.addAll(whiteSetS);
        newBlackR.removeAll(newBlackS);
        ret.setBlackState(getStateFromStateSet(new DoubleStateSet(newBlackR, newBlackS)));

        /* White State */
        Set<Integer> newWhiteR = new HashSet<>(whiteSetR);
        Set<Integer> newWhiteS = new HashSet<>(whiteSetS);
        newWhiteS.addAll(blackSetS);
        newWhiteS.addAll(blackSetR);
        newWhiteR.removeAll(newWhiteS);
        ret.setWhiteState(getStateFromStateSet(new DoubleStateSet(newWhiteR, newWhiteS)));

        knownTransitionsList.get(fromState.getId()).put(vector, ret);
        return ret;
    }

    /**
     * Get the {@link NextTraverser} associated to the given CEA.
     * @param cea {@link ExecutableCEA} to get the traverser for.
     * @return Previously (or newly) created {@link NextTraverser}.
     */
    public static NextTraverser getInstance(ExecutableCEA cea) {
        NextTraverser instance = traverserMap.get(cea);
        if (instance == null) {
            instance = new NextTraverser(cea);
            instance.init(new DoubleStateSet(
                    Stream.of(instance.INITIAL).collect(Collectors.toSet()),
                    Collections.emptySet()
            ));
            traverserMap.put(cea, instance);
        }
        return instance;
    }
}

