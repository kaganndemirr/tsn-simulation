package ktu.kaganndemirr.util.mcdm;

import ktu.kaganndemirr.application.SRTApplication;
import ktu.kaganndemirr.application.TTApplication;
import ktu.kaganndemirr.architecture.GCL;
import ktu.kaganndemirr.architecture.GCLEdge;
import ktu.kaganndemirr.architecture.Node;
import ktu.kaganndemirr.message.Unicast;
import ktu.kaganndemirr.message.UnicastCandidate;
import ktu.kaganndemirr.util.Constants;
import ktu.kaganndemirr.util.RandomNumberGenerator;
import ktu.kaganndemirr.util.UnicastCandidateSortingMethods;
import org.jgrapht.GraphPath;

import java.util.*;
import java.util.stream.Collectors;

import static ktu.kaganndemirr.util.mcdm.HelperMethods.getEdgeTTDurationMap;
import static ktu.kaganndemirr.util.mcdm.HelperMethods.getSameEdgeList;

public class WPMMethods {
    private static GraphPath<Node, GCLEdge> v2SRTTTLengthGraphPath(double wSRT, double wTT, double wLength, List<Double> srtCostList, List<Double> ttCostList, List<GraphPath<Node, GCLEdge>> graphPathList, String wpmValueType) {
        Map<GraphPath<Node, GCLEdge>, Integer> graphPathPathScoreMap = new HashMap<>();
        GraphPath<Node, GCLEdge> selectedGraphPath;

        if(Objects.equals(wpmValueType, Constants.ACTUAL)){
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
                            cost = Constants.NEW_COST;
                        }
                        else {
                            cost = Math.pow((srtCostList.get(i) / srtCostList.get(j)), wSRT) * Math.pow((ttCostList.get(i) / ttCostList.get(j)), wTT) * Math.pow(((double) graphPathList.get(i).getLength() / graphPathList.get(j).getLength()), wLength);
                        }

                        if (cost < Constants.WPM_THRESHOLD) {
                            graphPathPathScoreMap.put(graphPathList.get(i), graphPathPathScoreMap.get(graphPathList.get(i)) + 1);

                        } else {
                            graphPathPathScoreMap.put(graphPathList.get(j), graphPathPathScoreMap.get(graphPathList.get(j)) + 1);
                        }
                    }
                }
            }

        }
        else if(Objects.equals(wpmValueType, Constants.RELATIVE)){
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
                            cost = Constants.NEW_COST;
                        }

                        else {
                            double relativeSRTCostJ = srtCostList.get(j) / (srtCostList.get(j) + ttCostList.get(j) + graphPathList.get(j).getLength());
                            double relativeTTCostJ =  ttCostList.get(j) / (srtCostList.get(j) + ttCostList.get(j) + graphPathList.get(j).getLength());
                            double relativeLengthCostJ =  graphPathList.get(j).getLength() / (srtCostList.get(j) + ttCostList.get(j) + graphPathList.get(j).getLength());

                            cost = Math.pow((relativeSRTCostI / relativeSRTCostJ), wSRT) * Math.pow((relativeTTCostI / relativeTTCostJ), wTT) * Math.pow((relativeLengthCostI / relativeLengthCostJ), wLength);
                        }

                        if(cost < Constants.WPM_THRESHOLD){
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

    public static List<Unicast> deadlineSRTTTLength(List<UnicastCandidate> srtUnicastCandidateList, List<Unicast> ttUnicastList, double wSRT, double wTT, double wLength, String wpmVersion, String wpmValueType) {
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

            if(Objects.equals(wpmVersion, Constants.WPM_VERSION_V1)){
                for (int i = 0; i < gpList.size(); i++) {
                    double cost = Math.pow(srtCostList.get(i), wSRT) * Math.pow(ttCostList.get(i), wTT) * Math.pow(gpList.get(i).getEdgeList().size(), wLength);
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
                selectedGP = v2SRTTTLengthGraphPath(wSRT, wTT, wLength, srtCostList, ttCostList, gpList, wpmValueType);
            }

            Unicast selectedU = new Unicast(unicastCandidate.getApplication(), unicastCandidate.getTarget(), selectedGP);
            solution.add(selectedU);
        }

        return solution;
    }

    public static List<Unicast> deadlineCWRSRTTTLength(List<UnicastCandidate> srtUnicastCandidateList, List<Unicast> ttUnicastList, String wpmVersion, String wpmValueType) {
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

            if(Objects.equals(wpmVersion, Constants.WPM_VERSION_V1)){
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
                selectedGP = v2SRTTTLengthGraphPath(weightList.getFirst(), weightList.get(1), weightList.getLast(), srtCostList, ttCostList, gpList, wpmValueType);
            }

            Unicast selectedU = new Unicast(unicastCandidate.getApplication(), unicastCandidate.getTarget(), selectedGP);
            solution.add(selectedU);
        }

        return solution;
    }


}
