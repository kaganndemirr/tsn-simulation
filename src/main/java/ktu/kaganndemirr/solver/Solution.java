package ktu.kaganndemirr.solver;

import ktu.kaganndemirr.evaluator.Cost;
import ktu.kaganndemirr.message.Multicast;

import java.util.List;

public class Solution {
    private final Cost cost;
    private final List<Multicast> multicastList;

    public Solution(Cost cost, List<Multicast> multicastList) {
        this.cost = cost;
        this.multicastList = multicastList;
    }

    public List<Multicast> getMulticastList() {
        return multicastList;
    }

    public Cost getCost() {
        return cost;
    }
}
