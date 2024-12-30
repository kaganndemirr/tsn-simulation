package ktu.kaganndemirr.evaluator;

import ktu.kaganndemirr.message.Multicast;

import java.util.Map;

public interface Cost {

    void reset();

    double getTotalCost();

    String toDetailedString();

    Map<Multicast, Double> getWorstCaseDelayMap();

}
