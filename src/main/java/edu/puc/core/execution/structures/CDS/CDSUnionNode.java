package edu.puc.core.execution.structures.CDS;

public class CDSUnionNode extends CDSNode {

    private final CDSNode left;
    private final CDSNode right;

    // We could always compute paths from getPaths but this would need
    // to traverse the whole structure, and we want to do it in constant time.
    private final int paths;

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
                // TODO
                // In the paper here they were comparing max(left.right) <= max(right.right) but max is missing in the DS.
                // So they always picks max(left.right) >= max(right.right) [Figure 5(c)].
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
