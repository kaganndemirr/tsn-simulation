package ktu.kaganndemirr.routing.phy.pathpenalization.metaheuristic;

import ktu.kaganndemirr.application.Application;
import ktu.kaganndemirr.architecture.GCLEdge;
import ktu.kaganndemirr.architecture.Node;
import ktu.kaganndemirr.evaluator.AVBLatencyMathCost;
import ktu.kaganndemirr.evaluator.Cost;
import ktu.kaganndemirr.evaluator.Evaluator;
import ktu.kaganndemirr.message.Multicast;
import ktu.kaganndemirr.message.Unicast;
import ktu.kaganndemirr.message.UnicastCandidate;
import ktu.kaganndemirr.routing.phy.pathpenalization.RandomizedPathPenalizationKShortestPaths;
import ktu.kaganndemirr.solver.Solution;
import ktu.kaganndemirr.util.Bag;
import ktu.kaganndemirr.util.Constants;
import ktu.kaganndemirr.util.MetaheuristicMethods;
import ktu.kaganndemirr.util.mcdm.MCDMConstants;
import org.jgrapht.Graph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class WPMLWRCWRDeadline {
    private static final Logger logger = LoggerFactory.getLogger(WPMLWR.class.getSimpleName());

    private final int k;

    private List<Unicast> ttUnicastList;

    private final Map<Double, Double> durationMap;

    private Graph<Node, GCLEdge> graph;

    private List<Application> applicationList;

    private List<UnicastCandidate> srtUnicastCandidateList;

    private final Object costLock;

    private Cost globalBestCost;

    private final List<Unicast> bestSolution;

    private Evaluator evaluator;

    public WPMLWRCWRDeadline(int k){
        this.k = k;
        costLock = new Object();
        durationMap = new HashMap<>();
        globalBestCost = new AVBLatencyMathCost();
        bestSolution = new ArrayList<>();
    }

    public Solution solve(Graph<Node, GCLEdge> graph, List<Application> applicationList, Bag bag, int threadNumber, Evaluator evaluator, Duration timeout){
        ttUnicastList = RandomizedPathPenalizationKShortestPaths.getTTUnicastList(applicationList);

        this.graph = graph;
        this.applicationList = applicationList;

        this.evaluator = evaluator;

        try (ExecutorService exec = Executors.newFixedThreadPool(threadNumber)) {

            Timer timer = getTimer(timeout);

            for (int i = 0; i < threadNumber; i++) {
                exec.execute(new WPMLWRCWRDeadlineRunnable(bag));
            }

            exec.awaitTermination(timeout.toSeconds(), TimeUnit.SECONDS);
            exec.shutdown();

            if (!exec.isTerminated()) {
                exec.shutdownNow();
            }

            timer.cancel();

        } catch (InterruptedException e) {
            logger.info("Executor interrupted");
        }
        return new Solution(globalBestCost, Multicast.generateMulticastList(bestSolution));
    }

    private Timer getTimer(Duration duration) {
        Timer timer = new Timer();
        if (logger.isInfoEnabled()) {
            TimerTask progressUpdater = new TimerTask() {
                private int i = 0;
                private final DecimalFormat numberFormat = new DecimalFormat(".00");

                @Override
                public void run() {
                    //Report progress every 10sec

                    float searchProgress = (++i * (float) Constants.PROGRESS_PERIOD_SECOND) / duration.toMillis();
                    logger.info("Searching {} %: CurrentBest {}", numberFormat.format(searchProgress * 100), globalBestCost);
                }
            };

            timer.schedule(progressUpdater, Constants.PROGRESS_PERIOD_SECOND, Constants.PROGRESS_PERIOD_SECOND);
        }
        return timer;
    }

    private class WPMLWRCWRDeadlineRunnable implements Runnable {
        private int i = 0;
        Instant solutionStartTime = Instant.now();

        private final Bag bag;

        public WPMLWRCWRDeadlineRunnable(Bag bag) {
            this.bag = bag;
        }

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                i++;
                RandomizedPathPenalizationKShortestPaths randomizedPathPenalizationKShortestPaths = new RandomizedPathPenalizationKShortestPaths(graph, applicationList, bag, k);

                srtUnicastCandidateList = randomizedPathPenalizationKShortestPaths.getSRTUnicastCandidateList();

                List<Unicast> initialSolution = null;
                if (Objects.equals(bag.getMCDMObjective(), Constants.SRT_TT)){
                    //TODO
                } else if (Objects.equals(bag.getMCDMObjective(), Constants.SRT_TT_LENGTH)) {
                    if(Objects.equals(bag.getCWR(), MCDMConstants.THREAD_LOCAL_RANDOM)){
                        initialSolution = null;
                    }
                } else if (Objects.equals(bag.getMCDMObjective(), Constants.SRT_TT_LENGTH_UTIL)) {
                    //TODO
                }

                List<Unicast> solution = null;
                if (Objects.equals(bag.getMetaheuristicName(), Constants.GRASP)){
                    solution = MetaheuristicMethods.GRASP(initialSolution, evaluator, srtUnicastCandidateList, globalBestCost);
                } else if (Objects.equals(bag.getMetaheuristicName(), Constants.ALO)) {
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
        return bestSolution;
    }

    public List<UnicastCandidate> getSRTUnicastCandidateList() {
        return srtUnicastCandidateList;
    }

    public Map<Double, Double> getDurationMap() {
        return durationMap;
    }
}


