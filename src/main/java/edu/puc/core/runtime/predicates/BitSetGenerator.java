package edu.puc.core.runtime.predicates;

import edu.puc.core.parser.plan.cea.PredicateFactory;
import edu.puc.core.runtime.events.Event;

import java.util.BitSet;
import java.util.stream.Collectors;

public class BitSetGenerator {

    private final int[] streamToBitArray;
    private final int[] eventToBitArray;
    private final int bitCount;
    private final int predicateOffset;
    private final PredicateEvaluator[] predicateEvaluators;

    public BitSetGenerator(PredicateFactory factory){
        streamToBitArray = factory.getStreamToBitArray();
        eventToBitArray = factory.getEventToBitArray();
        bitCount = factory.getBitCount();
        predicateOffset = factory.getPredicateOffset();
        predicateEvaluators = factory.getAtomicPredicateList().stream()
                .map(PredicateEvaluator::getEvaluatorForPredicate)
                .collect(Collectors.toList())
                .toArray(new PredicateEvaluator[]{});
    }

    /**
     * Builds the {@link BitSet} for the given {@link Event}, setting the first
     * bits to represent the Stream, the next ones for the {@link Event}
     * and the last ones for each predicate and its evaluation over the event.
     * 
     * @param event The {@link Event} to get the {@link BitSet} from.
     * @return The {@link BitSet} representation for the provided {@link Event}.
     */
    public BitSet getBitSetFromEvent(Event event){
        BitSet bitset = new BitSet(bitCount);
        bitset.set(streamToBitArray[event.getStream()]);
        bitset.set(eventToBitArray[event.getType()]);

        int idx = predicateOffset;
        for (PredicateEvaluator evaluator : predicateEvaluators) {
            if (evaluator.eval(event)) bitset.set(idx);
            idx++;
        }
        return bitset;
    }

}
