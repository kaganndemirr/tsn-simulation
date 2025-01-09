package ktu.kaganndemirr.util.mcdm;

import ktu.kaganndemirr.application.Application;
import ktu.kaganndemirr.application.SRTApplication;
import ktu.kaganndemirr.application.TTApplication;
import ktu.kaganndemirr.architecture.EndSystem;
import ktu.kaganndemirr.architecture.GCLEdge;
import ktu.kaganndemirr.architecture.Node;
import ktu.kaganndemirr.message.Unicast;
import ktu.kaganndemirr.message.UnicastCandidate;
import ktu.kaganndemirr.util.Bag;
import ktu.kaganndemirr.util.Constants;
import ktu.kaganndemirr.util.UnicastCandidateSortingMethods;
import org.jgrapht.GraphPath;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static ktu.kaganndemirr.util.mcdm.HelperMethods.*;

public class WSMMethods {
    public static List<Double> normalizeGPCostMax(List<GraphPath<Node, GCLEdge>> graphPathList) {
        double max = findMaxCandidateLength(graphPathList);

        List<Double> normalizedList = new ArrayList<>();

        for (GraphPath<Node, GCLEdge> graphPath : graphPathList) {
            normalizedList.add(graphPath.getEdgeList().size() / max);
        }

        return normalizedList;
    }

    public static List<Double> normalizeCostMax(List<Double> list) {
        double max = Collections.max(list);

        List<Double> normalizedList = new ArrayList<>();

        if (max == 0) {
            for (int i = 0; i < list.size(); i++) {
                normalizedList.add(0.0);
            }
        } else {
            for (Double d : list) {
                normalizedList.add(d / max);
            }
        }

        return normalizedList;
    }

    public static List<Unicast> deadlineSRTTTLength(List<UnicastCandidate> srtUnicastCandidateList, List<Unicast> ttUnicastList, String wsmNormalization, double wSRT, double wTT, double wLength) {
        List<UnicastCandidate> sortedSRTUnicastCandidateList = UnicastCandidateSortingMethods.sortUnicastCandidateListForDeadlineAscending(srtUnicastCandidateList);

        List<Unicast> solution = new ArrayList<>();

        if(!ttUnicastList.isEmpty()){
            solution.addAll(ttUnicastList);
        }

        Map<GCLEdge, Double> edgeDurationMap = getEdgeTTDurationMap(ttUnicastList);

        for (UnicastCandidate unicastCandidate : sortedSRTUnicastCandidateList) {
            List<Double> srtCostList = new ArrayList<>();
            List<Double> ttCostList = new ArrayList<>();
            List<GraphPath<Node, GCLEdge>> graphPathList = unicastCandidate.getCandidatePathList();

            List<Double> normalizedCostGPList;
            switch (wsmNormalization) {
                case Constants.MIN_MAX -> normalizedCostGPList = null;
                case Constants.VECTOR -> normalizedCostGPList = null;
                default -> normalizedCostGPList = normalizeGPCostMax(graphPathList);
            }

            for (GraphPath<Node, GCLEdge> graphPath : graphPathList) {
                double srtCost = 0;
                double ttCost = 0;
                for (Unicast unicast : solution) {
                    if (unicast.getApplication() instanceof TTApplication) {
                        ArrayList<GCLEdge> sameElements = getSameEdgeList(graphPath.getEdgeList(), unicast.getPath().getEdgeList());
                        for (GCLEdge edge : sameElements) {
                            ttCost += edgeDurationMap.get(edge);
                        }
                    } else if (unicast.getApplication() instanceof SRTApplication srtApplication) {
                        int sameEdgeNumber = getSameEdgeList(graphPath.getEdgeList(), unicast.getPath().getEdgeList()).size();
                        double unicastCandidateTraffic = (unicastCandidate.getApplication().getFrameSizeByte() * unicastCandidate.getApplication().getNumber0fFrames()) / unicastCandidate.getApplication().getCMI();
                        double unicastTraffic = (srtApplication.getFrameSizeByte() * srtApplication.getNumber0fFrames()) / srtApplication.getCMI();
                        srtCost += sameEdgeNumber * (unicastTraffic * unicastCandidateTraffic);
                    }
                }
                srtCostList.add(srtCost);
                ttCostList.add(ttCost);
            }

            List<Double> normalizedSRTCostList;
            List<Double> normalizedTTCostList;

            switch (wsmNormalization) {
                case Constants.MIN_MAX -> {
                    normalizedSRTCostList = null;
                    normalizedTTCostList = null;
                }
                case Constants.VECTOR -> {
                    normalizedSRTCostList = null;
                    normalizedTTCostList = null;
                }
                default -> {
                    normalizedSRTCostList = normalizeCostMax(srtCostList);
                    normalizedTTCostList = normalizeCostMax(ttCostList);
                }
            }

            double maxCost = Double.MAX_VALUE;
            GraphPath<Node, GCLEdge> selectedGraphPath = null;

            for (int i = 0; i < graphPathList.size(); i++) {
                assert normalizedSRTCostList != null;
                double cost = wSRT * normalizedSRTCostList.get(i) + wTT * normalizedTTCostList.get(i) + wLength * normalizedCostGPList.get(i);
                if (cost < maxCost) {
                    maxCost = cost;
                    selectedGraphPath = graphPathList.get(i);
                    if (maxCost == 0) {
                        break;
                    }
                }
            }

            Unicast selectedU = new Unicast(unicastCandidate.getApplication(), unicastCandidate.getTarget(), selectedGraphPath);
            solution.add(selectedU);

        }
        return solution;
    }

    public static GraphPath<Node, GCLEdge> srtTTLengthGraphPathV1(Bag bag, Application application, EndSystem target, List<GraphPath<Node, GCLEdge>> kShortestPathsGraphPathList, List<Unicast> ttUnicastList, List<GraphPath<Node, GCLEdge>> mcdmGraphPathList, String scenarioOutputPath) {
        List<Unicast> solution = new ArrayList<>();

        if(!ttUnicastList.isEmpty()){
            solution.addAll(ttUnicastList);
        }

        if(!mcdmGraphPathList.isEmpty()){
            for(GraphPath<Node, GCLEdge> graphPath: mcdmGraphPathList){
                solution.add(new Unicast(application, target, graphPath));
            }
        }

        Map<GCLEdge, Double> edgeDurationMap = getEdgeTTDurationMap(ttUnicastList);

        List<Double> srtCostList = new ArrayList<>();
        List<Double> ttCostList = new ArrayList<>();

        List<Double> normalizedCostGPList;
        switch (bag.getWSMNormalization()) {
            case Constants.MIN_MAX -> normalizedCostGPList = null;
            case Constants.VECTOR -> normalizedCostGPList = null;
            default -> normalizedCostGPList = normalizeGPCostMax(kShortestPathsGraphPathList);
        }

        for (GraphPath<Node, GCLEdge> graphPath : kShortestPathsGraphPathList) {
            double srtCost = 0;
            double ttCost = 0;
            for (Unicast unicast : solution) {
                if (unicast.getApplication() instanceof TTApplication) {
                    ArrayList<GCLEdge> sameElements = getSameEdgeList(graphPath.getEdgeList(), unicast.getPath().getEdgeList());
                    for (GCLEdge edge : sameElements) {
                        ttCost += edgeDurationMap.get(edge);
                    }
                } else if (unicast.getApplication() instanceof SRTApplication srtApplication) {
                    int sameEdgeNumber = getSameEdgeList(graphPath.getEdgeList(), unicast.getPath().getEdgeList()).size();
                    double unicastCandidateTraffic = (application.getFrameSizeByte() * application.getNumber0fFrames()) / application.getCMI();
                    double unicastTraffic = (srtApplication.getFrameSizeByte() * srtApplication.getNumber0fFrames()) / srtApplication.getCMI();
                    srtCost += sameEdgeNumber * (unicastTraffic * unicastCandidateTraffic);
                }
            }
            srtCostList.add(srtCost);
            ttCostList.add(ttCost);
        }

        List<Double> normalizedSRTCostList;
        List<Double> normalizedTTCostList;

        switch (bag.getWSMNormalization()) {
            case Constants.MIN_MAX -> {
                normalizedSRTCostList = null;
                normalizedTTCostList = null;
            }
            case Constants.VECTOR -> {
                normalizedSRTCostList = null;
                normalizedTTCostList = null;
            }
            default -> {
                normalizedSRTCostList = normalizeCostMax(srtCostList);
                normalizedTTCostList = normalizeCostMax(ttCostList);
            }
        }



        double maxCost = Double.MAX_VALUE;
        GraphPath<Node, GCLEdge> selectedGraphPath = null;

        for (int i = 0; i < kShortestPathsGraphPathList.size(); i++) {
            assert normalizedSRTCostList != null;
            assert normalizedCostGPList != null;
            double cost = bag.getWSRT() * normalizedSRTCostList.get(i) + bag.getWTT() * normalizedTTCostList.get(i) + bag.getWLength() * normalizedCostGPList.get(i);
            if (cost < maxCost) {
                maxCost = cost;
                selectedGraphPath = kShortestPathsGraphPathList.get(i);
                if (maxCost == 0) {
                    break;
                }
            }
        }

        return selectedGraphPath;
    }
}
