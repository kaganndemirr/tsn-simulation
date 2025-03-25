package ktu.kaganndemirr.routing;

import ktu.kaganndemirr.evaluator.AVBLatencyMathCost;
import ktu.kaganndemirr.evaluator.Cost;
import ktu.kaganndemirr.message.Multicast;
import ktu.kaganndemirr.message.Unicast;
import ktu.kaganndemirr.solver.Solution;
import ktu.kaganndemirr.util.Bag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShortestPath {
    private static final Logger logger = LoggerFactory.getLogger(ShortestPath.class.getSimpleName());

    private long shortestPathDuration;

    private List<Unicast> ttUnicastList;
    private List<Unicast> srtUnicastList;

    private final List<Unicast> unicastList;

    private Cost cost;


    private final Map<Double, Double> durationMap;

    public ShortestPath(){
        shortestPathDuration = 0;
        unicastList = new ArrayList<>();
        cost = new AVBLatencyMathCost();
        durationMap = new HashMap<>();
    }

    public Solution solve(Bag bag){
        Instant shortestPathStartTime = Instant.now();
        ShortestPaths shortestPaths = new ShortestPaths(bag);
        Instant shortestPathEndTime = Instant.now();
        shortestPathDuration = Duration.between(shortestPathStartTime, shortestPathEndTime).toMillis();

        ttUnicastList = shortestPaths.getTTUnicastList();
        srtUnicastList = shortestPaths.getSRTUnicastList();

        unicastList.addAll(ttUnicastList);
        unicastList.addAll(srtUnicastList);

        cost = bag.getEvaluator().evaluate(unicastList);

        durationMap.put(Double.parseDouble(cost.toString().split("\\s")[0]), (shortestPathDuration / 1e3));

        return new Solution(cost, Multicast.generateMulticastList(unicastList));
    }

    public List<Unicast> getSolution() {
        return unicastList;
    }

    public List<Unicast> getTTUnicastList() {
        return ttUnicastList;
    }

    public List<Unicast> getSRTUnicastList() {
        return srtUnicastList;
    }

    public Map<Double, Double> getDurationMap() {
        return durationMap;
    }
}



