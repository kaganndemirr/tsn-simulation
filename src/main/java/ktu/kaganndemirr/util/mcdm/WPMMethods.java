package ktu.kaganndemirr.util.mcdm;

import ktu.kaganndemirr.application.Application;
import ktu.kaganndemirr.application.SRTApplication;
import ktu.kaganndemirr.application.TTApplication;
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

import static ktu.kaganndemirr.util.mcdm.HelperMethods.getEdgeTTDurationMap;
import static ktu.kaganndemirr.util.mcdm.HelperMethods.getSameEdgeList;

public class WPMMethods {
    private static final Logger logger = LoggerFactory.getLogger(WPMMethods.class.getSimpleName());

    private static GraphPath<Node, GCLEdge> srtTTLengthV2GraphPath(Bag bag, List<Double> srtCostList, List<Double> ttCostList, List<GraphPath<Node, GCLEdge>> graphPathList) {
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
                        if(srtCostList.get(j) == 0 || ttCostList.get(j) == 0){
                            cost = MCDMConstants.NEW_COST;
                        }
                        else {
                            cost = Math.pow((srtCostList.get(i) / srtCostList.get(j)), bag.getWSRT()) * Math.pow((ttCostList.get(i) / ttCostList.get(j)), bag.getWTT()) * Math.pow(((double) graphPathList.get(i).getLength() / graphPathList.get(j).getLength()), bag.getWLength());
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

                        double relativeSRTCostI = srtCostList.get(i) / (srtCostList.get(i) + ttCostList.get(i) + graphPathList.get(i).getLength());
                        double relativeTTCostI =  ttCostList.get(i) / (srtCostList.get(i) + ttCostList.get(i) + graphPathList.get(i).getLength());
                        double relativeLengthCostI =  graphPathList.get(i).getLength() / (srtCostList.get(i) + ttCostList.get(i) + graphPathList.get(i).getLength());

                        if (!graphPathPathScoreMap.containsKey(graphPathList.get(j))){
                            graphPathPathScoreMap.put(graphPathList.get(j), 0);
                        }

                        double cost;
                        if(srtCostList.get(j) == 0 || ttCostList.get(j) == 0){
                            cost = MCDMConstants.NEW_COST;
                        }

                        else {
                            double relativeSRTCostJ = srtCostList.get(j) / (srtCostList.get(j) + ttCostList.get(j) + graphPathList.get(j).getLength());
                            double relativeTTCostJ =  ttCostList.get(j) / (srtCostList.get(j) + ttCostList.get(j) + graphPathList.get(j).getLength());
                            double relativeLengthCostJ =  graphPathList.get(j).getLength() / (srtCostList.get(j) + ttCostList.get(j) + graphPathList.get(j).getLength());

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

    public static List<Unicast> deadlineSRTTTLength(List<UnicastCandidate> srtUnicastCandidateList, List<Unicast> ttUnicastList, Bag bag) {
        List<UnicastCandidate> sortedSRTUnicastCandidateList = UnicastCandidateSortingMethods.sortUnicastCandidateListForDeadlineAscending(srtUnicastCandidateList);

        List<Unicast> solution = new ArrayList<>();

        if(!ttUnicastList.isEmpty()){
            solution.addAll(ttUnicastList);
        }

        Map<GCLEdge, Double> edgeDurationMap = getEdgeTTDurationMap(ttUnicastList);

        for (UnicastCandidate unicastCandidate : sortedSRTUnicastCandidateList) {
            List<Double> srtCostList = new ArrayList<>();
            List<Double> ttCostList = new ArrayList<>();
            List<GraphPath<Node, GCLEdge>> gpList = unicastCandidate.getCandidatePathList();

            for (GraphPath<Node, GCLEdge> gp : gpList) {
                double srtCost = 0;
                double ttCost = 0;
                for (Unicast unicast : solution) {
                    if (unicast.getApplication() instanceof TTApplication) {
                        ArrayList<GCLEdge> sameElements = getSameEdgeList(gp.getEdgeList(), unicast.getPath().getEdgeList());
                        for (GCLEdge edge : sameElements) {
                            ttCost += edgeDurationMap.get(edge);
                        }
                    } else if (unicast.getApplication() instanceof SRTApplication srtApplication) {
                        int sameEdgeNumber = getSameEdgeList(gp.getEdgeList(), unicast.getPath().getEdgeList()).size();
                        double unicastCandidateTraffic = (unicastCandidate.getApplication().getFrameSizeByte() * unicastCandidate.getApplication().getNumber0fFrames()) / unicastCandidate.getApplication().getCMI();
                        double unicastTraffic = (srtApplication.getFrameSizeByte() * srtApplication.getNumber0fFrames()) / srtApplication.getCMI();
                        srtCost += sameEdgeNumber * (unicastTraffic * unicastCandidateTraffic);
                    }
                }
                srtCostList.add(srtCost);
                ttCostList.add(ttCost);
            }

            double maxCost = Double.MAX_VALUE;
            GraphPath<Node, GCLEdge> selectedGP = null;

            if(Objects.equals(bag.getWPMVersion(), MCDMConstants.WPM_VERSION_V1)){
                for (int i = 0; i < gpList.size(); i++) {
                    double cost = Math.pow(srtCostList.get(i), bag.getWSRT()) * Math.pow(ttCostList.get(i), bag.getWTT()) * Math.pow(gpList.get(i).getEdgeList().size(), bag.getWLength());
                    if (cost < maxCost) {
                        maxCost = cost;
                        selectedGP = gpList.get(i);
                        if (maxCost == 0) {
                            break;
                        }
                    }
                }
            }
            else {
                selectedGP = srtTTLengthV2GraphPath(bag, srtCostList, ttCostList, gpList);
            }

            Unicast selectedU = new Unicast(unicastCandidate.getApplication(), unicastCandidate.getTarget(), selectedGP);
            solution.add(selectedU);
        }

        return solution;
    }

    public static GraphPath<Node, GCLEdge> srtTTLength(Bag bag, Application application, EndSystem target, List<GraphPath<Node, GCLEdge>> kShortestPathsGraphPathList, List<Unicast> unicastList, List<GraphPath<Node, GCLEdge>> mcdmGraphPathList, BufferedWriter costsWriter, int candidatePathIndex) throws IOException {
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

        List<Double> srtCostList = new ArrayList<>();
        List<Double> ttCostList = new ArrayList<>();

        for (GraphPath<Node, GCLEdge> graphPath : kShortestPathsGraphPathList) {
            double srtCost = 0;
            double ttCost = 0;
            for (Unicast unicast : solution) {
                if (unicast.getApplication() instanceof TTApplication) {
                    ArrayList<GCLEdge> sameElements = getSameEdgeList(graphPath.getEdgeList(), unicast.getPath().getEdgeList());
                    for (GCLEdge edge : sameElements) {
                        ttCost += edgeDurationMap.get(edge);
                    }
                } else if (unicast.getApplication() instanceof SRTApplication) {
                    int sameEdgeNumber = getSameEdgeList(graphPath.getEdgeList(), unicast.getPath().getEdgeList()).size();
                    double unicastCandidateTraffic = (application.getFrameSizeByte() * application.getNumber0fFrames()) / application.getCMI();
                    double unicastTraffic = (unicast.getApplication().getFrameSizeByte() * unicast.getApplication().getNumber0fFrames()) / unicast.getApplication().getCMI();
                    srtCost += sameEdgeNumber * (unicastTraffic * unicastCandidateTraffic);
                }
            }
            srtCostList.add(srtCost);
            ttCostList.add(ttCost);
        }

        double maxCost = Double.MAX_VALUE;
        GraphPath<Node, GCLEdge> selectedGraphPath = null;

        List<Integer> lengthCostList = new ArrayList<>();
        List<Double> resultCostList = new ArrayList<>();
        if(Objects.equals(bag.getWPMVersion(), MCDMConstants.WPM_VERSION_V1)){
            for (int i = 0; i < kShortestPathsGraphPathList.size(); i++) {
                double cost = Math.pow(srtCostList.get(i), bag.getWSRT()) * Math.pow(ttCostList.get(i), bag.getWTT()) * Math.pow(kShortestPathsGraphPathList.get(i).getEdgeList().size(), bag.getWLength());
                if(logger.isDebugEnabled()){
                    lengthCostList.add(kShortestPathsGraphPathList.get(i).getEdgeList().size());
                    resultCostList.add(cost);
                }
                if (cost < maxCost) {
                    maxCost = cost;
                    selectedGraphPath = kShortestPathsGraphPathList.get(i);
                    if (maxCost == 0) {
                        break;
                    }
                }
            }
        }
        else {
            selectedGraphPath = srtTTLengthV2GraphPath(bag, srtCostList, ttCostList, kShortestPathsGraphPathList);
        }

        if(logger.isDebugEnabled()){
            costsWriter.write(application.getName() + "_" + candidatePathIndex + "\n");
            costsWriter.write(srtCostList + "\n");
            costsWriter.write(ttCostList + "\n");
            costsWriter.write(lengthCostList + "\n");
            costsWriter.write(resultCostList + "\n");
            costsWriter.newLine();
        }

        return selectedGraphPath;
    }

    public static List<Unicast> deadlineCWRSRTTTLength(Bag bag, List<UnicastCandidate> srtUnicastCandidateList, List<Unicast> ttUnicastList) {
        List<UnicastCandidate> sortedSRTUnicastCandidateList = UnicastCandidateSortingMethods.sortUnicastCandidateListForDeadlineAscending(srtUnicastCandidateList);

        List<Unicast> solution = new ArrayList<>();

        if(!ttUnicastList.isEmpty()){
            solution.addAll(ttUnicastList);
        }

        Map<GCLEdge, Double> edgeDurationMap = getEdgeTTDurationMap(ttUnicastList);

        for (UnicastCandidate unicastCandidate : sortedSRTUnicastCandidateList) {
            ArrayList<Double> srtCostList = new ArrayList<>();
            ArrayList<Double> ttCostList = new ArrayList<>();
            List<GraphPath<Node, GCLEdge>> gpList = unicastCandidate.getCandidatePathList();

            for (GraphPath<Node, GCLEdge> gp : gpList) {
                double srtCost = 0;
                double ttCost = 0;
                for (Unicast unicast : solution) {
                    if (unicast.getApplication() instanceof TTApplication) {
                        ArrayList<GCLEdge> sameElements = getSameEdgeList(gp.getEdgeList(), unicast.getPath().getEdgeList());
                        for (GCLEdge edge : sameElements) {
                            ttCost += edgeDurationMap.get(edge);
                        }
                    } else if (unicast.getApplication() instanceof SRTApplication srtApplication) {
                        int sameEdgeNumber = getSameEdgeList(gp.getEdgeList(), unicast.getPath().getEdgeList()).size();
                        double unicastCandidateTraffic = (unicastCandidate.getApplication().getFrameSizeByte() * unicastCandidate.getApplication().getNumber0fFrames()) / unicastCandidate.getApplication().getCMI();
                        double unicastTraffic = (srtApplication.getFrameSizeByte() * srtApplication.getNumber0fFrames()) / srtApplication.getCMI();
                        srtCost += sameEdgeNumber * (unicastTraffic * unicastCandidateTraffic);
                    }
                }
                srtCostList.add(srtCost);
                ttCostList.add(ttCost);
            }

            double maxCost = Double.MAX_VALUE;
            GraphPath<Node, GCLEdge> selectedGP = null;

            List<Double> weightList = RandomNumberGenerator.generateRandomWeightsAVBTTLengthThreadLocalRandom();
            bag.setWSRT(weightList.getFirst());
            bag.setWTT(weightList.get(1));
            bag.setWLength(weightList.getLast());

            if(Objects.equals(bag.getWPMVersion(), MCDMConstants.WPM_VERSION_V1)){
                for (int i = 0; i < gpList.size(); i++) {
                    double cost = Math.pow(srtCostList.get(i), weightList.getFirst()) * Math.pow(ttCostList.get(i), weightList.get(1)) * Math.pow(gpList.get(i).getEdgeList().size(), weightList.getLast());
                    if (cost < maxCost) {
                        maxCost = cost;
                        selectedGP = gpList.get(i);
                        if (maxCost == 0) {
                            break;
                        }
                    }
                }
            }
            else {
                selectedGP = srtTTLengthV2GraphPath(bag, srtCostList, ttCostList, gpList);
            }

            Unicast selectedU = new Unicast(unicastCandidate.getApplication(), unicastCandidate.getTarget(), selectedGP);
            solution.add(selectedU);
        }

        return solution;
    }

}
