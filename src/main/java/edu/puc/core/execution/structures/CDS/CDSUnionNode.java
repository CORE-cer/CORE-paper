package edu.puc.core.execution.structures.CDS;

public class CDSUnionNode extends CDSNode {

    private final CDSNode left;
    private final CDSNode right;

    // We could always compute paths from getPaths but this would need
    // to traverse the whole structure, and we want to do it in constant time.
    private final int paths;

    public CDSUnionNode(CDSNode left, CDSNode right) {
        if (left instanceof CDSOutputNode || left == BOTTOM) {
            this.left = left;
            this.right = right;
        } else {
            if (right instanceof CDSOutputNode || right == BOTTOM) {
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
        this.paths = this.left.getPaths() + this.right.getPaths();
    }

    public CDSNode getLeft() {
        return left;
    }

    public CDSNode getRight() {
        return right;
    }

    @Override
    public boolean isBottom() {
        return false;
    }

    @Override
    public int getPaths() {
        return this.paths;
    }
}
