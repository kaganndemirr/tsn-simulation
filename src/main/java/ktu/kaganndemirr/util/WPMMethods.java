package ktu.kaganndemirr.util;

import ktu.kaganndemirr.application.SRTApplication;
import ktu.kaganndemirr.application.TTApplication;
import ktu.kaganndemirr.architecture.GCL;
import ktu.kaganndemirr.architecture.GCLEdge;
import ktu.kaganndemirr.architecture.Node;
import ktu.kaganndemirr.message.Unicast;
import ktu.kaganndemirr.message.UnicastCandidate;
import ktu.kaganndemirr.util.holders.Bag;
import org.jgrapht.GraphPath;

import java.util.*;
import java.util.stream.Collectors;

public class WPMMethods {
    public static Map<GCLEdge, Double> getEdgeTTDurationMap(List<Unicast> ttUnicastList) {
        Map<GCLEdge, Double> edgeTTDurationMap = new HashMap<>();
        for (Unicast unicast : ttUnicastList) {
            for (GCLEdge edge : unicast.getPath().getEdgeList()) {
                for (GCL gcl : edge.getGCL()) {
                    if (!edgeTTDurationMap.containsKey(edge)) {
                        edgeTTDurationMap.put(edge, (gcl.getDuration() / (unicast.getApplication().getCMI() / gcl.getFrequency())));
                    } else {
                        edgeTTDurationMap.put(edge, edgeTTDurationMap.get(edge) + (gcl.getDuration() / (unicast.getApplication().getCMI() / gcl.getFrequency())));
                    }
                }

            }
        }
        return edgeTTDurationMap;
    }

    public static ArrayList<GCLEdge> getSameEdgeList(List<GCLEdge> gclEdgeList1, List<GCLEdge> gclEdgeList2) {
        ArrayList<GCLEdge> sameEdgeList = new ArrayList<>();
        for (GCLEdge edge : gclEdgeList1) {
            if (gclEdgeList2.contains(edge)) {
                sameEdgeList.add(edge);
            }
        }
        return sameEdgeList;
    }

    private static GraphPath<Node, GCLEdge> v2SRTTTLengthGraphPath(List<Double> srtCostList, List<Double> ttCostList, UnicastCandidate unicastCandidate, Map<GCLEdge, Double> edgeUtilizationMap, Bag bag) {
        Map<GraphPath<Node, GCLEdge>, Integer> graphPathPathScoreMap = new HashMap<>();
        GraphPath<Node, GCLEdge> selectedGraphPath;

        if(Objects.equals(bag.getWPMValueType(), Constants.ACTUAL)){
            for(int i = 0; i < unicastCandidate.getCandidatePathList().size(); i++){
                for(int j = i + 1; j < unicastCandidate.getCandidatePathList().size(); j++){
                    if(!unicastCandidate.getCandidatePathList().get(i).equals(unicastCandidate.getCandidatePathList().get(j))) {
                        if (!graphPathPathScoreMap.containsKey(unicastCandidate.getCandidatePathList().get(i))){
                            graphPathPathScoreMap.put(unicastCandidate.getCandidatePathList().get(i), 0);
                        }
                        if (!graphPathPathScoreMap.containsKey(unicastCandidate.getCandidatePathList().get(j))){
                            graphPathPathScoreMap.put(unicastCandidate.getCandidatePathList().get(j), 0);
                        }

                        double cost;
                        if(srtCostList.get(j) == 0 || ttCostList.get(j) == 0){
                            cost = Constants.NEW_COST;
                        }
                        else {
                            if(Objects.equals(bag.getWPMObjective(), Constants.SRT_TT)){
                                cost = Math.pow((srtCostList.get(i) / srtCostList.get(j)), bag.getWSRT()) * Math.pow((ttCostList.get(i) / ttCostList.get(j)), bag.getWTT());
                            } else if (Objects.equals(bag.getWPMObjective(), Constants.SRT_TT_LENGTH)) {
                                cost = Math.pow((srtCostList.get(i) / srtCostList.get(j)), bag.getWSRT()) * Math.pow((ttCostList.get(i) / ttCostList.get(j)), bag.getWTT()) * Math.pow(((double) unicastCandidate.getCandidatePathList().get(i).getLength() / unicastCandidate.getCandidatePathList().get(j).getLength()), bag.getWLength());
                            } else if (Objects.equals(bag.getWPMObjective(), Constants.SRT_TT_LENGTH_UTIL)) {
                                Map<GCLEdge, Double> newEdgeUtilizationMap = new HashMap<>(edgeUtilizationMap);
                                for(GCLEdge edge: unicastCandidate.getCandidatePathList().get(i).getEdgeList()){
                                    if(!newEdgeUtilizationMap.containsKey(edge)){
                                        newEdgeUtilizationMap.put(edge, unicastCandidate.getApplication().getMessageSizeMbps());
                                    }
                                    else{
                                        newEdgeUtilizationMap.put(edge, newEdgeUtilizationMap.get(edge) + unicastCandidate.getApplication().getMessageSizeMbps());
                                    }
                                }
                                cost = Math.pow((srtCostList.get(i) / srtCostList.get(j)), bag.getWSRT()) * Math.pow((ttCostList.get(i) / ttCostList.get(j)), bag.getWTT()) * Math.pow(((double) unicastCandidate.getCandidatePathList().get(i).getLength() / unicastCandidate.getCandidatePathList().get(j).getLength()), bag.getWLength());
                            }

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

    public static List<Unicast> constructSolution(List<UnicastCandidate> srtUnicastCandidateList, List<Unicast> ttUnicastList, Bag bag) {
        List<Unicast> solution = new ArrayList<>();

        Map<GCLEdge, Double> edgeDurationMap = null;
        if(!ttUnicastList.isEmpty()){
            solution.addAll(ttUnicastList);
            edgeDurationMap = getEdgeTTDurationMap(ttUnicastList);
        }

        List<UnicastCandidate> sortedSRTUnicastCandidateList = null;

        if(Objects.equals(bag.getSRTUnicastCandidateSortingMethod(), Constants.SRT_UNICAST_CANDIDATE_SORTING_METHOD)){
            sortedSRTUnicastCandidateList = UnicastCandidateSortingMethods.sortUnicastCandidateListForDeadlineAscending(srtUnicastCandidateList);
        }

        Map<GCLEdge, Double> edgeUtilizationMap = new HashMap<>();
        if(Objects.equals(bag.getWPMObjective(), Constants.SRT_TT_LENGTH_UTIL)){
            if(!ttUnicastList.isEmpty()){
                for(Unicast unicast: ttUnicastList){
                    for(GCLEdge edge: unicast.getPath().getEdgeList()){
                        edgeUtilizationMap.put(edge, unicast.getApplication().getMessageSizeMbps());
                    }
                }
            }
        }

        assert sortedSRTUnicastCandidateList != null;
        for (UnicastCandidate unicastCandidate : sortedSRTUnicastCandidateList) {
            ArrayList<Double> srtCostList = new ArrayList<>();
            ArrayList<Double> ttCostList = new ArrayList<>();
            List<GraphPath<Node, GCLEdge>> candidatePathList = unicastCandidate.getCandidatePathList();

            for (GraphPath<Node, GCLEdge> candidatePath : candidatePathList) {
                double srtCost = 0;
                double ttCost = 0;
                for (Unicast unicast : solution) {
                    if (unicast.getApplication() instanceof TTApplication) {
                        ArrayList<GCLEdge> sameElements = getSameEdgeList(candidatePath.getEdgeList(), unicast.getPath().getEdgeList());
                        for (GCLEdge edge : sameElements) {
                            assert edgeDurationMap != null;
                            ttCost += edgeDurationMap.get(edge);
                        }
                    } else if (unicast.getApplication() instanceof SRTApplication srtApplication) {
                        int sameEdgeNumber = getSameEdgeList(candidatePath.getEdgeList(), unicast.getPath().getEdgeList()).size();
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

            if(Objects.equals(bag.getWPMVersion(), Constants.WPM_VERSION_V1)){
                for (int i = 0; i < candidatePathList.size(); i++) {
                    double cost = 0;
                    if(Objects.equals(bag.getWPMObjective(), Constants.SRT_TT)){
                        cost = Math.pow(srtCostList.get(i), bag.getWSRT()) * Math.pow(ttCostList.get(i), bag.getWTT());
                    }
                    else if(Objects.equals(bag.getWPMObjective(), Constants.SRT_TT_LENGTH)){
                        cost = Math.pow(srtCostList.get(i), bag.getWSRT()) * Math.pow(ttCostList.get(i), bag.getWTT()) * Math.pow(candidatePathList.get(i).getEdgeList().size(), bag.getWLength());
                    }
                    else if(Objects.equals(bag.getWPMObjective(), Constants.SRT_TT_LENGTH_UTIL)){
                        Map<GCLEdge, Double> newEdgeUtilizationMap = new HashMap<>(edgeUtilizationMap);
                        for(GCLEdge edge: candidatePathList.get(i).getEdgeList()){
                            if(!newEdgeUtilizationMap.containsKey(edge)){
                                newEdgeUtilizationMap.put(edge, unicastCandidate.getApplication().getMessageSizeMbps());
                            }
                            else{
                                newEdgeUtilizationMap.put(edge, newEdgeUtilizationMap.get(edge) + unicastCandidate.getApplication().getMessageSizeMbps());
                            }
                        }
                        cost = Math.pow(srtCostList.get(i), bag.getWSRT()) * Math.pow(ttCostList.get(i), bag.getWTT()) * Math.pow(candidatePathList.get(i).getEdgeList().size(), bag.getWLength()) * Math.pow(Collections.max(newEdgeUtilizationMap.values()), bag.getWUtil());
                    }

                    if (cost < maxCost) {
                        maxCost = cost;
                        selectedGP = candidatePathList.get(i);
                        if (maxCost == 0) {
                            break;
                        }
                    }
                }
            }
            else {
                selectedGP = v2SRTTTLengthGraphPath(srtCostList, ttCostList, unicastCandidate, edgeUtilizationMap, bag);
            }

            Unicast selectedUnicast = new Unicast(unicastCandidate.getApplication(), unicastCandidate.getTarget(), selectedGP);
            solution.add(selectedUnicast);

            if(Objects.equals(bag.getWPMObjective(), Constants.SRT_TT_LENGTH_UTIL)){
                for(GCLEdge edge: selectedUnicast.getPath().getEdgeList()){
                    edgeUtilizationMap.put(edge, selectedUnicast.getApplication().getMessageSizeMbps());
                }
            }
            
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

            List<Double> weightList = RandomNumberGenerator.generateRandomWeightsSRTTTLengthThreadLocalRandom();

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
