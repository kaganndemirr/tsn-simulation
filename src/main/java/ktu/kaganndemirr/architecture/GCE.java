package ktu.kaganndemirr.architecture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GCE {
    private static final Logger logger = LoggerFactory.getLogger(GCE.class.getSimpleName());

    private final double start;
    private final double end;

    public GCE(double start, double end) {
        this.start = start;
        this.end = end;
    }

    public double getStart() {
        return start;
    }

    public double getEnd() {
        return end;
    }

    public double getDuration() {
        return end - start;
    }

    @Override
    public String toString() {
        return "[" + start + "-" + end + "]";
    }
}
