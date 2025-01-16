package ktu.kaganndemirr.util.mcdm;

import ktu.kaganndemirr.application.Application;
import ktu.kaganndemirr.application.SRTApplication;
import ktu.kaganndemirr.application.TTApplication;
import ktu.kaganndemirr.architecture.GCL;
import ktu.kaganndemirr.architecture.GCLEdge;
import ktu.kaganndemirr.architecture.Node;
import ktu.kaganndemirr.message.Unicast;
import ktu.kaganndemirr.message.UnicastCandidate;
import ktu.kaganndemirr.routing.phy.yen.metaheuristic.WSMLWR;
import ktu.kaganndemirr.util.Bag;
import ktu.kaganndemirr.util.RandomNumberGenerator;
import org.jgrapht.GraphPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.*;

import static ktu.kaganndemirr.util.mcdm.WSMMethods.*;

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

    public static List<List<Double>> getSRTTTLengthCostListForCandidatePathComputation(List<GraphPath<Node, GCLEdge>> kShortestPathsGraphPathList, List<Unicast> solution, Application application, Map<GCLEdge, Double> edgeDurationMap){
        List<List<Double>> srtTTLengthCostList = new ArrayList<>();

        List<Double> srtCostList = new ArrayList<>();
        List<Double> ttCostList = new ArrayList<>();
        List<Double> lengthList = new ArrayList<>();

        for (GraphPath<Node, GCLEdge> graphPath : kShortestPathsGraphPathList) {
            double srtCost = 0;

            for (Unicast unicast : solution) {
                if (unicast.getApplication() instanceof SRTApplication) {
                    int sameEdgeNumber = getSameEdgeList(graphPath.getEdgeList(), unicast.getPath().getEdgeList()).size();
                    double sSSf = (application.getFrameSizeByte() * application.getNumber0fFrames()) * (unicast.getApplication().getFrameSizeByte() * unicast.getApplication().getNumber0fFrames());
                    double tSTf = application.getCMI() * unicast.getApplication().getCMI();
                    srtCost += sameEdgeNumber * tSTf * sSSf;
                }
            }

            srtCostList.add(srtCost);

            double ttCost = 0;
            for (Unicast unicast : solution) {
                if (unicast.getApplication() instanceof TTApplication) {
                    List<GCLEdge> sameElements = getSameEdgeList(graphPath.getEdgeList(), unicast.getPath().getEdgeList());
                    for (GCLEdge edge : sameElements) {
                        ttCost += edgeDurationMap.get(edge);
                    }
                }
            }

            ttCostList.add(ttCost);
            lengthList.add((double) graphPath.getEdgeList().size());
        }

        srtTTLengthCostList.add(srtCostList);
        srtTTLengthCostList.add(ttCostList);
        srtTTLengthCostList.add(lengthList);

        return srtTTLengthCostList;
    }
}
