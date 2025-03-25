package ktu.kaganndemirr.util.mcdm;

import ktu.kaganndemirr.application.SRTApplication;
import ktu.kaganndemirr.application.TTApplication;
import ktu.kaganndemirr.architecture.GCL;
import ktu.kaganndemirr.architecture.GCLEdge;
import ktu.kaganndemirr.architecture.Node;
import ktu.kaganndemirr.message.Unicast;
import ktu.kaganndemirr.message.UnicastCandidate;
import org.jgrapht.GraphPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HelperMethods {
    private static final Logger logger = LoggerFactory.getLogger(HelperMethods.class.getSimpleName());

    public static Map<GCLEdge, Double> getEdgeTTDurationMap(List<Unicast> unicastList) {
        Map<GCLEdge, Double> edgeTTDurationMap = new HashMap<>();
        for (Unicast unicast : unicastList) {
            if (unicast.getApplication() instanceof TTApplication){
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

        }
        return edgeTTDurationMap;
    }

    public static int findMaxCandidateLength(List<GraphPath<Node, GCLEdge>> gpList) {
        int maxLength = 0;
        for(GraphPath<Node, GCLEdge> gp: gpList){
            if(gp.getEdgeList().size() > maxLength){
                maxLength = gp.getEdgeList().size();
            }
        }

        return maxLength;
    }

    public static List<GCLEdge> getSameEdgeList(List<GCLEdge> gclEdgeList1, List<GCLEdge> gclEdgeList2) {
        List<GCLEdge> sameEdgeList = new ArrayList<>();
        for (GCLEdge edge : gclEdgeList1) {
            if (gclEdgeList2.contains(edge)) {
                sameEdgeList.add(edge);
            }
        }
        return sameEdgeList;
    }

    public static List<CandidatePathHolder> createSRTTTLengthCandidatePathHolder(UnicastCandidate unicastCandidate, List<Unicast> solution, Map<GCLEdge, Double> edgeDurationMap){
        List<CandidatePathHolder> candidatePathHolderList = new ArrayList<>();
        for (GraphPath<Node, GCLEdge> candidatePath : unicastCandidate.getCandidatePathList()) {
            CandidatePathHolder candidatePathHolder = new CandidatePathHolder();
            candidatePathHolder.setCandidatePath(candidatePath);
            double srtCost = 0;

            for (Unicast unicast : solution) {
                if (unicast.getApplication() instanceof SRTApplication) {
                    int sameEdgeNumber = getSameEdgeList(candidatePath.getEdgeList(), unicast.getPath().getEdgeList()).size();
                    double sSSf = (unicastCandidate.getApplication().getFrameSizeByte() * unicastCandidate.getApplication().getNumber0fFrames()) * (unicast.getApplication().getFrameSizeByte() * unicast.getApplication().getNumber0fFrames());
                    double tSTf = unicastCandidate.getApplication().getCMI() * unicast.getApplication().getCMI();
                    double dSDf = (double) 1 / (unicastCandidate.getApplication().getDeadline() * unicast.getApplication().getDeadline());
                    srtCost += sameEdgeNumber * (sSSf / tSTf) * dSDf;
                }
            }

            candidatePathHolder.setSRTCost(srtCost);

            double ttCost = 0;
            for (Unicast unicast : solution) {
                if (unicast.getApplication() instanceof TTApplication) {
                    List<GCLEdge> sameElements = getSameEdgeList(candidatePath.getEdgeList(), unicast.getPath().getEdgeList());
                    for (GCLEdge edge : sameElements) {
                        ttCost += edgeDurationMap.get(edge);
                    }
                }
            }

            candidatePathHolder.setTTCost(ttCost);
            candidatePathHolder.setLength((double) candidatePath.getEdgeList().size());

            candidatePathHolderList.add(candidatePathHolder);
        }

        return candidatePathHolderList;
    }
}
