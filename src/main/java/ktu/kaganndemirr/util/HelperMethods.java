package ktu.kaganndemirr.util;

import ktu.kaganndemirr.application.Application;
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
import java.nio.file.Path;
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

        resultList.add(bag.getRouting());

        if (bag.getMTRName() != null){
            resultList.add(bag.getMTRName());
        }

        resultList.add(bag.getPathFindingMethod());

        resultList.add(bag.getAlgorithm());

        if (bag.getMCDMObjective() != null){
            resultList.add(bag.getMCDMObjective());
        }

        if (bag.getUnicastCandidateSortingMethod() != null){
            resultList.add(bag.getUnicastCandidateSortingMethod());
        }

        if (bag.getWSRT() != 0){
            resultList.add(String.valueOf(bag.getWSRT()));
        }
        if (bag.getWTT() != 0){
            resultList.add(String.valueOf(bag.getWTT()));
        }
        if (bag.getWLength() != 0){
            resultList.add(String.valueOf(bag.getWLength()));
        }
        if (bag.getWUtil() != 0){
            resultList.add(String.valueOf(bag.getWUtil()));
        }

        if(bag.getMCDMName() != null){
            resultList.add(bag.getMCDMName());
        }

        if (bag.getWSMNormalization() != null){
            resultList.add(bag.getWSMNormalization());
        }

        if(bag.getWPMVersion() != null){
            resultList.add(bag.getWPMVersion());
        }

        if(bag.getWPMValueType() != null)
        {
            resultList.add(bag.getWPMValueType());
        }

        if (bag.getLWR() != null){
            resultList.add(bag.getLWR());
        }

        if (bag.getCWR() != null){
            resultList.add(bag.getCWR());
        }

        if (bag.getK() != 0){
            resultList.add(String.valueOf(bag.getK()));
        }
        if (bag.getMetaheuristicName() != null){
            resultList.add(bag.getMetaheuristicName());
        }
        if (bag.getEvaluatorName() != null){
            resultList.add(bag.getEvaluatorName());
        }

        resultList.add(bag.getTopologyName() + "_" + bag.getApplicationName());

        return buildPath(resultList);
    }

    public static String createResultOutputPath(Bag bag){
        List<String> resultList = new ArrayList<>();
        resultList.add("outputs");

        resultList.add(bag.getRouting());

        if (bag.getMTRName() != null){
            resultList.add(bag.getMTRName());
        }

        resultList.add(bag.getPathFindingMethod());

        resultList.add(bag.getAlgorithm());

        if (bag.getMCDMObjective() != null){
            resultList.add(bag.getMCDMObjective());
        }

        if (bag.getUnicastCandidateSortingMethod() != null){
            resultList.add(bag.getUnicastCandidateSortingMethod());
        }

        if (bag.getWSRT() != 0){
            resultList.add(String.valueOf(bag.getWSRT()));
        }
        if (bag.getWTT() != 0){
            resultList.add(String.valueOf(bag.getWTT()));
        }
        if (bag.getWLength() != 0){
            resultList.add(String.valueOf(bag.getWLength()));
        }
        if (bag.getWUtil() != 0){
            resultList.add(String.valueOf(bag.getWUtil()));
        }

        if(bag.getMCDMName() != null){
            resultList.add(bag.getMCDMName());
        }

        if (bag.getWSMNormalization() != null){
            resultList.add(bag.getWSMNormalization());
        }

        if(bag.getWPMVersion() != null){
            resultList.add(bag.getWPMVersion());
        }

        if(bag.getWPMValueType() != null)
        {
            resultList.add(bag.getWPMValueType());
        }

        if (bag.getLWR() != null){
            resultList.add(bag.getLWR());
        }

        if (bag.getCWR() != null){
            resultList.add(bag.getCWR());
        }

        if (bag.getK() != 0){
            resultList.add(String.valueOf(bag.getK()));
        }
        if (bag.getMetaheuristicName() != null){
            resultList.add(bag.getMetaheuristicName());
        }
        if (bag.getEvaluatorName() != null){
            resultList.add(bag.getEvaluatorName());
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
        String result = "Routing: " + bag.getRouting();
        if (bag.getMTRName() != null){
            result += ", MTR Name: " + bag.getMTRName();
        }
        result += ", PathFindingMethod: " + bag.getPathFindingMethod();
        result += ", Algorithm: " + bag.getAlgorithm();
        if (bag.getUnicastCandidateSortingMethod() != null){
            result += ", Unicast Candidate Sorting Method: " + bag.getUnicastCandidateSortingMethod();
        }
        if (bag.getLWR() != null){
            result += ", LWR: " + bag.getLWR();
        }
        if (bag.getK() != 0){
            result += ", K: " + bag.getK();
        }
        if (bag.getMCDMName() != null){
            result += ", MCDM Name: " + bag.getMCDMName();
        }
        if (bag.getWPMVersion() != null){
            result += ", WPM Version: " + bag.getWPMVersion();
        }
        if (bag.getWPMValueType() != null){
            result += ", WPM Value Type: " + bag.getWPMValueType();
        }
        if (bag.getMCDMObjective() != null){
            result += ", MCDM Objective: " + bag.getMCDMObjective();
        }
        if (bag.getWSMNormalization() != null){
            result += ", WSM Normalization: " + bag.getWSMNormalization();
        }
        if (bag.getCWR() != null){
            result += ", CWR: " + bag.getCWR();
        }
        if (bag.getWSRT() != 0){
            result += ", wSRT: " + bag.getWSRT();
        }
        if (bag.getWTT() != 0){
            result += ", wTT: " + bag.getWTT();
        }
        if (bag.getWLength() != 0){
            result += ", wLength: " + bag.getWLength();
        }
        if (bag.getWUtil() != 0){
            result += ", wUtil: " + bag.getWUtil();
        }
        if (bag.getThreadNumber() != 0){
            result += ", ThreadNumber: " + bag.getThreadNumber();
        }
        if (bag.getTimeout() != 0){
            result += ", Timeout: " + bag.getTimeout() + "(sec)";
        }
        if (bag.getMetaheuristicName() != null){
            result += ", Metaheuristic Name: " + bag.getMetaheuristicName();
        }

        result += ", Evaluator Name: " + bag.getEvaluatorName();

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

    public static List<Integer> findKForTopologies(int k, int virtualTopologyListSize) {
        List<Integer> kList = new ArrayList<>();
        if (k % virtualTopologyListSize == 0) {
            for (int j = 0; j < virtualTopologyListSize; j++) {
                kList.add(k / virtualTopologyListSize);
            }
        } else {
            for (int j = 0; j < virtualTopologyListSize; j++) {
                kList.add(k / virtualTopologyListSize);
            }
            int z = 0;
            while (z < k % virtualTopologyListSize) {
                kList.set(z, kList.get(z) + 1);
                z++;
            }
        }
        return kList;
    }
}
