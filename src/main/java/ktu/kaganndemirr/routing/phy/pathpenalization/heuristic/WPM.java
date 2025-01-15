package ktu.kaganndemirr.routing.phy.pathpenalization.heuristic;

import ktu.kaganndemirr.application.Application;
import ktu.kaganndemirr.architecture.GCLEdge;
import ktu.kaganndemirr.architecture.Node;
import ktu.kaganndemirr.evaluator.Cost;
import ktu.kaganndemirr.evaluator.Evaluator;
import ktu.kaganndemirr.message.Multicast;
import ktu.kaganndemirr.message.Unicast;
import ktu.kaganndemirr.message.UnicastCandidate;
import ktu.kaganndemirr.routing.phy.pathpenalization.PathPenalizationKShortestPaths;
import ktu.kaganndemirr.solver.Solution;
import ktu.kaganndemirr.util.Bag;
import ktu.kaganndemirr.util.Constants;
import ktu.kaganndemirr.util.mcdm.WPMMethods;
import org.jgrapht.Graph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class WPM {
    private static final Logger logger = LoggerFactory.getLogger(ktu.kaganndemirr.routing.phy.yen.heuristic.WPMDeadline.class.getSimpleName());

    private List<UnicastCandidate> srtUnicastCandidateList;

    private List<Unicast> solution;

    private final Map<Double, Double> durationMap;

    public WPM() {
        this.durationMap = new HashMap<>();
    }

    public Solution solve(Bag bag){
        Instant pathPenalizationKShortestPathsStartTime = Instant.now();
        PathPenalizationKShortestPaths pathPenalizationKShortestPaths = new PathPenalizationKShortestPaths(bag);
        Instant pathPenalizationKShortestPathsEndTime = Instant.now();
        long pathPenalizationKShortestPathsDuration = Duration.between(pathPenalizationKShortestPathsStartTime, pathPenalizationKShortestPathsEndTime).toMillis();

        srtUnicastCandidateList = pathPenalizationKShortestPaths.getSRTUnicastCandidateList();
        List<Unicast> ttUnicastList = pathPenalizationKShortestPaths.getTTUnicastList();

        Instant solutionStartTime = Instant.now();
        if (Objects.equals(bag.getMCDMObjective(), Constants.SRT_TT)){
            solution = null;
        } else if (Objects.equals(bag.getMCDMObjective(), Constants.SRT_TT_LENGTH)) {
            solution = WPMMethods.srtTTLength(bag, srtUnicastCandidateList, ttUnicastList);
        } else if (Objects.equals(bag.getMCDMObjective(), Constants.SRT_TT_LENGTH_UTIL)) {
            solution = null;
        }

        Instant solutionEndTime = Instant.now();
        long solutionDuration = Duration.between(solutionStartTime, solutionEndTime).toMillis();
        Cost cost = bag.getEvaluator().evaluate(solution);

        durationMap.put(Double.parseDouble(cost.toString().split("\\s")[0]), (solutionDuration / 1e3) + (pathPenalizationKShortestPathsDuration) / 1e3);

        return new Solution(cost, Multicast.generateMulticastList(solution));
    }

    public List<Unicast> getSolution() {
        return solution;
    }

    public List<UnicastCandidate> getSRTUnicastCandidateList() {
        return srtUnicastCandidateList;
    }

    public Map<Double, Double> getDurationMap() {
        return durationMap;
    }

}