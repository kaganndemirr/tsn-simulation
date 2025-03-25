package ktu.kaganndemirr.routing.metaheuristic;

import ktu.kaganndemirr.evaluator.AVBLatencyMathCost;
import ktu.kaganndemirr.evaluator.Cost;
import ktu.kaganndemirr.evaluator.Evaluator;
import ktu.kaganndemirr.message.Multicast;
import ktu.kaganndemirr.message.Unicast;
import ktu.kaganndemirr.message.UnicastCandidate;
import ktu.kaganndemirr.routing.KShortestPaths;
import ktu.kaganndemirr.solver.Solution;
import ktu.kaganndemirr.util.Bag;
import ktu.kaganndemirr.util.Constants;
import ktu.kaganndemirr.util.LaursenMethods;
import ktu.kaganndemirr.util.MetaheuristicMethods;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static ktu.kaganndemirr.util.HelperMethods.createScenarioOutputPath;
import static ktu.kaganndemirr.util.HelperMethods.writeSolutionsToFile;

public class RO {
    private static final Logger logger = LoggerFactory.getLogger(RO.class.getSimpleName());

    private final int k;

    private long kShortestPathsDuration;

    private List<UnicastCandidate> ttUnicastCandidateList;
    private List<UnicastCandidate> srtUnicastCandidateList;

    private List<Unicast> ttUnicastList;
    private List<Unicast> srtUnicastList;

    private final List<Unicast> unicastList;

    private final Object writeLock;

    private final Object costLock;

    private Cost globalBestCost;

    private final List<Unicast> bestSolution;

    private Evaluator evaluator;

    private String scenarioOutputPath;

    private final Map<Double, Double> durationMap;

    public RO(int k){
        this.k = k;
        kShortestPathsDuration = 0;
        unicastList = new ArrayList<>();
        writeLock = new Object();
        costLock = new Object();
        globalBestCost = new AVBLatencyMathCost();
        bestSolution = new ArrayList<>();
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

        this.evaluator = bag.getEvaluator();

        scenarioOutputPath = createScenarioOutputPath(bag);

        new File(scenarioOutputPath).mkdirs();

        try (ExecutorService exec = Executors.newFixedThreadPool(bag.getThreadNumber())) {

            Timer timer = getTimer(Duration.ofSeconds(bag.getTimeout()));


            for (int i = 0; i < bag.getThreadNumber(); i++) {
                exec.execute(new LaursenRoutingOptimizationRunnable(bag));
            }

            exec.awaitTermination(Duration.ofSeconds(bag.getTimeout()).toSeconds(), TimeUnit.SECONDS);
            exec.shutdown();

            if (!exec.isTerminated()) {
                exec.shutdownNow();
            }

            timer.cancel();

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
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
                    float searchProgress = (++i * (float) Constants.PROGRESS_PERIOD_SECOND) / duration.toMillis();
                    logger.info("Searching {} %: CurrentBest {}", numberFormat.format(searchProgress * 100), globalBestCost);
                }
            };

            timer.schedule(progressUpdater, Constants.PROGRESS_PERIOD_SECOND, Constants.PROGRESS_PERIOD_SECOND);
        }
        return timer;
    }

    private class LaursenRoutingOptimizationRunnable implements Runnable {
        private int i = 0;
        Instant solutionStartTime = Instant.now();

        private final Bag bag;

        public LaursenRoutingOptimizationRunnable(Bag bag){
            this.bag = bag;
        }

        @Override
        public void run() {
            String threadName = Thread.currentThread().getName();

            while (!Thread.currentThread().isInterrupted()) {
                i++;


                List<Unicast> solution = null;
                List<Unicast> initialSolution = null;

                if(Objects.equals(bag.getMetaheuristicName(), Constants.GRASP)){
                    initialSolution = LaursenMethods.constructInitialSolution(srtUnicastCandidateList, unicastList, k, evaluator);
                    solution = MetaheuristicMethods.GRASP(initialSolution, evaluator, srtUnicastCandidateList, globalBestCost);
                } else if (Objects.equals(bag.getMetaheuristicName(), Constants.ALO)) {
                    initialSolution = LaursenMethods.constructInitialSolution(srtUnicastCandidateList, unicastList, k, evaluator);
                    solution = MetaheuristicMethods.ALO(initialSolution, initialSolution, srtUnicastCandidateList, k, evaluator);
                }

                if (logger.isDebugEnabled()) {
                    synchronized (writeLock) {
                        assert solution != null;
                        try {
                            writeSolutionsToFile(initialSolution, solution, scenarioOutputPath, threadName, i);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }

                //Evaluate and see if better than anything we have seen before
                Cost cost = evaluator.evaluate(solution);
                //pre-check before entering critical-section
                if (cost.getTotalCost() < globalBestCost.getTotalCost()) {
                    synchronized (costLock) {
                        if (cost.getTotalCost() < globalBestCost.getTotalCost()) {
                            globalBestCost = cost;
                            Instant solutionEndTime = Instant.now();
                            durationMap.put(Double.parseDouble(globalBestCost.toString().split("\\s")[0]), (Duration.between(solutionStartTime, solutionEndTime).toMillis() / 1e3) + kShortestPathsDuration / 1e3);
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


