package edu.puc.core.execution.structures.CDS.time;

import java.lang.ref.WeakReference;

public class CDSTimeUnionNode extends CDSTimeNode {

    private final WeakReference<CDSTimeNode> left;
    private final WeakReference<CDSTimeNode> right;


    CDSTimeUnionNode(WeakReference<CDSTimeNode> left, WeakReference<CDSTimeNode> right) {
        CDSTimeNode tempLeft = left.get();
        if (tempLeft != null) {
            this.mm = tempLeft.getMm();
        }
        this.left = left;
        this.right = right;
    }


    public CDSTimeNode getRight() {
        return right.get();
    }

    public WeakReference<CDSTimeNode> getRightReference() {
        return right;
    }

    public WeakReference<CDSTimeNode> getLeftReference() {
        return left;
    }

    public CDSTimeNode getLeft() {
        return left.get();
    }

    @Override
    public boolean isBottom() {
        return false;
    }
}
