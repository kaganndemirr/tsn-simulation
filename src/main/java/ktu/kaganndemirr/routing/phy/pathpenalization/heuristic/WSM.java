package ktu.kaganndemirr.routing.phy.pathpenalization.heuristic;

import ktu.kaganndemirr.evaluator.Cost;
import ktu.kaganndemirr.message.Multicast;
import ktu.kaganndemirr.message.Unicast;
import ktu.kaganndemirr.message.UnicastCandidate;
import ktu.kaganndemirr.routing.phy.pathpenalization.PathPenalizationKShortestPaths;
import ktu.kaganndemirr.solver.Solution;
import ktu.kaganndemirr.util.Bag;
import ktu.kaganndemirr.util.Constants;
import ktu.kaganndemirr.util.mcdm.WSMMethods;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static ktu.kaganndemirr.util.HelperMethods.createScenarioOutputPath;

public class WSM {
    private static final Logger logger = LoggerFactory.getLogger(WSM.class.getSimpleName());

    private List<UnicastCandidate> srtUnicastCandidateList;

    private List<Unicast> solution;

    private final Map<Double, Double> durationMap;


    public WSM() {
        this.durationMap = new HashMap<>();
    }

    public Solution solve(Bag bag) throws IOException {
        Instant pathPenalizationKShortestPathsStartTime = Instant.now();
        PathPenalizationKShortestPaths pathPenalizationKShortestPaths = new PathPenalizationKShortestPaths(bag);
        Instant pathPenalizationKShortestPathsEndTime = Instant.now();
        long pathPenalizationKShortestPathsDuration = Duration.between(pathPenalizationKShortestPathsStartTime, pathPenalizationKShortestPathsEndTime).toMillis();

        srtUnicastCandidateList = pathPenalizationKShortestPaths.getSRTUnicastCandidateList();
        List<Unicast> ttUnicastList = pathPenalizationKShortestPaths.getTTUnicastList();

        String scenarioOutputPath = null;
        if(logger.isDebugEnabled()){
            scenarioOutputPath = createScenarioOutputPath(bag);

            new File(scenarioOutputPath).mkdirs();
        }

        BufferedWriter costsWriter = null;
        if(logger.isDebugEnabled()){
            assert scenarioOutputPath != null;
            costsWriter = new BufferedWriter(new FileWriter(Paths.get(scenarioOutputPath, "Costs.txt").toString(), true));
        }
        Instant solutionStartTime = Instant.now();
        try{

            if (Objects.equals(bag.getMCDMObjective(), Constants.SRT_TT)){
                solution = null;
            } else if (Objects.equals(bag.getMCDMObjective(), Constants.SRT_TT_LENGTH)) {
                solution = WSMMethods.srtTTLength(bag, srtUnicastCandidateList, ttUnicastList, costsWriter);
            } else if (Objects.equals(bag.getMCDMObjective(), Constants.SRT_TT_LENGTH_UTIL)) {
                solution = null;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Instant solutionEndTime = Instant.now();
        if(logger.isDebugEnabled()){
            assert costsWriter != null;
            costsWriter.close();
        }
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