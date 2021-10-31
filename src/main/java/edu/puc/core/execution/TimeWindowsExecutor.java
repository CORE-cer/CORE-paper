package edu.puc.core.execution;

import edu.puc.core.execution.cea.Traverser;
import edu.puc.core.execution.structures.CDS.time.CDSNodeManager;
import edu.puc.core.execution.structures.CDS.time.CDSTimeNode;
import edu.puc.core.execution.structures.nodelist.NodeList;
import edu.puc.core.execution.structures.output.CDSTimeComplexEventGrouping;
import edu.puc.core.execution.structures.states.State;
import edu.puc.core.execution.structures.states.StateTuple;
import edu.puc.core.parser.plan.cea.Transition;
import edu.puc.core.parser.plan.query.TimeWindow;
import edu.puc.core.runtime.events.Event;
import edu.puc.core.runtime.predicates.BitSetGenerator;
import edu.puc.core.runtime.profiling.Profiler;

import java.util.*;

public class TimeWindowsExecutor extends BaseExecutor {

    private final TimeWindow timeWindow;
    private final long windowDelta;
    private long currentTime;
    private CDSNodeManager manager;
    private int count;

    private Map<State<?>, NodeList> states;
    private ArrayList<State<?>> orderedStateList;

    private State<?> qinit;

    private final int PRUNE_LIMIT = 1000;


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
        states = new HashMap<>();

        count = 0;
        manager = new CDSNodeManager();

        qinit = traverser.getInitialState();
        orderedStateList = new ArrayList<>();
    }

    private void startNewRun() {
        NodeList initList = new NodeList(manager);
        initList.addSorted(manager.createBottomNode(currentTime));
        states.put(qinit, initList);
    }

    @Override
    public boolean sendEvent(Event event){
        /* same algorithm as old ANY */
        long startExecutionTime = System.nanoTime();
        updateTime(event);
        ArrayList<State<?>> newOrderedStateList = new ArrayList<>();
        Map<State<?>, NodeList> _states = new HashMap<>();
        activeFinalStates = new HashSet<>();
        boolean enumerated = false;

        startNewRun();

        BitSet bitSet = bitSetGenerator.getBitSetFromEvent(event);

        updateNextStates(event, newOrderedStateList, _states, bitSet, qinit);
        for (State<?> currentState : orderedStateList) {
            if (states.get(currentState).getHead() == null || states.get(currentState).getHead().getMm() - currentTime > windowDelta) {
                break;
            }
            updateNextStates(event, newOrderedStateList, _states, bitSet, currentState);
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
        if (count > PRUNE_LIMIT) {

            manager.prune(currentTime, windowDelta);
            count = 0;
            Profiler.incrementCleanUps();
        }
        Profiler.addExecutionTime(System.nanoTime() - startPruneTime);
        return enumerated;
    }

    private void updateNextStates(Event event, ArrayList<State<?>> newOrderedStateList, Map<State<?>, NodeList> _states, BitSet bitSet, State<?> currentState) {
        StateTuple nextStates = traverser.nextState(currentState, bitSet);
        State<?> blackState = nextStates.getBlackState();
        State<?> whiteState = nextStates.getWhiteState();

        if (!whiteState.isRejectionState()) {
            updateWhiteState(newOrderedStateList, _states, states.get(currentState), whiteState);
        }

        if (!blackState.isRejectionState()) {
            update(event, newOrderedStateList, _states, currentState, blackState, Transition.TransitionType.BLACK);
            if (blackState.isFinalState()) {
                activeFinalStates.add(blackState);
            }
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

    private void update(Event event, ArrayList<State<?>> newOrderedStateList, Map<State<?>, NodeList> _states, State<?> currentState, State<?> state, Transition.TransitionType transitionType) {
        CDSTimeNode newNode = manager.createOutputNode(states.get(currentState).merge(), transitionType, event, currentTime);
        updateStates(newOrderedStateList, _states, newNode, state);
    }

    private void updateStates(ArrayList<State<?>> newOrderedStateList, Map<State<?>, NodeList> _states, CDSTimeNode newNode, State<?> state) {
        if (currentTime - newNode.getMm() >= windowDelta) {
            return;
        }
        NodeList stateNodeList = _states.get(state);

        if (stateNodeList != null) {
            /* If we already added the node, then we update the node list */
            stateNodeList.addSorted(newNode);
            return;
        }

        /* If we haven't added the node to the state map, we add it to the ordered list too */
        newOrderedStateList.add(state);

        /* Create and put the new Node List */
        NodeList newNodeList = new NodeList(manager);
        newNodeList.addSorted(newNode);
        _states.put(state, newNodeList);
    }

    private void updateWhiteState(ArrayList<State<?>> newOrderedStateList, Map<State<?>, NodeList> _states, NodeList oldList, State<?> state) {
        NodeList stateNodeList = _states.get(state);

        if (stateNodeList != null) {
            /* If we already added the node, then we update the node list */
            stateNodeList.addSorted(oldList.merge());
            return;
        }

        newOrderedStateList.add(state);
        _states.put(state, oldList);
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
                NodeList current = states.get(state);
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

    private boolean checkFirstMatch() {
        for (State<?> state : activeFinalStates) {
            NodeList current = states.get(state);
            if (current != null) {
                if (currentTime - current.merge().getMm() < windowDelta) {
                    return true;
                }
            }
        }
        return false;
    }
}
