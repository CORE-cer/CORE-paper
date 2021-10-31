package edu.puc.core.execution.structures.CDS.time;

public class CDSTimeBottomNode extends CDSTimeNode {

    CDSTimeBottomNode(long currentTime) {
        this.mm = currentTime;
    }

    public boolean isBottom() {
        return true;
    }
}

