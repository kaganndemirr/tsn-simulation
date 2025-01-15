package ktu.kaganndemirr.util.mcdm;

import ktu.kaganndemirr.application.Application;
import ktu.kaganndemirr.architecture.EndSystem;
import ktu.kaganndemirr.architecture.GCLEdge;
import ktu.kaganndemirr.architecture.Node;
import ktu.kaganndemirr.message.Unicast;
import ktu.kaganndemirr.message.UnicastCandidate;
import ktu.kaganndemirr.util.Bag;
import ktu.kaganndemirr.util.UnicastCandidateSortingMethods;
import org.jgrapht.GraphPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

import static ktu.kaganndemirr.util.mcdm.HelperMethods.*;

public class WSMMethods {
    private static final Logger logger = LoggerFactory.getLogger(WSMMethods.class.getSimpleName());

    public static List<Double> normalizeSRTTTLengthCostListMax(List<Double> srtTTCostlist) {
        double max = Collections.max(srtTTCostlist);

        List<Double> normalizedSRTTTCostlist = new ArrayList<>();

        if (max == 0) {
            for (int i = 0; i < srtTTCostlist.size(); i++) {
                normalizedSRTTTCostlist.add(0.0);
            }
        } else {
            for (Double d : srtTTCostlist) {
                normalizedSRTTTCostlist.add(d / max);
            }
        }

        return normalizedSRTTTCostlist;
    }

    public static List<Unicast> srtTTLength(Bag bag, List<UnicastCandidate> srtUnicastCandidateList, List<Unicast> unicastList, BufferedWriter costsWriter) throws IOException {
        List<UnicastCandidate> sortedSRTUnicastCandidateList = null;
        if(Objects.equals(bag.getUnicastCandidateSortingMethod(), MCDMConstants.DEADLINE)){
            sortedSRTUnicastCandidateList = UnicastCandidateSortingMethods.sortUnicastCandidateListForDeadlineAscending(srtUnicastCandidateList);
        }

        List<Unicast> solution = new ArrayList<>();

        if(!unicastList.isEmpty()){
            solution.addAll(unicastList);
        }

        Map<GCLEdge, Double> edgeDurationMap = getEdgeTTDurationMap(unicastList);

        assert sortedSRTUnicastCandidateList != null;
        return getSRTTTLengthCostList(bag, sortedSRTUnicastCandidateList, solution, edgeDurationMap, costsWriter);
    }

    public static GraphPath<Node, GCLEdge> srtTTLengthForCandidatePathComputing(Bag bag, Application application, EndSystem target, List<GraphPath<Node, GCLEdge>> kShortestPathsGraphPathList, List<Unicast> unicastList, List<GraphPath<Node, GCLEdge>> mcdmGraphPathList, BufferedWriter costsWriter, int candidatePathIndex) throws IOException {
        List<Unicast> solution = new ArrayList<>();

        if(!unicastList.isEmpty()){
            solution.addAll(unicastList);
        }

        if(!mcdmGraphPathList.isEmpty()){
            for(GraphPath<Node, GCLEdge> graphPath: mcdmGraphPathList){
                solution.add(new Unicast(application, target, graphPath));
            }
        }

        Map<GCLEdge, Double> edgeDurationMap = getEdgeTTDurationMap(unicastList);

        List<List<Double>> srtTTLengthCostList = getSRTTTLengthCostListForCandidatePathComputation(kShortestPathsGraphPathList, solution, application, edgeDurationMap);

        double maxCost = Double.MAX_VALUE;
        GraphPath<Node, GCLEdge> selectedGraphPath = null;

        for (int i = 0; i < kShortestPathsGraphPathList.size(); i++) {
            double cost = Math.pow(srtTTLengthCostList.getFirst().get(i), bag.getWSRT()) * Math.pow(srtTTLengthCostList.get(1).get(i), bag.getWTT()) * Math.pow(srtTTLengthCostList.getLast().get(i), bag.getWLength());
            if (cost < maxCost) {
                maxCost = cost;
                selectedGraphPath = kShortestPathsGraphPathList.get(i);
                if (maxCost == 0) {
                    break;
                }
            }
        }

        return selectedGraphPath;
    }
}
