package ktu.kaganndemirr.evaluator;

import ktu.kaganndemirr.message.Multicast;
import ktu.kaganndemirr.util.Bag;

import java.io.IOException;
import java.util.Map;

public interface Cost {

    void reset();

    double getTotalCost();

    String toDetailedString();

    Map<Multicast, Double> getWCDMap();

    void writeResultToFile(Bag bag) throws IOException;

}
