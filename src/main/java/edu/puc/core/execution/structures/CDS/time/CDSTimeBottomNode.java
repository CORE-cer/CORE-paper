package edu.puc.core.execution.structures.CDS.time;

public class CDSTimeBottomNode extends CDSTimeNode {

    private final long mm;

    // Use with care since the current time is always wrong!
    public static final CDSTimeBottomNode BOTTOM = new CDSTimeBottomNode(0);

    public CDSTimeBottomNode(long currentTime) {
        this.mm = currentTime;
    }

    @Override
    public long getMm() {
        return mm;
    }

    @Override
    public boolean isBottom() {
        return true;
    }

    @Override
    public int getPaths() {
        return 1;
    }
}

