package edu.puc.core.parser.plan.cea;

import edu.puc.core.parser.plan.Label;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class Transition implements Comparable<Transition> {

    public enum TransitionType {
        WHITE("w"),
        BLACK("b");

        private final String symbol;

        TransitionType(String symbol) {
            this.symbol = symbol;
        }

        public String getSymbol() {
            return symbol;
        }

        public boolean isBlack() {
            return this == TransitionType.BLACK;
        }
    }

    private final int fromState;
    private final int toState;
    private TransitionType transitionType;
    private Set<Label> labels;
    private BitVector bitVector;

    private Transition(Transition toCopy) {
        fromState = toCopy.fromState;
        toState = toCopy.toState;
        transitionType = toCopy.transitionType;
        labels = new HashSet<>(toCopy.labels);
        bitVector = toCopy.bitVector.copy();
    }

    public Transition(int fromState, int toState, BitVector bitVector, TransitionType transitionType) {
        this.fromState = fromState;
        this.toState = toState;
        this.transitionType = transitionType;
        this.bitVector = bitVector;
        labels = new HashSet<>();
    }

    public Transition(int fromState, int toState, BitVector bitVector, Label label, TransitionType transitionType) {
        this(fromState, toState, bitVector, new HashSet<>(java.util.stream.Stream.of(label).collect(Collectors.toSet())), transitionType);
        labels.add(label);
    }


    public Transition(int fromState, int toState, BitVector bitVector, Set<Label> labels, TransitionType transitionType) {
        this(fromState, toState, bitVector, transitionType);
        this.labels = new HashSet<>(labels);
    }

    public Transition displaceTransition(int nStates) {
        return new Transition(fromState + nStates, toState + nStates, bitVector, labels, transitionType);
    }

    public Transition replaceToState(int toState) {
        return new Transition(fromState, toState, bitVector, labels, transitionType);
    }

    public Transition replaceFromState(int fromState) {
        return new Transition(fromState, toState, bitVector, labels, transitionType);
    }

    public int getFromState() {
        return fromState;
    }

    public int getToState() {
        return toState;
    }

    public boolean overLabel(Label label) {
        return labels.contains(label);
    }

    public boolean isBlack() {
        return transitionType.isBlack();
    }

    public void addLabel(Label label) {
        labels.add(label);
    }

    public Transition copy() {
        return new Transition(this);
    }

    public TransitionType getType() {
        return transitionType;
    }

    public void setType(TransitionType transitionType) {
        this.transitionType = transitionType;
    }

    public Set<Label> getLabels() {
        return labels;
    }

    public BitVector getBitVector(){
        return bitVector;
    }

    public Transition addPredicate(BitVector bitVector){
        Transition newTransition = copy();
        newTransition.bitVector = this.bitVector.cojoin(bitVector);
        return newTransition;
    }

    @Override
    public String toString() {
        return "Transition(" +
                transitionType.getSymbol() + ", " +
                fromState + ", " +
                toState + ", " +
                bitVector.toString() +
                ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Transition)) return false;
        Transition other = (Transition)obj;
        return (other.bitVector.equals(bitVector)
                && other.fromState == fromState
                && other.toState == toState
                && other.transitionType == transitionType);
    }

    @Override
    public int hashCode() {
        return bitVector.hashCode() * 31 + transitionType.hashCode() * 17 + toState * 13 + fromState * 11;
    }

    @Override
    public int compareTo(Transition o) {
        // Black < white
        if (transitionType.isBlack() && !o.transitionType.isBlack()) {
            return -1;
        } else if (!transitionType.isBlack() && o.transitionType.isBlack()) {
            return 1;
        }
        if (fromState < o.fromState) return -1;
        if (fromState > o.fromState) return 1;
        return Integer.compare(toState, o.toState);
    }
}
