package ktu.kaganndemirr.util;

import ktu.kaganndemirr.application.SRTApplication;
import ktu.kaganndemirr.architecture.GCLEdge;
import ktu.kaganndemirr.message.Unicast;
import ktu.kaganndemirr.solver.Solution;
import ktu.kaganndemirr.util.holders.Bag;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HelperMethods {
    public static String createFoundNoSolutionString(Solution solution){
        return "Found No solution: " + solution.getCost().toDetailedString();
    }

    public static String createFoundSolutionString(Solution solution){
        return "Found solution: " + solution.getCost().toDetailedString();
    }

    public static String createInfoString(Bag bag){
        String infoString = "Solving problem using " + bag.getRouting() + ", " + bag.getPathFindingMethod() + ", " + bag.getAlgorithm();

        if(bag.getThreadNumber() != 0){
            infoString += ", ThreadNumber: " + bag.getThreadNumber();
        }

        if(bag.getTimeout() != 0){
            infoString += ", Timeout: " + bag.getTimeout();
        }

        if(bag.getLWR() != null){
            infoString += ", LWR: " + bag.getLWR();
        }

        if(bag.getK() != 0){
            infoString += ", K: " + bag.getK();
        }

        if(bag.getWPMObjective() != null){
            infoString += ", wpmObjective: " + bag.getWPMObjective();
        }

        if(bag.getCWR() != null){
            infoString += ", CWR: " + bag.getCWR();
        }

        if(bag.getWSRT() != 0){
            infoString += ", wSRT: " + bag.getWSRT();
        }

        if(bag.getWTT() != 0){
            infoString += ", wTT: " + bag.getWTT();
        }

        if(bag.getWLength() != 0){
            infoString += ", wLength: " + bag.getWLength();
        }

        if(bag.getWUtil() != 0){
            infoString += ", wUtil: " + bag.getWUtil();
        }

        if(bag.getWPMVersion() != null){
            infoString += ", wpmVersion: " + bag.getWPMVersion();
        }

        if(bag.getWPMValueType() != null){
            infoString += ", wpmValueType: " + bag.getWPMValueType();
        }

        if(bag.getMetaheuristicName() != null){
            infoString += ", metaheuristicName: " + bag.getMetaheuristicName();
        }

        if(bag.getEvaluatorName() != null){
            infoString += ", evaluatorName: " + bag.getEvaluatorName();
        }
        
        return infoString;
    }

    public static double findAveragePathLengthIncludingES(List<Unicast> solution) {
        double total = 0;
        int size = 0;
        for (Unicast unicast : solution) {
            if (unicast.getApplication() instanceof SRTApplication) {
                total += unicast.getPath().getEdgeList().size();
                size++;
            }
        }

        return total / size;
    }

    public static double findAveragePathLengthWithoutES(List<Unicast> solution) {
        List<List<GCLEdge>> onlySwitchLinkList = solution.stream()
                .map(item -> item.getPath().getEdgeList().subList(1, item.getPath().getEdgeList().size() - 1)).toList();

        Map<Integer, Integer> lengthMap = new HashMap<>();

        for (int i = 0; i < onlySwitchLinkList.size(); i++) {
            List<GCLEdge> subList = onlySwitchLinkList.get(i);
            lengthMap.put(i, subList.size());
        }

        double total = 0;
        for (int i = 0; i < lengthMap.size(); i++) {
            total += lengthMap.get(i);
        }

        return total / lengthMap.size();
    }

    public static String createSolutionInfoString(String topologyOutputLocation){
        return "Routes written to " + Paths.get(topologyOutputLocation, "Routes.txt file.");
    }

    public static String createWCDInfoString(String topologyOutputLocation){
        return "WCDs, average WCD, variance and std written to " + Paths.get(topologyOutputLocation, "WCDs.txt file.");
    }

    public static String createWCDResultString(String mainOutputLocation){
        return "Also average WCD, variance and std written to " + Paths.get(mainOutputLocation, "Results.txt file.");
    }

    public static String createLinkUtilizationNameInfoString(String topologyOutputLocation){
        return "Link utilization's sorted by link names " + Paths.get(topologyOutputLocation, "LinkUtilsSortedByNames.txt file.");
    }

    public static String createLinkUtilizationUtilInfoString(String topologyOutputLocation){
        return "Link utilization's sorted by link utilization's " + Paths.get(topologyOutputLocation, "LinkUtilsSortedByUtils.txt file.");
    }

    public static String createLinkUtilizationResultString(String mainOutputLocation){
        return "Unused Links, Max Loaded Link Number, Max Loaded Link Utilization, Average Link Utilization, Variance and Std written to " + Paths.get(mainOutputLocation, "Results.txt");
    }

    public static String createDurationResultString(String mainOutputLocation){
        return "Costs and computation times written to " + Paths.get(mainOutputLocation, "Results.txt file.");
    }

    public static String createSRTCandidateInfoString(String topologyOutputLocation){
        return "SRT Candidate Routes written to " + Paths.get(topologyOutputLocation, "SRTCandidateRoutes.txt file.");
    }
}
