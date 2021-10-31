package edu.puc.core.execution.structures.CDS;

public class CDSUnionNode extends CDSNode {

    private final CDSNode left;
    private final CDSNode right;

    public CDSUnionNode(CDSNode left, CDSNode right) {
        if (left instanceof CDSOutputNode) {
            this.left = left;
            this.right = right;
        } else {
            if (right instanceof CDSOutputNode) {
                this.left = right;
                this.right = left;
            } else {
                CDSUnionNode tempLeft = (CDSUnionNode) left;
                CDSUnionNode tempRight = (CDSUnionNode) right;
                CDSUnionNode u2 = new CDSUnionNode(tempLeft.getRight(), tempRight.getRight());
                CDSUnionNode u1 = new CDSUnionNode(tempRight.getLeft(), u2);
                this.left = tempLeft.getLeft();
                this.right = u1;
            }
        }
    }

    public CDSNode getRight() {
        return right;
    }

    public boolean isBottom() {
        return false;
    }

    public CDSNode getLeft() {
        return left;
    }
}
