package edu.puc.core.execution.structures.CDS.time;

import java.util.Iterator;

/** Linked list of T that is only used by CDSNodeManager to keep strong references to the nodes */
public class LinkedNodeList implements Iterable<LinkedNode> {

    private LinkedNode head;
    private LinkedNode tail;

    LinkedNodeList() {}

    public void add(LinkedNode newNode) {
        if (head == null) {
            head = tail = newNode;
            return;
        }
        tail.setNext(newNode);
        tail = newNode;
    }

    void setHead(LinkedNode head) {
        this.head = head;
    }

    public Iterator<LinkedNode> iterator() {
        return new Iterator<LinkedNode>() {

            private LinkedNode current = head;

            @Override
            public boolean hasNext() {
                return current != null;
            }

            @Override
            public LinkedNode next() {
                LinkedNode toRet = current;
                current = current.getNext();
                return toRet;
            }
        };
    }
}