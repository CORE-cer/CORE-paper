package edu.puc.core.execution.cea;

import edu.puc.core.execution.structures.states.State;
import edu.puc.core.execution.structures.states.StateTuple;
import edu.puc.core.execution.structures.states.TripleStateSet;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LastTraverser extends Traverser<TripleStateSet> {

    private static HashMap<ExecutableCEA, LastTraverser> traverserMap = new HashMap<>();

    private LastTraverser(ExecutableCEA cea) {
        super(cea);
    }

    @Override
    StateTuple calculateNextState(State<TripleStateSet> fromState, BitSet vector) {
        StateTuple ret = new StateTuple();

        Set<Integer> blackSetR = new HashSet<>();
        Set<Integer> blackSetS = new HashSet<>();
        Set<Integer> blackSetT = new HashSet<>();

        Set<Integer> whiteSetR = new HashSet<>();
        Set<Integer> whiteSetS = new HashSet<>();
        Set<Integer> whiteSetT = new HashSet<>();

        /* R set */
        getNextStates(fromState.getStateSet().getFirstStateSet(), vector, blackSetR, whiteSetR);

        /* S set */
        getNextStates(fromState.getStateSet().getSecondStateSet(), vector, blackSetS, whiteSetS);

        /* T set */
        getNextStates(fromState.getStateSet().getThirdStateSet(), vector, blackSetT, whiteSetT);

        /* Black State */
        Set<Integer> newBlackR = new HashSet<>(blackSetR);
        Set<Integer> newBlackT = new HashSet<>(blackSetT);
        newBlackR.removeAll(blackSetS);
        newBlackT.addAll(whiteSetT);
        newBlackT.addAll(whiteSetR);
        newBlackT.addAll(whiteSetS);
        newBlackT.removeAll(blackSetS);
        newBlackT.removeAll(newBlackR);
        ret.setBlackState(getStateFromStateSet(new TripleStateSet(newBlackR, blackSetS, newBlackT)));

        /* White State */
        Set<Integer> newWhiteR = new HashSet<>(whiteSetR);
        Set<Integer> newWhiteS = new HashSet<>(whiteSetS);
        Set<Integer> newWhiteT = new HashSet<>(whiteSetT);
        newWhiteS.addAll(blackSetS);
        newWhiteS.addAll(blackSetR);
        newWhiteS.addAll(blackSetT);
        newWhiteR.removeAll(newWhiteS);
        newWhiteT.removeAll(newWhiteR);
        newWhiteT.removeAll(newWhiteS);
        ret.setWhiteState(getStateFromStateSet(new TripleStateSet(newWhiteR, newWhiteS, newWhiteT)));

        knownTransitionsList.get(fromState.getId()).put(vector, ret);
        return ret;
    }

    /**
     * Get the {@link LastTraverser} associated to the given CEA.
     * @param cea {@link ExecutableCEA} to get the traverser for.
     * @return Previously (or newly) created {@link LastTraverser}.
     */
    public static LastTraverser getInstance(ExecutableCEA cea) {
        LastTraverser instance = traverserMap.get(cea);
        if (instance == null) {
            instance = new LastTraverser(cea);
            instance.init(new TripleStateSet(
                    Stream.of(instance.INITIAL).collect(Collectors.toSet()),
                    Collections.emptySet(),
                    Collections.emptySet()
            ));
            traverserMap.put(cea, instance);
        }
        return instance;
    }
}

