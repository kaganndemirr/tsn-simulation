package ktu.kaganndemirr.util;

import ktu.kaganndemirr.architecture.GCLEdge;
import ktu.kaganndemirr.architecture.Node;
import ktu.kaganndemirr.message.Unicast;
import ktu.kaganndemirr.message.UnicastCandidate;
import org.jgrapht.GraphPath;

import java.util.*;
import java.util.stream.Collectors;

public class UMethods {
    @SafeVarargs
    private static Set<GCLEdge> findDuplicateKeys(Map<GCLEdge, ?>... maps) {
        Set<GCLEdge> keys = new HashSet<>();
        return Arrays.stream(maps)
                .flatMap(map -> map.keySet().stream())
                .filter(key -> !keys.add(key))
                .collect(Collectors.toSet());
    }

    public static List<Unicast> constructSolution(Bag bag, List<UnicastCandidate> unicastCandidateList, List<Unicast> unicastList) {

        List<Unicast> solution = new ArrayList<>(unicastList);


        Map<GCLEdge, Double> utilizationMap = new HashMap<>();
        Map<GCLEdge, Double> bestUtilizationMap = new HashMap<>();

        if (!solution.isEmpty()) {

            for (Unicast unicast : solution) {
                for (GCLEdge edge : unicast.getPath().getEdgeList()) {
                    if (!utilizationMap.containsKey(edge)) {
                        if (Objects.equals(bag.getTSNSimulationVersion(), Constants.TSNCF)){
                            utilizationMap.put(edge, unicast.getApplication().getMessageSizeMbps() / bag.getRate());
                        } else {
                            utilizationMap.put(edge, unicast.getApplication().getFrameSizeByte() * unicast.getApplication().getNumber0fFrames() / unicast.getApplication().getCMI());
                        }

                    } else {
                        if (Objects.equals(bag.getTSNSimulationVersion(), Constants.TSNCF)){
                            utilizationMap.put(edge, utilizationMap.get(edge) + unicast.getApplication().getMessageSizeMbps() / bag.getRate());
                        } else {
                            utilizationMap.put(edge, utilizationMap.get(edge) + unicast.getApplication().getFrameSizeByte() * unicast.getApplication().getNumber0fFrames() / unicast.getApplication().getCMI());
                        }
                    }
                }
            }
        }

        Unicast bestUnicast = null;
        for (UnicastCandidate unicastCandidate : unicastCandidateList) {
            if (solution.isEmpty()) {
                solution.add(new Unicast(unicastCandidate.getApplication(), unicastCandidate.getTarget(), unicastCandidate.getCandidatePathList().getFirst()));
                for (GCLEdge edge : unicastCandidate.getCandidatePathList().getFirst().getEdgeList()) {
                    if(!utilizationMap.containsKey(edge)){
                        if (Objects.equals(bag.getTSNSimulationVersion(), Constants.TSNCF)){
                            utilizationMap.put(edge, unicastCandidate.getApplication().getMessageSizeMbps() / bag.getRate());
                        }else{
                            utilizationMap.put(edge, unicastCandidate.getApplication().getFrameSizeByte() * unicastCandidate.getApplication().getNumber0fFrames() / unicastCandidate.getApplication().getCMI());
                        }

                    }
                    else{
                        if (Objects.equals(bag.getTSNSimulationVersion(), Constants.TSNCF)){
                            utilizationMap.put(edge, utilizationMap.get(edge) + unicastCandidate.getApplication().getMessageSizeMbps() / bag.getRate());
                        }else {
                            utilizationMap.put(edge, utilizationMap.get(edge) + unicastCandidate.getApplication().getFrameSizeByte() * unicastCandidate.getApplication().getNumber0fFrames() / unicastCandidate.getApplication().getCMI());
                        }

                    }
                }
            } else {
                double minUtilization = Double.MAX_VALUE;
                for (GraphPath<Node, GCLEdge> candidatePath : unicastCandidate.getCandidatePathList()) {
                    Map<GCLEdge, Double> candidateUtilizationMap = new HashMap<>();
                    for (GCLEdge edge : candidatePath.getEdgeList()) {
                        if (Objects.equals(bag.getTSNSimulationVersion(), Constants.TSNCF)){
                            candidateUtilizationMap.put(edge, unicastCandidate.getApplication().getMessageSizeMbps() / bag.getRate());
                        }else {
                            candidateUtilizationMap.put(edge, unicastCandidate.getApplication().getFrameSizeByte() * unicastCandidate.getApplication().getNumber0fFrames() / unicastCandidate.getApplication().getCMI());
                        }

                    }

                    double maxUmax = 0;
                    Set<GCLEdge> sameEdgeSet = findDuplicateKeys(utilizationMap, candidateUtilizationMap);
                    if (!sameEdgeSet.isEmpty()) {
                        ArrayList<Double> sameEdgeUtil = new ArrayList<>();
                        for (GCLEdge edge : sameEdgeSet) {
                            sameEdgeUtil.add(candidateUtilizationMap.get(edge) + utilizationMap.get(edge));
                        }
                        maxUmax = Collections.max(sameEdgeUtil);
                    }

                    if (maxUmax == 0) {
                        bestUtilizationMap.putAll(utilizationMap);
                        bestUnicast = new Unicast(unicastCandidate.getApplication(), unicastCandidate.getTarget(), candidatePath);
                        break;
                    }

                    if (maxUmax < minUtilization) {
                        Map<GCLEdge, Double> utilizationMapCopy = new HashMap<>(utilizationMap);
                        for (Map.Entry<GCLEdge, Double> entry : candidateUtilizationMap.entrySet()) {
                            if (utilizationMapCopy.containsKey(entry.getKey())) {
                                utilizationMapCopy.put(entry.getKey(), utilizationMapCopy.get(entry.getKey()) + candidateUtilizationMap.get(entry.getKey()));
                            } else {
                                utilizationMapCopy.put(entry.getKey(), candidateUtilizationMap.get(entry.getKey()));
                            }
                        }

                        bestUtilizationMap = utilizationMapCopy;
                        bestUnicast = new Unicast(unicastCandidate.getApplication(), unicastCandidate.getTarget(), candidatePath);
                        minUtilization = maxUmax;
                    }
                }

                solution.add(bestUnicast);
                utilizationMap = bestUtilizationMap;
            }
        }
        return solution;
    }
}
