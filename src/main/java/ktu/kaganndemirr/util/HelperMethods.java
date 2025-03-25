package ktu.kaganndemirr.util;

import ktu.kaganndemirr.application.SRTApplication;
import ktu.kaganndemirr.architecture.GCLEdge;
import ktu.kaganndemirr.architecture.Node;
import ktu.kaganndemirr.message.Unicast;
import ktu.kaganndemirr.message.UnicastCandidate;
import ktu.kaganndemirr.solver.Solution;
import org.jgrapht.GraphPath;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HelperMethods {
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

    public static String createFoundNoSolutionString(Solution solution){
        return "Found No solution: " + solution.getCost().toDetailedString();
    }

    public static String createFoundSolutionString(Solution solution){
        return "Found solution: " + solution.getCost().toDetailedString();
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

    public static String createScenarioOutputPath(Bag bag){
        List<String> resultList = new ArrayList<>();

        resultList.add("outputs");
        resultList.add("TSN Simulation Version=" + bag.getTSNSimulationVersion());
        resultList.add("Routing=" + bag.getRouting());
        if (bag.getMTRName() != null){
            resultList.add("MTR Name=" + bag.getMTRName());
        }
        if(bag.getVTNumber() != 0){
            resultList.add("VT Number=" + bag.getVTNumber());
        }
        resultList.add("Path Finding Method=" + bag.getPathFindingMethod());
        if (bag.getLWR() != null){
            if(bag.getCWR() != null){
                resultList.add("Algorithm=LWRCWR");
            }
        }
        resultList.add("Algorithm=" + bag.getAlgorithm());
        if (bag.getK() != 0){
            resultList.add("K=" + bag.getK());
        }
        if (bag.getMetaheuristicName() != null){
            resultList.add("Metaheuristic Name=" + bag.getMetaheuristicName());
        }
        resultList.add("Evaluator Name=" + bag.getEvaluatorName());
        if(bag.getMCDMName() != null){
            resultList.add("MCDM Name=" + bag.getMCDMName());
        }
        if (bag.getWSMNormalization() != null){
            resultList.add("WSM Normalization=" + bag.getWSMNormalization());
        }
        if(bag.getWPMVersion() != null){
            resultList.add("WPM Version=" + bag.getWPMVersion());
        }
        if(bag.getWPMValueType() != null)
        {
            resultList.add("WPM Value Type=" + bag.getWPMValueType());
        }
        if (bag.getMCDMObjective() != null){
            resultList.add("MCDM Objective=" + bag.getMCDMObjective());
        }
        if (bag.getUnicastCandidateSortingMethod() != null){
            resultList.add("SRT Sorted by=" + bag.getUnicastCandidateSortingMethod());
        }
        if (bag.getLWR() != null){
            if(bag.getCWR() != null){
                resultList.add("LWR=" + bag.getLWR() + ",CWR=" + bag.getCWR());
            }
            else{
                resultList.add("LWR=" + bag.getLWR());
            }
        }
        if (bag.getCWR() != null){
            resultList.add("CWR=" + bag.getCWR());
        }
        if (bag.getWSRT() != 0){
            resultList.add("wSRT=" + bag.getWSRT());
        }
        if (bag.getWTT() != 0){
            resultList.add("wTT=" + bag.getWTT());
        }
        if (bag.getWLength() != 0){
            resultList.add("wLength=" + bag.getWLength());
        }
        if (bag.getWUtil() != 0){
            resultList.add("wUtil=" + bag.getWUtil());
        }

        resultList.add(bag.getTopologyName() + "_" + bag.getApplicationName());

        return buildPath(resultList);
    }

    public static String createResultOutputPath(Bag bag){
        List<String> resultList = new ArrayList<>();

        resultList.add("outputs");
        resultList.add("TSN Simulation Version=" + bag.getTSNSimulationVersion());
        resultList.add("Routing=" + bag.getRouting());
        if (bag.getMTRName() != null){
            resultList.add("MTR Name=" + bag.getMTRName());
        }
        if(bag.getVTNumber() != 0){
            resultList.add("VT Number=" + bag.getVTNumber());
        }
        resultList.add("Path Finding Method=" + bag.getPathFindingMethod());
        if (bag.getLWR() != null){
            if(bag.getCWR() != null){
                resultList.add("Algorithm=LWRCWR");
            }
        }
        resultList.add("Algorithm=" + bag.getAlgorithm());
        if (bag.getK() != 0){
            resultList.add("K=" + bag.getK());
        }
        if (bag.getMetaheuristicName() != null){
            resultList.add("Metaheuristic Name=" + bag.getMetaheuristicName());
        }
        resultList.add("Evaluator Name=" + bag.getEvaluatorName());
        if(bag.getMCDMName() != null){
            resultList.add("MCDM Name=" + bag.getMCDMName());
        }
        if (bag.getWSMNormalization() != null){
            resultList.add("WSM Normalization=" + bag.getWSMNormalization());
        }
        if(bag.getWPMVersion() != null){
            resultList.add("WPM Version=" + bag.getWPMVersion());
        }
        if(bag.getWPMValueType() != null)
        {
            resultList.add("WPM Value Type=" + bag.getWPMValueType());
        }
        if (bag.getMCDMObjective() != null){
            resultList.add("MCDM Objective=" + bag.getMCDMObjective());
        }
        if (bag.getUnicastCandidateSortingMethod() != null){
            resultList.add("SRT Sorted by=" + bag.getUnicastCandidateSortingMethod());
        }
        if (bag.getLWR() != null){
            if(bag.getCWR() != null){
                resultList.add("LWR=" + bag.getLWR() + ",CWR=" + bag.getCWR());
            }
            else{
                resultList.add("LWR=" + bag.getLWR());
            }
        }
        if (bag.getCWR() != null){
            resultList.add("CWR=" + bag.getCWR());
        }
        if (bag.getWSRT() != 0){
            resultList.add("wSRT=" + bag.getWSRT());
        }
        if (bag.getWTT() != 0){
            resultList.add("wTT=" + bag.getWTT());
        }
        if (bag.getWLength() != 0){
            resultList.add("wLength=" + bag.getWLength());
        }
        if (bag.getWUtil() != 0){
            resultList.add("wUtil=" + bag.getWUtil());
        }

        return buildPath(resultList);
    }

    public static void writeSolutionsToFile(List<Unicast> initialSolution, List<Unicast> solution, String scenarioOutputPath, String threadName, int i) throws IOException {
        BufferedWriter initialSolutionWriter = new BufferedWriter(new FileWriter(Paths.get(scenarioOutputPath, "InitialSolution.txt").toString(), true));
        initialSolutionWriter.write("############## ThreadName:" + threadName + " Iteration:" + i + " ##############\n");
        for (Unicast unicast : initialSolution) {
            if (unicast.getApplication() instanceof SRTApplication) {
                initialSolutionWriter.write(unicast.getApplication().getName() + ": ");
                initialSolutionWriter.write(unicast.getPath().getEdgeList() + "\n");
            }
        }

        initialSolutionWriter.close();

        BufferedWriter solutionWriter = new BufferedWriter(new FileWriter(Paths.get(scenarioOutputPath, "Solution.txt").toString(), true));
        solutionWriter.write("############## ThreadName:" + threadName + " Iteration:" + i + " ##############\n");
        for (Unicast unicast : solution) {
            if (unicast.getApplication() instanceof SRTApplication) {
                solutionWriter.write(unicast.getApplication().getName() + ": ");
                solutionWriter.write(unicast.getPath().getEdgeList() + "\n");
            }
        }

        solutionWriter.close();
    }

    public static void writeSRTCandidateRoutesToFile(List<UnicastCandidate> srtUnicastCandidateList, String scenarioOutputPath, String threadName, int i) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(Paths.get(scenarioOutputPath, "SRTCandidateRoutes.txt").toString(), true));
        writer.write("############## ThreadName:" + threadName + " Iteration:" + i + " ##############\n");
        for (UnicastCandidate unicastCandidate : srtUnicastCandidateList) {
            int candidatePathIndex = 0;
            for (GraphPath<Node, GCLEdge> gp : unicastCandidate.getCandidatePathList()) {
                writer.write(unicastCandidate.getApplication().getName() + "_" + candidatePathIndex + "\t" + gp.getEdgeList() + "\n");
                candidatePathIndex++;
            }
        }
        writer.close();
    }

    public static String buildPath(List<String> pathSegments) {
        String separator = File.separator;

        return String.join(separator, pathSegments);
    }

    public static String createInfo(Bag bag){
        String result = "TSN Simulation Version=" + bag.getTSNSimulationVersion();
        result += ", Routing=" + bag.getRouting();
        if (bag.getMTRName() != null){
            if (bag.getVTNumber() != 0){
                result += ", MTR Name=" + bag.getMTRName() + ", VT Number=" + bag.getVTNumber();
            }
            else{
                result += ", MTR Name=" + bag.getMTRName() + ", VT Number=Algorithm Will Decide";
            }
        }
        result += ", PathFindingMethod=" + bag.getPathFindingMethod();
        if (bag.getLWR() != null){
            if(bag.getCWR() != null){
                result += ", Algorithm=LWRCWR";
            }
        }
        result += ", Algorithm=" + bag.getAlgorithm();
        if (bag.getK() != 0){
            result += ", K=" + bag.getK();
        }
        if (bag.getMetaheuristicName() != null){
            result += ", Metaheuristic Name=" + bag.getMetaheuristicName();
        }
        if (bag.getThreadNumber() != 0){
            result += ", ThreadNumber=" + bag.getThreadNumber();
        }
        if (bag.getTimeout() != 0){
            result += ", Timeout=" + bag.getTimeout() + "(sec)";
        }
        result += ", Evaluator Name=" + bag.getEvaluatorName();
        if (bag.getMCDMName() != null){
            result += ", MCDM Name=" + bag.getMCDMName();
        }
        if (bag.getWSMNormalization() != null){
            result += ", WSM Normalization=" + bag.getWSMNormalization();
        }
        if (bag.getWPMVersion() != null){
            result += ", WPM Version=" + bag.getWPMVersion();
        }
        if (bag.getWPMValueType() != null){
            result += ", WPM Value Type=" + bag.getWPMValueType();
        }
        if (bag.getMCDMObjective() != null){
            result += ", MCDM Objective=" + bag.getMCDMObjective();
        }
        if (bag.getUnicastCandidateSortingMethod() != null){
            result += ", SRT Sorted by=" + bag.getUnicastCandidateSortingMethod();
        }
        if (bag.getLWR() != null){
            if(bag.getCWR() != null){
                result += ", LWR=" + bag.getLWR() + ", CWR=" + bag.getCWR();
            }
            else{
                result += ", LWR=" + bag.getLWR();
            }
        }

        if (bag.getCWR() != null){
            result += ", CWR=" + bag.getCWR();
        }
        if (bag.getWSRT() != 0){
            result += ", wSRT=" + bag.getWSRT();
        }
        if (bag.getWTT() != 0){
            result += ", wTT=" + bag.getWTT();
        }
        if (bag.getWLength() != 0){
            result += ", wLength=" + bag.getWLength();
        }
        if (bag.getWUtil() != 0){
            result += ", wUtil=" + bag.getWUtil();
        }
        return result;
    }

    public static List<GraphPath<Node, GCLEdge>> fillKShortestPathGraphPathList(List<GraphPath<Node, GCLEdge>> kShortestPathList, int k){
        if (kShortestPathList.size() != k) {
            int appPathsSize = kShortestPathList.size();
            for (int j = 0; j < k - appPathsSize; j++) {
                kShortestPathList.add(kShortestPathList.getLast());
            }
        }
        return kShortestPathList;
    }
}
