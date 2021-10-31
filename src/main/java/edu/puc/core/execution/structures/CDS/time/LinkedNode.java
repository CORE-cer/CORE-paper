package edu.puc.core.execution.structures.CDS.time;


public class LinkedNode {

    private LinkedNode next;
    private final CDSTimeNode data;

    LinkedNode(CDSTimeNode data) {
        this.data = data;
    }

    CDSTimeNode getData() {
        return data;
    }

    LinkedNode getNext() {
        return next;
    }

    void setNext(LinkedNode next) {
        this.next = next;
    }
}
