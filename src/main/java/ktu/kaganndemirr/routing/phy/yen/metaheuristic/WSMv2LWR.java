package ktu.kaganndemirr.routing.phy.yen.metaheuristic;

import ktu.kaganndemirr.application.Application;
import ktu.kaganndemirr.architecture.GCLEdge;
import ktu.kaganndemirr.architecture.Node;
import ktu.kaganndemirr.evaluator.AVBLatencyMathCost;
import ktu.kaganndemirr.evaluator.Cost;
import ktu.kaganndemirr.evaluator.Evaluator;
import ktu.kaganndemirr.message.Multicast;
import ktu.kaganndemirr.message.Unicast;
import ktu.kaganndemirr.message.UnicastCandidate;
import ktu.kaganndemirr.routing.phy.yen.YenKShortestPaths;
import ktu.kaganndemirr.routing.phy.yen.YenMCDMKShortestPaths;
import ktu.kaganndemirr.routing.phy.yen.YenRandomizedKShortestPaths;
import ktu.kaganndemirr.solver.Solution;
import ktu.kaganndemirr.util.Bag;
import ktu.kaganndemirr.util.Constants;
import ktu.kaganndemirr.util.LaursenMethods;
import ktu.kaganndemirr.util.MetaheuristicMethods;
import ktu.kaganndemirr.util.mcdm.WPMMethods;
import org.jgrapht.Graph;
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

import static ktu.kaganndemirr.routing.phy.yen.YenMCDMKShortestPaths.getTTUnicastList;
import static ktu.kaganndemirr.util.HelperMethods.*;

public class WSMv2LWR {
    private static final Logger logger = LoggerFactory.getLogger(WSMv2LWR.class.getSimpleName());

    private final int k;

    private final Map<Double, Double> durationMap;

    private List<UnicastCandidate> srtUnicastCandidateList;

    private final Object writeLock;

    private final Object costLock;

    private Cost globalBestCost;

    private final List<Unicast> bestSolution;

    private Evaluator evaluator;

    private String scenarioOutputPath;

    public WSMv2LWR(int k){
        this.k = k;
        writeLock = new Object();
        costLock = new Object();
        durationMap = new HashMap<>();
        globalBestCost = new AVBLatencyMathCost();
        bestSolution = new ArrayList<>();
    }

    public Solution solve(Bag bag){
        List<Unicast> ttUnicastList = getTTUnicastList(bag.getApplicationList());

        this.evaluator = bag.getEvaluator();

        scenarioOutputPath = createScenarioOutputPath(bag);

        new File(scenarioOutputPath).mkdirs();

        try (ExecutorService exec = Executors.newFixedThreadPool(bag.getThreadNumber())) {

            Timer timer = getTimer(Duration.ofSeconds(bag.getTimeout()));

            for (int i = 0; i < bag.getThreadNumber(); i++) {
                exec.execute(new WSMv2Runnable(bag, ttUnicastList));
            }

            exec.awaitTermination(Duration.ofSeconds(bag.getTimeout()).toSeconds(), TimeUnit.SECONDS);
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

    private class WSMv2Runnable implements Runnable {
        private int i = 0;
        Instant solutionStartTime = Instant.now();

        Bag bag;
        List<Unicast> ttUnicastList;

        public WSMv2Runnable(Bag bag, List<Unicast> ttUnicastList) {
            this.bag = bag;
            this.ttUnicastList = ttUnicastList;
        }

        @Override
        public void run() {
            if(Objects.equals(bag.getLog(), Constants.DEBUG)){
                System.setProperty("org.slf4j.simple.SimpleLogger.DEFAULT_LOG_LEVEL_KEY", "DEBUG");
            }

            final Logger logger = LoggerFactory.getLogger(WSMv2Runnable.class.getSimpleName());

            String threadName = Thread.currentThread().getName();

            while (!Thread.currentThread().isInterrupted()) {
                i++;

                YenMCDMKShortestPaths yenMCDMKShortestPaths = new YenMCDMKShortestPaths(bag, ttUnicastList, k, scenarioOutputPath);

                srtUnicastCandidateList = yenMCDMKShortestPaths.getSRTUnicastCandidateList();

                List<Unicast> solution = null;
                List<Unicast> initialSolution = null;

                if(Objects.equals(bag.getMetaheuristicName(), Constants.GRASP)){
                    initialSolution = LaursenMethods.constructInitialSolution(srtUnicastCandidateList, ttUnicastList, k, evaluator);
                    solution = MetaheuristicMethods.GRASP(initialSolution, evaluator, srtUnicastCandidateList, globalBestCost);
                } else if (Objects.equals(bag.getMetaheuristicName(), Constants.ALO)) {
                    initialSolution = LaursenMethods.constructInitialSolution(srtUnicastCandidateList, ttUnicastList, k, evaluator);
                    solution = MetaheuristicMethods.ALO(initialSolution, initialSolution, srtUnicastCandidateList, k, evaluator);
                } else if (Objects.equals(bag.getMetaheuristicName(), Constants.CONSTRUCT_INITIAL_SOLUTION)) {
                    solution = LaursenMethods.constructInitialSolution(srtUnicastCandidateList, ttUnicastList, k, evaluator);
                    initialSolution = solution;
                }

                if(logger.isDebugEnabled()){
                    synchronized (writeLock) {
                        assert solution != null;
                        try {
                            writeSolutionsToFile(initialSolution, solution, scenarioOutputPath, threadName, i);
                            writeSRTCandidateRoutesToFile(srtUnicastCandidateList, scenarioOutputPath, threadName, i);
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
                            durationMap.put(Double.parseDouble(globalBestCost.toString().split("\\s")[0]), (Duration.between(solutionStartTime, solutionEndTime).toMillis() / 1e3));
                            bestSolution.clear();
                            assert solution != null;
                            bestSolution.addAll(solution);
                        }
                    }
                }
            }

            logger.info(" {} finished in {} iterations", threadName, i);
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

