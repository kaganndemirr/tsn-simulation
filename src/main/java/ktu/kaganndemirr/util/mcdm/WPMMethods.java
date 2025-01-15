package ktu.kaganndemirr.util.mcdm;

import ktu.kaganndemirr.application.Application;
import ktu.kaganndemirr.architecture.EndSystem;
import ktu.kaganndemirr.architecture.GCLEdge;
import ktu.kaganndemirr.architecture.Node;
import ktu.kaganndemirr.message.Unicast;
import ktu.kaganndemirr.message.UnicastCandidate;
import ktu.kaganndemirr.util.Bag;
import ktu.kaganndemirr.util.RandomNumberGenerator;
import ktu.kaganndemirr.util.UnicastCandidateSortingMethods;
import org.jgrapht.GraphPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static ktu.kaganndemirr.util.mcdm.HelperMethods.*;

public class WPMMethods {
    private static final Logger logger = LoggerFactory.getLogger(WPMMethods.class.getSimpleName());

    private static GraphPath<Node, GCLEdge> srtTTLengthV2GraphPath(Bag bag, List<List<Double>> srtTTLengthCostList, List<GraphPath<Node, GCLEdge>> graphPathList) {
        Map<GraphPath<Node, GCLEdge>, Integer> graphPathPathScoreMap = new HashMap<>();
        GraphPath<Node, GCLEdge> selectedGraphPath;

        if(Objects.equals(bag.getWPMValueType(), MCDMConstants.ACTUAL)){
            for(int i = 0; i < graphPathList.size(); i++){
                for(int j = i + 1; j < graphPathList.size(); j++){
                    if(!graphPathList.get(i).equals(graphPathList.get(j))) {
                        if (!graphPathPathScoreMap.containsKey(graphPathList.get(i))){
                            graphPathPathScoreMap.put(graphPathList.get(i), 0);
                        }
                        if (!graphPathPathScoreMap.containsKey(graphPathList.get(j))){
                            graphPathPathScoreMap.put(graphPathList.get(j), 0);
                        }

                        double cost;
                        if(srtTTLengthCostList.getFirst().get(j) == 0 || srtTTLengthCostList.get(1).get(j) == 0){
                            cost = MCDMConstants.NEW_COST;
                        }
                        else {
                            cost = Math.pow((srtTTLengthCostList.getFirst().get(i) / srtTTLengthCostList.getFirst().get(j)), bag.getWSRT()) * Math.pow((srtTTLengthCostList.get(1).get(i) / srtTTLengthCostList.get(1).get(j)), bag.getWTT()) * Math.pow(( srtTTLengthCostList.getLast().get(i) / srtTTLengthCostList.getLast().get(j)), bag.getWLength());
                        }

                        if (cost < MCDMConstants.WPM_THRESHOLD) {
                            graphPathPathScoreMap.put(graphPathList.get(i), graphPathPathScoreMap.get(graphPathList.get(i)) + 1);

                        } else {
                            graphPathPathScoreMap.put(graphPathList.get(j), graphPathPathScoreMap.get(graphPathList.get(j)) + 1);
                        }
                    }
                }
            }

        }
        else if(Objects.equals(bag.getWPMValueType(), MCDMConstants.RELATIVE)){
            for(int i = 0; i < graphPathList.size(); i++){
                for(int j = i + 1; j < graphPathList.size(); j++){
                    if(!graphPathList.get(i).equals(graphPathList.get(j))){
                        if (!graphPathPathScoreMap.containsKey(graphPathList.get(i))){
                            graphPathPathScoreMap.put(graphPathList.get(i), 0);
                        }

                        double relativeSRTCostI = srtTTLengthCostList.getFirst().get(i) / (srtTTLengthCostList.getFirst().get(i) + srtTTLengthCostList.get(1).get(i) + srtTTLengthCostList.getLast().get(i));
                        double relativeTTCostI =  srtTTLengthCostList.get(1).get(i) / (srtTTLengthCostList.getFirst().get(i) + srtTTLengthCostList.get(1).get(i) + srtTTLengthCostList.getLast().get(i));
                        double relativeLengthCostI =  srtTTLengthCostList.getLast().get(i) / (srtTTLengthCostList.getFirst().get(i) + srtTTLengthCostList.get(1).get(i) + srtTTLengthCostList.getLast().get(i));

                        if (!graphPathPathScoreMap.containsKey(graphPathList.get(j))){
                            graphPathPathScoreMap.put(graphPathList.get(j), 0);
                        }

                        double cost;
                        if(srtTTLengthCostList.getFirst().get(j) == 0 || srtTTLengthCostList.get(1).get(j) == 0){
                            cost = MCDMConstants.NEW_COST;
                        }

                        else {
                            double relativeSRTCostJ = srtTTLengthCostList.getFirst().get(j) / (srtTTLengthCostList.getFirst().get(j) + srtTTLengthCostList.get(1).get(j) + srtTTLengthCostList.getLast().get(j));
                            double relativeTTCostJ =  srtTTLengthCostList.get(1).get(j) / (srtTTLengthCostList.getFirst().get(j) + srtTTLengthCostList.get(1).get(j) + srtTTLengthCostList.getLast().get(j));
                            double relativeLengthCostJ =  srtTTLengthCostList.getLast().get(j) / (srtTTLengthCostList.getFirst().get(j) + srtTTLengthCostList.get(1).get(j) + srtTTLengthCostList.getLast().get(j));

                            cost = Math.pow((relativeSRTCostI / relativeSRTCostJ), bag.getWSRT()) * Math.pow((relativeTTCostI / relativeTTCostJ), bag.getWTT()) * Math.pow((relativeLengthCostI / relativeLengthCostJ), bag.getWLength());
                        }

                        if(cost < MCDMConstants.WPM_THRESHOLD){
                            graphPathPathScoreMap.put(graphPathList.get(i), graphPathPathScoreMap.get(graphPathList.get(i)) + 1);
                        }
                        else{
                            graphPathPathScoreMap.put(graphPathList.get(j), graphPathPathScoreMap.get(graphPathList.get(j)) + 1);
                        }
                    }
                }
            }
        }

        //All graphPaths same so pick first one
        if(graphPathPathScoreMap.isEmpty()){
            graphPathPathScoreMap.put(graphPathList.getFirst(), 1);
        }

        Map<GraphPath<Node, GCLEdge>, Integer> sortedGraphPathPathScoreMap = graphPathPathScoreMap.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));

        selectedGraphPath = sortedGraphPathPathScoreMap.entrySet().stream().findFirst().get().getKey();

        return selectedGraphPath;
    }

    public static List<Unicast> srtTTLength(Bag bag, List<UnicastCandidate> srtUnicastCandidateList, List<Unicast> ttUnicastList) {
        List<UnicastCandidate> sortedSRTUnicastCandidateList = null;
        if(Objects.equals(bag.getUnicastCandidateSortingMethod(), MCDMConstants.DEADLINE)){
            sortedSRTUnicastCandidateList = UnicastCandidateSortingMethods.sortUnicastCandidateListForDeadlineAscending(srtUnicastCandidateList);
        }

        List<Unicast> solution = new ArrayList<>();

        if(!ttUnicastList.isEmpty()){
            solution.addAll(ttUnicastList);
        }

        Map<GCLEdge, Double> edgeDurationMap = getEdgeTTDurationMap(ttUnicastList);

//        assert sortedSRTUnicastCandidateList != null;
//        Map<UnicastCandidate, List<List<Double>>> unicastCandidateCostsMap = getSRTTTLengthCostList(bag, sortedSRTUnicastCandidateList, solution, edgeDurationMap);
//
//        if(Objects.equals(bag.getWPMVersion(), MCDMConstants.WPM_VERSION_V1)){
//            for(Map.Entry<UnicastCandidate, List<List<Double>>> entry: unicastCandidateCostsMap.entrySet()){
//                Unicast unicast = getWPMVersionV1Unicast(bag, entry);
//                solution.add(unicast);
//            }
//
//        }
//        else {
//            for(Map.Entry<UnicastCandidate, List<List<Double>>> entry: unicastCandidateCostsMap.entrySet()){
//
//                GraphPath<Node, GCLEdge> selectedGraphPath = srtTTLengthV2GraphPath(bag, entry.getValue(), entry.getKey().getCandidatePathList());
//
//                Unicast unicast = new Unicast(entry.getKey().getApplication(), entry.getKey().getTarget(), selectedGraphPath);
//                solution.add(unicast);
//            }
//
//        }

        return solution;
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

    public static List<Unicast> srtTTLengthCWR(Bag bag, String unicastCandidateSortingMethod, List<UnicastCandidate> srtUnicastCandidateList, List<Unicast> ttUnicastList) {
        List<UnicastCandidate> sortedSRTUnicastCandidateList = null;
        if(Objects.equals(unicastCandidateSortingMethod, MCDMConstants.DEADLINE)){
            sortedSRTUnicastCandidateList = UnicastCandidateSortingMethods.sortUnicastCandidateListForDeadlineAscending(srtUnicastCandidateList);
        }

        List<Unicast> solution = new ArrayList<>();

        if(!ttUnicastList.isEmpty()){
            solution.addAll(ttUnicastList);
        }

        Map<GCLEdge, Double> edgeDurationMap = getEdgeTTDurationMap(ttUnicastList);

        assert sortedSRTUnicastCandidateList != null;
//        Map<UnicastCandidate, List<List<Double>>> unicastCandidateCostsMap = getSRTTTLengthCostList(bag, sortedSRTUnicastCandidateList, solution, edgeDurationMap);
//
//        List<Double> weightList = RandomNumberGenerator.generateRandomWeightsAVBTTLengthThreadLocalRandom();
//        bag.setWSRT(weightList.getFirst());
//        bag.setWTT(weightList.get(1));
//        bag.setWLength(weightList.getLast());
//
//        if(Objects.equals(bag.getWPMVersion(), MCDMConstants.WPM_VERSION_V1)){
//            for(Map.Entry<UnicastCandidate, List<List<Double>>> entry: unicastCandidateCostsMap.entrySet()){
//                Unicast unicast = getWPMVersionV1Unicast(bag, entry);
//                solution.add(unicast);
//            }
//
//        }
//        else {
//            for(Map.Entry<UnicastCandidate, List<List<Double>>> entry: unicastCandidateCostsMap.entrySet()){
//
//                GraphPath<Node, GCLEdge> selectedGraphPath = srtTTLengthV2GraphPath(bag, entry.getValue(), entry.getKey().getCandidatePathList());
//
//                Unicast unicast = new Unicast(entry.getKey().getApplication(), entry.getKey().getTarget(), selectedGraphPath);
//                solution.add(unicast);
//            }
//
//        }

        return solution;
    }

    private static Unicast getWPMVersionV1Unicast(Bag bag, Map.Entry<UnicastCandidate, List<List<Double>>> entry) {
        double maxCost = Double.MAX_VALUE;
        GraphPath<Node, GCLEdge> selectedGraphPath = null;

        for(int i = 0; i < entry.getKey().getCandidatePathList().size(); i++){
            double cost = Math.pow(entry.getValue().getFirst().get(i), bag.getWSRT()) * Math.pow(entry.getValue().get(1).get(i), bag.getWTT()) * Math.pow(entry.getValue().getLast().get(i), bag.getWLength());
            if (cost < maxCost) {
                maxCost = cost;
                selectedGraphPath = entry.getKey().getCandidatePathList().get(i);
                if (maxCost == 0) {
                    break;
                }
            }
        }

        return new Unicast(entry.getKey().getApplication(), entry.getKey().getTarget(), selectedGraphPath);
    }

}
