package ktu.kaganndemirr.util;

import ktu.kaganndemirr.application.SRTApplication;
import ktu.kaganndemirr.application.TTApplication;
import ktu.kaganndemirr.architecture.GCL;
import ktu.kaganndemirr.architecture.GCLEdge;
import ktu.kaganndemirr.architecture.Node;
import ktu.kaganndemirr.message.Unicast;
import ktu.kaganndemirr.message.UnicastCandidate;
import org.jgrapht.GraphPath;

import java.util.*;

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

    private static GraphPath<Node, GCLEdge> wpmV2SRTTTLengthGraphPath(double wSRT, double wTT, double wLength, List<Double> srtCostList, List<Double> ttCostList, List<GraphPath<Node, GCLEdge>> graphPathList, String wpmValueType) {
        Map<GraphPath<Node, GCLEdge>, Integer> graphPathPathScore = new HashMap<>();
        GraphPath<Node, GCLEdge> selectedGraphPath;

        if(Objects.equals(wpmValueType, Constants.ACTUAL)){
            for(int i = 0; i < graphPathList.size(); i++){
                for(int j = i + 1; j < graphPathList.size(); j++){
                    if(!graphPathList.get(i).equals(graphPathList.get(j))) {
                        if (!graphPathPathScore.containsKey(graphPathList.get(i))){
                            graphPathPathScore.put(graphPathList.get(i), 0);
                        }
                        if (!graphPathPathScore.containsKey(graphPathList.get(j))){
                            graphPathPathScore.put(graphPathList.get(j), 0);
                        }

                        double cost;
                        if(srtCostList.get(j) == 0 || ttCostList.get(j) == 0){
                            cost = Constants.NEWCOST;
                        }
                        else {
                            cost = Math.pow((srtCostList.get(i) / srtCostList.get(j)), wSRT) * Math.pow((ttCostList.get(i) / ttCostList.get(j)), wTT) * Math.pow(((double) graphPathList.get(i).getLength() / graphPathList.get(j).getLength()), wLength);
                        }

                        if (cost < Constants.WPMTHRESHOLD) {
                            gpPathScore.put(gpList.get(i), gpPathScore.get(gpList.get(i)) + 1);

                        } else {
                            gpPathScore.put(gpList.get(i), gpPathScore.get(gpList.get(j)) + 1);
                        }
                    }
                }
            }
            LinkedHashMap<GraphPath<Node, GCLEdge>, Integer> sortedMapbyScore = new LinkedHashMap<>();
            gpPathScore.entrySet()
                    .stream()
                    .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                    .forEachOrdered(x -> sortedMapbyScore.put(x.getKey(), x.getValue()));

            selectedGP = sortedMapbyScore.entrySet().stream().findFirst().get().getKey();
        }
        else{
            for(int i = 0; i < gpList.size(); i++){
                for(int j = i + 1; j < gpList.size(); j++){
                    if(!gpList.get(i).equals(gpList.get(j))){
                        if (!gpPathScore.containsKey(gpList.get(i))){
                            gpPathScore.put(gpList.get(i), 0);
                        }

                        double relativeAVBCostFirst = costAVBList.get(i) / (costAVBList.get(i) + costTTList.get(i) + gpList.get(i).getLength());
                        double relativeTTCostFirst = costTTList.get(i) / (costAVBList.get(i) + costTTList.get(i) + gpList.get(i).getLength());
                        double relativeLengthCostFirst = gpList.get(i).getLength() / (costAVBList.get(i) + costTTList.get(i) + gpList.get(i).getLength());

                        if (!gpPathScore.containsKey(gpList.get(j))){
                            gpPathScore.put(gpList.get(j), 0);
                        }

                        double cost;
                        if(costAVBList.get(j) == 0 || costTTList.get(j) == 0){
                            cost = Constants.NEWCOST;
                        }
                        else {
                            double adjustedRelativeAVBCost = costAVBList.get(j) / (costAVBList.get(j) + costTTList.get(j) + gpList.get(j).getLength());
                            double adjustedRelativeTTCost = costTTList.get(j) / (costAVBList.get(j) + costTTList.get(j) + gpList.get(j).getLength());
                            double relativeLengthCostSecond = gpList.get(j).getLength() / (costAVBList.get(j) + costTTList.get(j) + gpList.get(j).getLength());

                            cost = Math.pow((relativeAVBCostFirst / adjustedRelativeAVBCost), wAVB) * Math.pow((relativeTTCostFirst / adjustedRelativeTTCost), wTT) * Math.pow((relativeLengthCostFirst / relativeLengthCostSecond), wLength);
                        }

                        if(cost < Constants.WPMTHRESHOLD){
                            gpPathScore.put(gpList.get(i), gpPathScore.get(gpList.get(i)) + 1);
                        }
                        else{
                            gpPathScore.put(gpList.get(j), gpPathScore.get(gpList.get(j)) + 1);
                        }
                    }
                }
            }
            LinkedHashMap<GraphPath<Node, GCLEdge>, Integer> sortedMapbyScore = new LinkedHashMap<>();
            gpPathScore.entrySet()
                    .stream()
                    .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                    .forEachOrdered(x -> sortedMapbyScore.put(x.getKey(), x.getValue()));

            selectedGP = sortedMapbyScore.entrySet().stream().findFirst().get().getKey();
        }
        return selectedGP;
    }

    public static List<Unicast> WPMDeadlineSRTTTLength(List<UnicastCandidate> srtUnicastCandidateList, List<Unicast> ttUnicastList, double wSRT, double wTT, double wLength, String wpmVersion, String wpmValueType) {
        srtUnicastCandidateList.sort(Comparator.comparingInt(unicastCandidate -> unicastCandidate.getApplication().getDeadline()));

        List<Unicast> solution = new ArrayList<>();

        if(!ttUnicastList.isEmpty()){
            solution.addAll(ttUnicastList);
        }

        Map<GCLEdge, Double> edgeDurationMap = getEdgeTTDurationMap(ttUnicastList);

        for (UnicastCandidate unicastCandidate : srtUnicastCandidateList) {
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
                selectedGP = MCDMSpecificMethods.getWPMv2AVBTTLengthGraphPath(wSRT, wTT, wLength, srtCostList, ttCostList, gpList, wpmValueType);
            }


            Unicast selectedU = new Unicast(unicastCandidate.getApplication(), unicastCandidate.getTarget(), selectedGP);
            solution.add(selectedU);

        }
        return partialSolution;
    }
}
