package ktu.kaganndemirr.routing.heuristic;

import ktu.kaganndemirr.evaluator.AVBLatencyMathCost;
import ktu.kaganndemirr.evaluator.Cost;
import ktu.kaganndemirr.message.Multicast;
import ktu.kaganndemirr.message.Unicast;
import ktu.kaganndemirr.message.UnicastCandidate;
import ktu.kaganndemirr.routing.KShortestPaths;
import ktu.kaganndemirr.solver.Solution;
import ktu.kaganndemirr.util.Bag;
import ktu.kaganndemirr.util.UMethods;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class U {
    private static final Logger logger = LoggerFactory.getLogger(U.class.getSimpleName());

    private long kShortestPathsDuration;

    private List<UnicastCandidate> ttUnicastCandidateList;
    private List<UnicastCandidate> srtUnicastCandidateList;

    private List<Unicast> ttUnicastList;
    private List<Unicast> srtUnicastList;

    private final List<Unicast> unicastList;

    private Cost cost;

    private List<Unicast> solution;

    private final Map<Double, Double> durationMap;

    public U(){
        kShortestPathsDuration = 0;
        unicastList = new ArrayList<>();
        cost = new AVBLatencyMathCost();
        solution = new ArrayList<>();
        durationMap = new HashMap<>();
    }

    public Solution solve(Bag bag){
        Instant yenKShortestPathsStartTime = Instant.now();
        KShortestPaths kShortestPaths = new KShortestPaths(bag);
        Instant yenKShortestPathsEndTime = Instant.now();
        kShortestPathsDuration = Duration.between(yenKShortestPathsStartTime, yenKShortestPathsEndTime).toMillis();

        ttUnicastCandidateList = kShortestPaths.getTTUnicastCandidateList();
        srtUnicastCandidateList = kShortestPaths.getSRTUnicastCandidateList();

        ttUnicastList = kShortestPaths.getTTUnicastList();
        srtUnicastList = kShortestPaths.getSRTUnicastList();

        unicastList.addAll(ttUnicastList);
        unicastList.addAll(srtUnicastList);

        solution = UMethods.constructSolution(bag, srtUnicastCandidateList, unicastList);

        cost = bag.getEvaluator().evaluate(solution);

        durationMap.put(Double.parseDouble(cost.toString().split("\\s")[0]), (kShortestPathsDuration / 1e3));

        return new Solution(cost, Multicast.generateMulticastList(solution));
    }

    public List<Unicast> getSolution() {
        return solution;
    }

    public List<UnicastCandidate> getTTUnicastCandidateList() {
        return ttUnicastCandidateList;
    }

    public List<UnicastCandidate> getSRTUnicastCandidateList() {
        return srtUnicastCandidateList;
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



