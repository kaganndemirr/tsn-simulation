package ktu.kaganndemirr.evaluator;

import ktu.kaganndemirr.message.Multicast;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class AVBLatencyMathCost implements Cost, Comparator<AVBLatencyMathCost> {

    private static Logger logger = LoggerFactory.getLogger(AVBLatencyMathCost.class.getSimpleName());

    private double o1;
    private double o2;
    private double o3;

    private boolean isUsed;

    private final Map<Multicast, Double> worstCaseDelayMap = new HashMap<>();

    public AVBLatencyMathCost() {
        reset();
    }

    public void setWCD(Multicast multicast, Double worstCaseDelay) {
        worstCaseDelayMap.put(multicast, worstCaseDelay);
    }

    public void add(O objective, double value) {
        isUsed = true;
        switch (objective) {
            case one -> o1 += value;
            case two -> o2 += value;
            case three -> o3 += value;
        }
    }

    @Override
    public double getTotalCost() {
        if (!isUsed) {
            return Double.MAX_VALUE;
        }
        double w1 = 10000;
        double w2 = 3.0;
        double w3 = 1.0;
        return w1 * o1 + w2 * o2 + w3 * o3;
    }

    @Override
    public void reset() {
        isUsed = false;
        o1 = 0.0;
        o2 = 0.0;
        o3 = 0.0;
    }

    @Override
    public int compare(AVBLatencyMathCost o1, AVBLatencyMathCost o2) {
        return (int) Math.round(o1.getTotalCost() - o2.getTotalCost());
    }

    public enum O {
        one, two, three;
    }

    public String toString() {
        return getTotalCost() + " (current o1 = " + o1 + ", o2 = " + o2 + ", o3 = " + o3 + ")";
    }

    public String toDetailedString() {
        return "Total : " + this + " | o1 " + o1 + ", o2 " + o2 + ", o3 " + o3 + " -- " + worstCaseDelayMap + " --";
    }

    @Override
    public Map<Multicast, Double> getWorstCaseDelayMap() {
        return worstCaseDelayMap;
    }

}
