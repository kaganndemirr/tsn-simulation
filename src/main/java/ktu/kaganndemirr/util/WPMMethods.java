package ktu.kaganndemirr.util;

import ktu.kaganndemirr.application.SRTApplication;
import ktu.kaganndemirr.application.TTApplication;
import ktu.kaganndemirr.architecture.GCL;
import ktu.kaganndemirr.architecture.GCLEdge;
import ktu.kaganndemirr.architecture.Node;
import ktu.kaganndemirr.message.Unicast;
import ktu.kaganndemirr.message.UnicastCandidate;
import ktu.kaganndemirr.util.constants.Constants;
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

    private static Map<GCLEdge, Double> createEdgeUtilizationMap(Map<GCLEdge, Double> edgeUtilizationMap, UnicastCandidate unicastCandidate, GraphPath<Node, GCLEdge> candidatePath) {
        Map<GCLEdge, Double> newEdgeUtilizationMap = new HashMap<>(edgeUtilizationMap);
        for(GCLEdge edge: candidatePath.getEdgeList()){
            if(!newEdgeUtilizationMap.containsKey(edge)){
                newEdgeUtilizationMap.put(edge, unicastCandidate.getApplication().getMessageSizeMbps());
            }
            else{
                newEdgeUtilizationMap.put(edge, newEdgeUtilizationMap.get(edge) + unicastCandidate.getApplication().getMessageSizeMbps());
            }
        }
        return newEdgeUtilizationMap;
    }

    private static GraphPath<Node, GCLEdge> v2SRTTTLengthGraphPath(List<Double> srtCostList, List<Double> ttCostList, UnicastCandidate unicastCandidate, Map<GCLEdge, Double> edgeUtilizationMap, Bag bag) {
        Map<GraphPath<Node, GCLEdge>, Integer> candidatePathScoreMap = new HashMap<>();
        GraphPath<Node, GCLEdge> selectedCandidatePath;

        for(int i = 0; i < unicastCandidate.getCandidatePathList().size(); i++){
            for(int j = i + 1; j < unicastCandidate.getCandidatePathList().size(); j++){
                if(!unicastCandidate.getCandidatePathList().get(i).equals(unicastCandidate.getCandidatePathList().get(j))) {
                    if (!candidatePathScoreMap.containsKey(unicastCandidate.getCandidatePathList().get(i))){
                        candidatePathScoreMap.put(unicastCandidate.getCandidatePathList().get(i), 0);
                    }
                    if (!candidatePathScoreMap.containsKey(unicastCandidate.getCandidatePathList().get(j))){
                        candidatePathScoreMap.put(unicastCandidate.getCandidatePathList().get(j), 0);
                    }

                    double cost = 0;
                    if(srtCostList.get(j) == 0 || ttCostList.get(j) == 0){
                        cost = Constants.NEW_COST;
                    }

                    if(Objects.equals(bag.getWPMValueType(), Constants.ACTUAL)){
                        if(Objects.equals(bag.getWPMObjective(), Constants.SRT_TT)){
                            cost = Math.pow((srtCostList.get(i) / srtCostList.get(j)), bag.getWSRT()) * Math.pow((ttCostList.get(i) / ttCostList.get(j)), bag.getWTT());
                        } else if (Objects.equals(bag.getWPMObjective(), Constants.SRT_TT_LENGTH)) {
                            cost = Math.pow((srtCostList.get(i) / srtCostList.get(j)), bag.getWSRT()) * Math.pow((ttCostList.get(i) / ttCostList.get(j)), bag.getWTT()) * Math.pow(((double) unicastCandidate.getCandidatePathList().get(i).getLength() / unicastCandidate.getCandidatePathList().get(j).getLength()), bag.getWLength());
                        } else if (Objects.equals(bag.getWPMObjective(), Constants.SRT_TT_LENGTH_UTIL)) {
                            Map<GCLEdge, Double> newEdgeUtilizationMap = createEdgeUtilizationMap(edgeUtilizationMap, unicastCandidate, unicastCandidate.getCandidatePathList().get(i));
                            cost = Math.pow((srtCostList.get(i) / srtCostList.get(j)), bag.getWSRT()) * Math.pow((ttCostList.get(i) / ttCostList.get(j)), bag.getWTT()) * Math.pow(((double) unicastCandidate.getCandidatePathList().get(i).getLength() / unicastCandidate.getCandidatePathList().get(j).getLength()), bag.getWLength()) * Math.pow(Collections.max(newEdgeUtilizationMap.values()), bag.getWUtil());
                        }
                    } else if (Objects.equals(bag.getWPMValueType(), Constants.RELATIVE)) {
                        double relativeSRTCostI;
                        double relativeTTCostI;
                        double relativeLengthCostI;
                        double relativeUtilCostI;

                        double relativeSRTCostJ;
                        double relativeTTCostJ;
                        double relativeLengthCostJ;
                        double relativeUtilCostJ;

                        if(Objects.equals(bag.getWPMObjective(), Constants.SRT_TT)){
                            relativeSRTCostI = srtCostList.get(i) / (srtCostList.get(i) + ttCostList.get(i) + unicastCandidate.getCandidatePathList().get(i).getLength());
                            relativeTTCostI =  ttCostList.get(i) / (srtCostList.get(i) + ttCostList.get(i) + unicastCandidate.getCandidatePathList().get(i).getLength());

                            relativeSRTCostJ = srtCostList.get(j) / (srtCostList.get(j) + ttCostList.get(j) + unicastCandidate.getCandidatePathList().get(j).getLength());
                            relativeTTCostJ =  ttCostList.get(j) / (srtCostList.get(j) + ttCostList.get(j) + unicastCandidate.getCandidatePathList().get(j).getLength());

                            cost = Math.pow((relativeSRTCostI / relativeSRTCostJ), bag.getWSRT()) * Math.pow((relativeTTCostI / relativeTTCostJ), bag.getWTT());

                        } else if (Objects.equals(bag.getWPMObjective(), Constants.SRT_TT_LENGTH)) {
                            relativeSRTCostI = srtCostList.get(i) / (srtCostList.get(i) + ttCostList.get(i) + unicastCandidate.getCandidatePathList().get(i).getLength());
                            relativeTTCostI =  ttCostList.get(i) / (srtCostList.get(i) + ttCostList.get(i) + unicastCandidate.getCandidatePathList().get(i).getLength());
                            relativeLengthCostI =  unicastCandidate.getCandidatePathList().get(i).getLength() / (srtCostList.get(i) + ttCostList.get(i) + unicastCandidate.getCandidatePathList().get(i).getLength());

                            relativeSRTCostJ = srtCostList.get(j) / (srtCostList.get(j) + ttCostList.get(j) + unicastCandidate.getCandidatePathList().get(j).getLength());
                            relativeTTCostJ =  ttCostList.get(j) / (srtCostList.get(j) + ttCostList.get(j) + unicastCandidate.getCandidatePathList().get(j).getLength());
                            relativeLengthCostJ =  unicastCandidate.getCandidatePathList().get(j).getLength() / (srtCostList.get(j) + ttCostList.get(j) + unicastCandidate.getCandidatePathList().get(j).getLength());

                            cost = Math.pow((relativeSRTCostI / relativeSRTCostJ), bag.getWSRT()) * Math.pow((relativeTTCostI / relativeTTCostJ), bag.getWTT()) * Math.pow((relativeLengthCostI / relativeLengthCostJ), bag.getWLength());
                        } else if (Objects.equals(bag.getWPMObjective(), Constants.SRT_TT_LENGTH_UTIL)) {
                            Map<GCLEdge, Double> newEdgeUtilizationMapI = createEdgeUtilizationMap(edgeUtilizationMap, unicastCandidate, unicastCandidate.getCandidatePathList().get(i));
                            double utilCostI = Collections.max(newEdgeUtilizationMapI.values());
                            relativeSRTCostI = srtCostList.get(i) / (srtCostList.get(i) + ttCostList.get(i) + unicastCandidate.getCandidatePathList().get(i).getLength() + utilCostI);
                            relativeTTCostI =  ttCostList.get(i) / (srtCostList.get(i) + ttCostList.get(i) + unicastCandidate.getCandidatePathList().get(i).getLength() + utilCostI);
                            relativeLengthCostI =  unicastCandidate.getCandidatePathList().get(i).getLength() / (srtCostList.get(i) + ttCostList.get(i) + unicastCandidate.getCandidatePathList().get(i).getLength() + utilCostI);
                            relativeUtilCostI = utilCostI / (srtCostList.get(i) + ttCostList.get(i) + unicastCandidate.getCandidatePathList().get(i).getLength() + utilCostI);

                            Map<GCLEdge, Double> newEdgeUtilizationMapJ = createEdgeUtilizationMap(edgeUtilizationMap, unicastCandidate, unicastCandidate.getCandidatePathList().get(j));
                            double utilCostJ = Collections.max(newEdgeUtilizationMapJ.values());
                            relativeSRTCostJ = srtCostList.get(j) / (srtCostList.get(j) + ttCostList.get(j) + unicastCandidate.getCandidatePathList().get(j).getLength() + utilCostJ);
                            relativeTTCostJ =  ttCostList.get(j) / (srtCostList.get(j) + ttCostList.get(j) + unicastCandidate.getCandidatePathList().get(j).getLength() + utilCostJ);
                            relativeLengthCostJ =  unicastCandidate.getCandidatePathList().get(j).getLength() / (srtCostList.get(j) + ttCostList.get(j) + unicastCandidate.getCandidatePathList().get(j).getLength() + utilCostJ);
                            relativeUtilCostJ = utilCostJ / (srtCostList.get(j) + ttCostList.get(j) + unicastCandidate.getCandidatePathList().get(j).getLength() + utilCostJ);

                            cost = Math.pow((relativeSRTCostI / relativeSRTCostJ), bag.getWSRT()) * Math.pow((relativeTTCostI / relativeTTCostJ), bag.getWTT()) * Math.pow((relativeLengthCostI / relativeLengthCostJ), bag.getWLength()) * Math.pow((relativeUtilCostI / relativeUtilCostJ), bag.getWUtil());
                        }
                    }
                    if (cost < Constants.WPM_THRESHOLD) {
                        candidatePathScoreMap.put(unicastCandidate.getCandidatePathList().get(i), candidatePathScoreMap.get(unicastCandidate.getCandidatePathList().get(i)) + 1);

                    } else {
                        candidatePathScoreMap.put(unicastCandidate.getCandidatePathList().get(j), candidatePathScoreMap.get(unicastCandidate.getCandidatePathList().get(j)) + 1);
                    }
                }
            }
        }

        //All graphPaths same so pick first one
        if(candidatePathScoreMap.isEmpty()){
            candidatePathScoreMap.put(unicastCandidate.getCandidatePathList().getFirst(), 1);
        }

        Map<GraphPath<Node, GCLEdge>, Integer> sortedGraphPathPathScoreMap = candidatePathScoreMap.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));

        selectedCandidatePath = sortedGraphPathPathScoreMap.entrySet().stream().findFirst().get().getKey();

        return selectedCandidatePath;
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

            if(bag.getCWR() != null){
                if(Objects.equals(bag.getCWR(), Constants.THREAD_LOCAL_RANDOM)){
                    List<Double> weightList = RandomNumberGenerator.generateRandomWeightsAVBTTLength();
                }
            }

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
                        Map<GCLEdge, Double> newEdgeUtilizationMap = createEdgeUtilizationMap(edgeUtilizationMap, unicastCandidate, candidatePathList.get(i));
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
}
