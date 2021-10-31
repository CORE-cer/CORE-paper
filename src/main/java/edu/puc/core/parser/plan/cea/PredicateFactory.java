package edu.puc.core.parser.plan.cea;

import edu.puc.core.parser.plan.Event;
import edu.puc.core.parser.plan.Stream;
import edu.puc.core.parser.plan.predicate.AtomicPredicate;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class PredicateFactory {
    private static PredicateFactory instance;

    private final List<Stream> streams;
    private final List<Event> events;
    private final List<AtomicPredicate> atomicPredicateList;


    public int[] getStreamToBitArray() {
        return streamToBitArray;
    }

    public int[] getEventToBitArray() {
        return eventToBitArray;
    }

    private final int[] streamToBitArray;
    private final int[] eventToBitArray;


    private final int bitCount;

    public int getPredicateOffset() {
        return predicateOffset;
    }

    private final int predicateOffset;

    private PredicateFactory(
            List<Stream> streams,
            List<Event> events,
            int predicateCount
    ){
        this.streams = streams;
        this.events = events;
        atomicPredicateList = new ArrayList<>();

        int streamOffset = 0;
        int eventOffset = streams.size();
        predicateOffset = eventOffset + events.size();
        bitCount = predicateOffset + predicateCount;

        streamToBitArray = new int[streams.size()];
        for (int i = 0; i < streams.size(); i++) {
            streamToBitArray[streams.get(i).getStreamID()] = i + streamOffset;
        }
        eventToBitArray = new int[events.size()];
        for (int i = 0; i < events.size(); i++) {
            eventToBitArray[events.get(i).getEventType()] = i + eventOffset;
        }

    }

    public static void init(
            Collection<Stream> streams,
            Collection<Event> events,
            int predicateCount
    ){
        instance = new PredicateFactory(
                streams.stream().distinct().collect(Collectors.toList()),
                events.stream().distinct().collect(Collectors.toList()),
                predicateCount);
    }

    public static PredicateFactory getInstance(){
        if (instance == null) throw new Error("PredicateFactory has not been created");
        return instance;
    }

    public List<Stream> getStreams() {
        return streams;
    }

    public List<Event> getEvents() {
        return events;
    }

    public List<AtomicPredicate> getAtomicPredicateList() {
        return atomicPredicateList;
    }

    public int getBitCount() {
        return bitCount;
    }

    public List<String> getStringDescription(){
        List<String> strings = new ArrayList<>();
        strings.addAll(streams.stream().map(Stream -> "stream(event) == " + Stream.getName()).collect(Collectors.toList()));
        strings.addAll(events.stream().map(Event -> "type(event) == " + Event.getName()).collect(Collectors.toList()));
        strings.addAll(atomicPredicateList.stream().map(AtomicPredicate::toString).collect(Collectors.toList()));
        return strings;
    }

    private BitVector getPredicateForBit(int eventBit) {
        BitSet mask = new BitSet(bitCount);
        mask.set(eventBit);

        BitSet match = new BitSet(bitCount);
        match.set(eventBit);

        return new BitVector(mask, match);
    }

    public BitVector from(Event event){
        int eventBit = eventToBitArray[event.getEventType()];
        return getPredicateForBit(eventBit);
    }

    public BitVector from(Stream stream, Event event){
        int eventBit = eventToBitArray[event.getEventType()];
        int streamBit = streamToBitArray[stream.getStreamID()];

        BitSet mask = new BitSet(bitCount);
        mask.set(eventBit);
        mask.set(streamBit);

        BitSet match = new BitSet(bitCount);
        match.set(eventBit);
        match.set(streamBit);

        return new BitVector(mask, match);
    }

    public BitVector from(AtomicPredicate atomicPredicate){
        int predicateBit = atomicPredicateList.indexOf(atomicPredicate);

        if (predicateBit < 0) {
            predicateBit = atomicPredicateList.size();
            atomicPredicateList.add(atomicPredicate);
        }

        predicateBit += predicateOffset;

        return getPredicateForBit(predicateBit);
    }
}
