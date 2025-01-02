package ktu.kaganndemirr.solver;

import ktu.kaganndemirr.evaluator.Cost;
import ktu.kaganndemirr.message.Multicast;
import ktu.kaganndemirr.message.Unicast;
import ktu.kaganndemirr.message.UnicastCandidate;
import ktu.kaganndemirr.routing.phy.yen.YenKShortestPaths;
import ktu.kaganndemirr.routing.phy.yen.heuristic.WPMDeadline;
import ktu.kaganndemirr.util.Constants;
import ktu.kaganndemirr.util.WPMMethods;
import ktu.kaganndemirr.util.constants.SolverConstants;
import ktu.kaganndemirr.util.holders.Bag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Solver {
    private static final Logger logger = LoggerFactory.getLogger(WPMDeadline.class.getSimpleName());

    private List<UnicastCandidate> srtUnicastCandidateList;
    private List<Unicast> ttUnicastList;

    private List<Unicast> solution;

    private final Map<Double, Double> durationMap = new HashMap<>();


    public Solution solve(Bag bag){
        Instant pathFinderMethodStartTime;
        Instant pathFinderMethodEndTime;
        long pathFinderMethodTime = 0;
        long solverTime = 0;
        switch (bag.getRouting()) {
            case SolverConstants.PHY -> {
                switch (bag.getPathFindingMethod()) {
                    case SolverConstants.SHORTEST_PATH -> {
                        switch (bag.getAlgorithm()) {
                            case SolverConstants.DIJKSTRA -> {
                                pathFinderMethodStartTime = Instant.now();
                                DijkstraShortestPath dijkstraShortestPath = new DijkstraShortestPath(bag);
                                pathFinderMethodEndTime = Instant.now();
                                pathFinderMethodTime = Duration.between(pathFinderMethodStartTime, pathFinderMethodEndTime).toMillis();

                                solution.addAll(dijkstraShortestPath.getSRTUnicastList());
                                solution.addAll(dijkstraShortestPath.getTTUnicastList());
                            }
                        }
                    }
                    case SolverConstants.YEN -> {
                        pathFinderMethodStartTime = Instant.now();
                        YenKShortestPaths yenKShortestPaths = new YenKShortestPaths(bag.getGraph(), bag.getApplicationList(), k);
                        pathFinderMethodEndTime = Instant.now();
                        pathFinderMethodTime = Duration.between(pathFinderMethodStartTime, pathFinderMethodEndTime).toSeconds();

                        srtUnicastCandidateList = yenKShortestPaths.getSRTUnicastCandidateList();
                        ttUnicastList = yenKShortestPaths.getTTUnicastList();

                        Instant solverStartTime = Instant.now();
                        List<Unicast> wpmSolution = WPMMethods.constructSolution(srtUnicastCandidateList, ttUnicastList, bag);
                        Instant solverEndTime = Instant.now();
                        solverTime = Duration.between(solverStartTime, solverEndTime).toMillis();
                    }
                }
            }
        }

        Cost cost = bag.getEvaluator().evaluate(solution);

        durationMap.put(Double.parseDouble(cost.toString().split("\\s")[0]), (pathFinderMethodTime / 1e3) + (solverTime) / 1e3);

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

    private List<Unicast> wpmSolver(Bag bag) {
        if (Objects.equals(bag.getWPMObjective(), Constants.SRT_TT)){
            solution = null;
        } else if (Objects.equals(bag.getWPMObjective(), Constants.SRT_TT_LENGTH)) {
            solution =
        } else if (Objects.equals(bag.getWPMObjective(), Constants.SRT_TT_LENGTH_UTIL)) {
            solution = null;
        }
    }
}
