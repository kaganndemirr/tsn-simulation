package ktu.kaganndemirr.routing.phy.yen.heuristic;

import ktu.kaganndemirr.application.Application;
import ktu.kaganndemirr.architecture.GCLEdge;
import ktu.kaganndemirr.architecture.Node;
import ktu.kaganndemirr.evaluator.Cost;
import ktu.kaganndemirr.evaluator.Evaluator;
import ktu.kaganndemirr.message.Multicast;
import ktu.kaganndemirr.message.Unicast;
import ktu.kaganndemirr.message.UnicastCandidate;
import ktu.kaganndemirr.routing.phy.yen.YenKShortestPaths;
import ktu.kaganndemirr.solver.Solution;
import ktu.kaganndemirr.util.Bag;
import ktu.kaganndemirr.util.Constants;
import ktu.kaganndemirr.util.mcdm.WPMMethods;
import org.jgrapht.Graph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class WPMDeadline {
    private static final Logger logger = LoggerFactory.getLogger(WPMDeadline.class.getSimpleName());

    private final int k;

    private List<UnicastCandidate> srtUnicastCandidateList;

    private List<Unicast> solution;

    private final Map<Double, Double> durationMap;


    public WPMDeadline(int k) {
        this.k = k;
        this.durationMap = new HashMap<>();
    }

    public Solution solve(Graph<Node, GCLEdge> graph, List<Application> applicationList, Bag bag, Evaluator evaluator) throws IOException {
        Instant graphPathsStartTime = Instant.now();
        YenKShortestPaths yenKShortestPaths = new YenKShortestPaths(bag);
        Instant graphPathsEndTime = Instant.now();
        long graphPathsDuration = Duration.between(graphPathsStartTime, graphPathsEndTime).toMillis();

        srtUnicastCandidateList = yenKShortestPaths.getSRTUnicastCandidateList();
        List<Unicast> ttUnicastList = yenKShortestPaths.getTTUnicastList();

        Instant solutionStartTime = Instant.now();
        if (Objects.equals(bag.getMCDMObjective(), Constants.SRT_TT)){
            solution = null;
        } else if (Objects.equals(bag.getMCDMObjective(), Constants.SRT_TT_LENGTH)) {
            solution = WPMMethods.srtTTLength(bag, srtUnicastCandidateList, ttUnicastList, null);
        } else if (Objects.equals(bag.getMCDMObjective(), Constants.SRT_TT_LENGTH_UTIL)) {
            solution = null;
        }

        Instant solutionEndTime = Instant.now();
        long solutionDuration = Duration.between(solutionStartTime, solutionEndTime).toMillis();
        Cost cost = evaluator.evaluate(solution);

        durationMap.put(Double.parseDouble(cost.toString().split("\\s")[0]), (solutionDuration / 1e3) + (graphPathsDuration) / 1e3);

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
