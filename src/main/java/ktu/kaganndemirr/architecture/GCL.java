package ktu.kaganndemirr.architecture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class GCL {
    private static final Logger logger = LoggerFactory.getLogger(GCL.class.getSimpleName());

    private final double offset;
    private final double duration;
    private final int frequency;

    public GCL(double offset, double duration, int frequency) {
        this.offset = offset;
        this.duration = duration;
        this.frequency = frequency;
    }

    public double getOffset() {
        return offset;
    }

    public double getDuration() {
        return duration;
    }

    public int getFrequency() {
        return frequency;
    }

    public String toString() {
        return "<" + offset + "," + duration + "," + frequency + ">";
    }

    @Override
    public boolean equals(Object obj) {
        boolean result;
        if (obj == null || getClass() != obj.getClass()) {
            result = false;
        } else {
            GCL gcl = (GCL) obj;
            result = (offset == gcl.offset) && (duration == gcl.duration) && (frequency == gcl.frequency);
        }
        return result;
    }

    @Override
    public int hashCode() {
        return Objects.hash(offset) + Objects.hash(duration) + Objects.hash(frequency);
    }
}
