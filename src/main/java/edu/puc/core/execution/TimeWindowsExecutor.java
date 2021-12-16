package edu.puc.core.execution;

import edu.puc.core.execution.cea.Traverser;
import edu.puc.core.execution.structures.CDS.time.CDSNodeManager;
import edu.puc.core.execution.structures.CDS.time.CDSTimeNode;
import edu.puc.core.execution.structures.nodelist.UnionList;
import edu.puc.core.execution.structures.output.CDSTimeComplexEventGrouping;
import edu.puc.core.execution.structures.states.State;
import edu.puc.core.execution.structures.states.StateTuple;
import edu.puc.core.parser.plan.cea.Transition;
import edu.puc.core.parser.plan.query.TimeWindow;
import edu.puc.core.runtime.events.Event;
import edu.puc.core.runtime.predicates.BitSetGenerator;
import edu.puc.core.runtime.profiling.Profiler;

import java.util.*;
import java.util.function.Supplier;

public class TimeWindowsExecutor extends BaseExecutor {

    private long currentTime;
    private final TimeWindow timeWindow;
    private final long windowDelta;
    private CDSNodeManager manager;
    private Map<State<?>, UnionList> states;
    private ArrayList<State<?>> orderedStateList;

    private int count;
    private final int PRUNE_THRESHOLD = 1000;

    TimeWindowsExecutor(Traverser traverser, BitSetGenerator bitSetGenerator, boolean discardPartials, TimeWindow timeWindow) {
        super(traverser, bitSetGenerator, discardPartials);
        switch (timeWindow.getKind()){
            case TIME:
                windowDelta = timeWindow.getNumberOfMilis();
                break;
            case EVENTS:
                windowDelta = timeWindow.getNumberOfEvents();
                break;
            case CUSTOM:
                windowDelta = timeWindow.getCustomNumber();
                break;
            case NONE:
            default:
                throw new Error("Invalid option for executor");
        }
        this.timeWindow = timeWindow;
    }


    @Override
    void setupCleanExecutor() {
        count = 0;
        states = new HashMap<>();
        manager = new CDSNodeManager();
        orderedStateList = new ArrayList<>();
    }

    @Override
    public boolean sendEvent(Event event) {
        return evaluation(event);
    }

    private void startNewRun() {
        UnionList ul = new UnionList(manager);
        ul.insert(manager.createBottomNode(currentTime));
        states.put(traverser.getInitialState(), ul);
    }

    private boolean evaluation(Event event) {
        long startExecutionTime = System.nanoTime();
        updateTime(event);
        ArrayList<State<?>> newOrderedStateList = new ArrayList<>();
        Map<State<?>, UnionList> _states = new HashMap<>();
        activeFinalStates = new HashSet<>();
        boolean enumerated = false;

        startNewRun();

        BitSet bitSet = bitSetGenerator.getBitSetFromEvent(event);

        execTrans(event, newOrderedStateList, _states, bitSet, traverser.getInitialState());
        for (State<?> currentState : orderedStateList) {
            // Only process if the union-list referenced is not null and inside the time-windows.
            if (states.get(currentState).getHead() == null || states.get(currentState).getHead().getMm() - currentTime > windowDelta) {
                break;
            }
            execTrans(event, newOrderedStateList, _states, bitSet, currentState);
        }

        states = _states;
        orderedStateList = newOrderedStateList;

        Profiler.addExecutionTime(System.nanoTime() - startExecutionTime);

        if (!activeFinalStates.isEmpty() && checkFirstMatch()) {
            long startEnumerationTime = System.nanoTime();
            enumerate(event);
            Profiler.addEnumerationTime(System.nanoTime() - startEnumerationTime);
            enumerated = true;
        }

        long startPruneTime = System.nanoTime();
        count++;
        if (count > PRUNE_THRESHOLD) {
            manager.prune(currentTime, windowDelta);
            count = 0;
            Profiler.incrementCleanUps();
        }
        Profiler.addExecutionTime(System.nanoTime() - startPruneTime);
        return enumerated;
    }

    private void execTrans(Event event, ArrayList<State<?>> newOrderedStateList, Map<State<?>, UnionList> _states, BitSet bitSet, State<?> currentState) {
        StateTuple nextStates = traverser.nextState(currentState, bitSet);
        State<?> blackState = nextStates.getBlackState();
        State<?> whiteState = nextStates.getWhiteState();

        // In the paper black comes before white
        if (!whiteState.isRejectionState()) {
            UnionList ul = states.get(currentState);
            add(whiteState, _states, ul::merge, ul, newOrderedStateList);
        }

        // Why only black triggers complex event enumeration?
        if (!blackState.isRejectionState()) {
            CDSTimeNode n = manager.createOutputNode(states.get(currentState).merge(), Transition.TransitionType.BLACK, event, currentTime);
            UnionList ul = new UnionList(manager);
            ul.insert(n);
            add(blackState, _states, () -> n, ul, newOrderedStateList);

            if (blackState.isFinalState()) {
                activeFinalStates.add(blackState);
            }
        }

    }

    private void add(
            State<?> q,
            Map<State<?>, UnionList> _states,
            Supplier<CDSTimeNode> n,
            UnionList ul,
            ArrayList<State<?>> newOrderedStateList
    ) {
        UnionList stateNodeList = _states.get(q);
        if (stateNodeList == null) {
            newOrderedStateList.add(q);
            _states.put(q, ul);
        } else {
            stateNodeList.insert(n.get());
        }
    }

    private void updateTime(Event event) {
        switch (timeWindow.getKind()) {
            case TIME:
                currentTime = event.getTimestamp();
                break;
            case EVENTS:
                currentTime = event.getIndex();
                break;
            case CUSTOM:
                currentTime = ((Double)event.getValue(timeWindow.getAttr())).longValue();
                break;
        }
    }

    // Check that the union-list is inside the time windows
    private boolean checkFirstMatch() {
        for (State<?> state : activeFinalStates) {
            UnionList current = states.get(state);
            if (current != null) {
                if (currentTime - current.merge().getMm() < windowDelta) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    void enumerate(Event triggeringEvent) {
        enumerate(triggeringEvent, 0);
    }

    @Override
    void enumerate(Event triggeringEvent, long limit) {
        watcher.update();
        if (matchCallback != null) {
            CDSTimeComplexEventGrouping complexEventGrouping = new CDSTimeComplexEventGrouping(triggeringEvent, limit, windowDelta, currentTime);
            boolean added = false;
            for (State<?> state : activeFinalStates) {
                UnionList current = states.get(state);
                if (current != null) {
                    complexEventGrouping.addCDSNode(current.merge());
                    added = true;
                }
            }
            if (added) {
                matchCallback.accept(complexEventGrouping);
            }
        }
        if (discardPartials) {
            setupCleanExecutor();
        }
    }
}
