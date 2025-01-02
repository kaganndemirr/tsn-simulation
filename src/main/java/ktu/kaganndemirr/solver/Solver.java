package ktu.kaganndemirr.solver;

import ktu.kaganndemirr.evaluator.Cost;
import ktu.kaganndemirr.message.Multicast;
import ktu.kaganndemirr.message.Unicast;
import ktu.kaganndemirr.message.UnicastCandidate;
import ktu.kaganndemirr.routing.phy.yen.heuristic.WPMDeadline;
import ktu.kaganndemirr.util.MetaheuristicMethods;
import ktu.kaganndemirr.util.WPMMethods;
import ktu.kaganndemirr.util.constants.Constants;
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

        DijkstraShortestPath dijkstraShortestPath;
        YenKShortestPaths yenKShortestPaths = null;
        PathPenalizationKShortestPaths pathPenalizationKShortestPaths = null;

        if(Objects.equals(bag.getPathFindingMethod(), SolverConstants.SHORTEST_PATH)){
            pathFinderMethodStartTime = Instant.now();
            dijkstraShortestPath = new DijkstraShortestPath(bag);
            pathFinderMethodEndTime = Instant.now();
            pathFinderMethodTime = Duration.between(pathFinderMethodStartTime, pathFinderMethodEndTime).toMillis();

            solution.addAll(dijkstraShortestPath.getSRTUnicastList());
            solution.addAll(dijkstraShortestPath.getTTUnicastList());
        } else if (Objects.equals(bag.getPathFindingMethod(), SolverConstants.YEN)) {
            pathFinderMethodStartTime = Instant.now();
            yenKShortestPaths = new YenKShortestPaths(bag);
            pathFinderMethodEndTime = Instant.now();
            pathFinderMethodTime = Duration.between(pathFinderMethodStartTime, pathFinderMethodEndTime).toSeconds();


        } else if (Objects.equals(bag.getPathFindingMethod(), SolverConstants.PATH_PENALIZATION)) {
            pathFinderMethodStartTime = Instant.now();
            pathPenalizationKShortestPaths = new PathPenalizationKShortestPaths(bag);
            pathFinderMethodEndTime = Instant.now();
            pathFinderMethodTime = Duration.between(pathFinderMethodStartTime, pathFinderMethodEndTime).toSeconds();
        }

        if(Objects.equals(bag.getAlgorithm(), SolverConstants.WPM_HEURISTIC)){
            assert yenKShortestPaths != null;
            srtUnicastCandidateList = yenKShortestPaths.getSRTUnicastCandidateList();
            ttUnicastList = yenKShortestPaths.getTTUnicastList();

            Instant solverStartTime = Instant.now();
            solution = WPMMethods.constructSolution(srtUnicastCandidateList, ttUnicastList, bag);
            Instant solverEndTime = Instant.now();
            solverTime = Duration.between(solverStartTime, solverEndTime).toMillis();
        }

        Cost cost = bag.getEvaluator().evaluate(solution);

        durationMap.put(Double.parseDouble(cost.toString().split("\\s")[0]), (pathFinderMethodTime / 1e3) + (solverTime) / 1e3);

        return new Solution(cost, Multicast.generateMulticastList(solution));
    }

    private class WPMRunnable implements Runnable {
        private int i = 0;
        Instant solutionStartTime = Instant.now();

        private final Bag bag;
        public WPMRunnable(Bag bag) {
            this.bag = bag;
        }

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                i++;

                List<Unicast> initialSolution = null;
                if (Objects.equals(wpmObjective, Constants.SRT_TT)){
                    //TODO
                } else if (Objects.equals(wpmObjective, Constants.SRT_TT_LENGTH)) {
                    if(Objects.equals(cwr, Constants.THREAD_LOCAL_RANDOM)){
                        initialSolution = WPMMethods.deadlineCWRSRTTTLength(srtUnicastCandidateList, ttUnicastList, wpmVersion, wpmValueType);
                    }
                } else if (Objects.equals(wpmObjective, Constants.SRT_TT_LENGTH_UTIL)) {
                    //TODO
                }


                List<Unicast> solution = null;
                if (Objects.equals(metaheuristicName, Constants.GRASP)){
                    solution = MetaheuristicMethods.GRASP(initialSolution, evaluator, srtUnicastCandidateList, globalBestCost);
                } else if (Objects.equals(metaheuristicName, Constants.ALO)) {
                    solution = MetaheuristicMethods.ALO(initialSolution, initialSolution, srtUnicastCandidateList, k, evaluator);
                }

                //Evaluate and see if better than anything we have seen before
                Cost cost = evaluator.evaluate(solution);
                //pre-check before entering critical-section
                if (cost.getTotalCost() < globalBestCost.getTotalCost()) {
                    synchronized (costLock) {
                        if (cost.getTotalCost() < globalBestCost.getTotalCost()) {
                            globalBestCost = cost;
                            Instant solutionEndTime = Instant.now();
                            durationMap.put(Double.parseDouble(globalBestCost.toString().split("\\s")[0]), (Duration.between(solutionStartTime, solutionEndTime).toMillis() / 1e3));
                            bestSolution.clear();
                            assert solution != null;
                            bestSolution.addAll(solution);
                        }
                    }
                }
            }

            logger.info(" {} finished in {} iterations", Thread.currentThread().getName(), i);
        }
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
