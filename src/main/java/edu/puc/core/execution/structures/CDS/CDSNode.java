package edu.puc.core.execution.structures.CDS;

public abstract class CDSNode {
    public static final CDSNode BOTTOM = new CDSNode() {
        @Override
        public int getPaths() {
            return 1;
        }

        @Override
        public boolean isBottom() {
            return true;
        }

        @Override
        public String toString() {
            return "Bottom";
        }
    };

    /** The number of paths from this node */
    abstract public int getPaths();

    abstract public boolean isBottom();
}
