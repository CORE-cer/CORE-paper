package edu.puc.core.execution.structures.CDS;

public abstract class CDSNode {
    public static final CDSNode BOTTOM = new CDSNode() {
        @Override
        public int getPaths() {
            return 0;
        }

        @Override
        public boolean isBottom() {
            return true;
        }
    };

    /** The number of paths below this */
    abstract public int getPaths();

    abstract public boolean isBottom();
}
