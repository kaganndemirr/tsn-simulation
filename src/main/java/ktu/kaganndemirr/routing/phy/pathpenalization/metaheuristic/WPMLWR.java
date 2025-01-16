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
import ktu.kaganndemirr.routing.phy.pathpenalization.PathPenalizationMCDMKShortestPaths;
import ktu.kaganndemirr.routing.phy.yen.YenKShortestPaths;
import ktu.kaganndemirr.routing.phy.yen.YenMCDMKShortestPaths;
import ktu.kaganndemirr.routing.phy.yen.YenRandomizedKShortestPaths;
import ktu.kaganndemirr.solver.Solution;
import ktu.kaganndemirr.util.Bag;
import ktu.kaganndemirr.util.Constants;
import ktu.kaganndemirr.util.LaursenMethods;
import ktu.kaganndemirr.util.MetaheuristicMethods;
import ktu.kaganndemirr.util.mcdm.WPMMethods;
import ktu.kaganndemirr.util.mcdm.WSMMethods;
import org.jgrapht.Graph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static ktu.kaganndemirr.util.HelperMethods.*;

public class WPMLWR {
    private static final Logger logger = LoggerFactory.getLogger(WPMLWR.class.getSimpleName());

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

    public WPMLWR(){
        unicastList = new ArrayList<>();
        writeLock = new Object();
        costLock = new Object();
        globalBestCost = new AVBLatencyMathCost();
        bestSolution = new ArrayList<>();
        durationMap = new HashMap<>();
    }

    public Solution solve(Bag bag){
        PathPenalizationMCDMKShortestPaths pathPenalizationMCDMKShortestPaths = new PathPenalizationMCDMKShortestPaths(bag);
        ttUnicastList = pathPenalizationMCDMKShortestPaths.getTTUnicastList();
        srtUnicastList = pathPenalizationMCDMKShortestPaths.getSRTUnicastList();

        unicastList.addAll(ttUnicastList);
        unicastList.addAll(srtUnicastList);

        this.evaluator = bag.getEvaluator();

        if(logger.isDebugEnabled()){
            scenarioOutputPath = createScenarioOutputPath(bag);

            new File(scenarioOutputPath).mkdirs();
        }


        try (ExecutorService exec = Executors.newFixedThreadPool(bag.getThreadNumber())) {

            Timer timer = getTimer(Duration.ofSeconds(bag.getTimeout()));

            for (int i = 0; i < bag.getThreadNumber(); i++) {
                exec.execute(new WSMv2Runnable(bag, unicastList));
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
        List<Unicast> unicastList;

        public WSMv2Runnable(Bag bag, List<Unicast> unicastList) {
            this.bag = bag;
            this.unicastList = unicastList;
        }

        @Override
        public void run() {
            String threadName = Thread.currentThread().getName();

            while (!Thread.currentThread().isInterrupted()) {
                i++;

                PathPenalizationMCDMKShortestPaths pathPenalizationMCDMKShortestPaths;
                try {
                    pathPenalizationMCDMKShortestPaths = new PathPenalizationMCDMKShortestPaths(bag, scenarioOutputPath, threadName, i);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                srtUnicastCandidateList = pathPenalizationMCDMKShortestPaths.getSRTUnicastCandidateList();
                ttUnicastCandidateList = pathPenalizationMCDMKShortestPaths.getTTUnicastCandidateList();

                BufferedWriter costsWriter = null;
                if(logger.isDebugEnabled()){
                    try {
                        costsWriter = new BufferedWriter(new FileWriter(Paths.get(scenarioOutputPath, "Costs.txt").toString(), true));
                        synchronized (writeLock){
                            costsWriter.write("############## ThreadName:" + threadName + " Iteration:" + i + " ##############\n");
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }

                List<Unicast> initialSolution = null;
                try{
                    if (Objects.equals(bag.getMCDMObjective(), Constants.SRT_TT)){
                        //TODO
                    } else if (Objects.equals(bag.getMCDMObjective(), Constants.SRT_TT_LENGTH)) {
                        initialSolution = WPMMethods.srtTTLength(bag, srtUnicastCandidateList, ttUnicastList, costsWriter);
                    } else if (Objects.equals(bag.getMCDMObjective(), Constants.SRT_TT_LENGTH_UTIL)) {
                        //TODO
                    }
                }catch (IOException e){
                    throw new RuntimeException(e);
                }

                List<Unicast> solution = null;

                if(Objects.equals(bag.getMetaheuristicName(), Constants.GRASP)){
                    solution = MetaheuristicMethods.GRASP(initialSolution, evaluator, srtUnicastCandidateList, globalBestCost);
                } else if (Objects.equals(bag.getMetaheuristicName(), Constants.ALO)) {
                    solution = MetaheuristicMethods.ALO(initialSolution, initialSolution, srtUnicastCandidateList, bag.getK(), evaluator);
                }

                if(logger.isDebugEnabled()){
                    synchronized (writeLock) {
                        assert solution != null;
                        try {
                            assert costsWriter != null;
                            costsWriter.close();
                            assert initialSolution != null;
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



