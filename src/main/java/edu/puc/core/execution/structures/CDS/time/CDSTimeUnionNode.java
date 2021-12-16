package edu.puc.core.execution.structures.CDS.time;

import java.lang.ref.WeakReference;

public class CDSTimeUnionNode extends CDSTimeNode {

    private final WeakReference<CDSTimeNode> left;
    private final WeakReference<CDSTimeNode> right;
    private final long mm;
    private final int paths;

    CDSTimeUnionNode(WeakReference<CDSTimeNode> left, WeakReference<CDSTimeNode> right) {
        CDSTimeNode tempLeft = left.get();
        CDSTimeNode tempRight = left.get();
        this.left = left;
        this.right = right;
        this.mm = tempLeft != null ? tempLeft.getMm() : 0;
        this.paths = (tempLeft != null ? tempLeft.getPaths() : 0) + (tempRight != null ? tempRight.getPaths() : 0);
    }

    public CDSTimeNode getRight() {
        return this.right.get();
    }

    public WeakReference<CDSTimeNode> getRightReference() {
        return this.right;
    }

    public WeakReference<CDSTimeNode> getLeftReference() {
        return this.left;
    }

    public CDSTimeNode getLeft() {
        return this.left.get();
    }

    @Override
    public boolean isBottom() {
        return false;
    }

    @Override
    public long getMm() {
        return this.mm;
    }

    @Override
    public int getPaths() {
        return this.paths;
    }
}
