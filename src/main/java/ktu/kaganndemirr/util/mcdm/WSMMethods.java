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
import ktu.kaganndemirr.util.RandomNumberGenerator;
import ktu.kaganndemirr.util.UnicastCandidateSortingMethods;
import org.jgrapht.GraphPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

import static ktu.kaganndemirr.util.mcdm.HelperMethods.*;

public class WSMMethods {
    private static final Logger logger = LoggerFactory.getLogger(WSMMethods.class.getSimpleName());

    public static List<Double> normalizeSRTTTLengthCostListMax(List<Double> srtTTCostlist) {
        double max = Collections.max(srtTTCostlist);

        List<Double> normalizedSRTTTCostlist = new ArrayList<>();

        if (max == 0) {
            for (int i = 0; i < srtTTCostlist.size(); i++) {
                normalizedSRTTTCostlist.add(0.0);
            }
        } else {
            for (Double d : srtTTCostlist) {
                normalizedSRTTTCostlist.add(d / max);
            }
        }

        return normalizedSRTTTCostlist;
    }

    public static List<Double> normalizeSRTTTLengthCostListVector(List<Double> srtTTCostlist) {
        List<Double> normalizedList = new ArrayList<>();
        double total = 0.0;
        for (Double d : srtTTCostlist) {
            total += d * d;
        }
        double totalSqrt = Math.sqrt(total);

        if (totalSqrt == 0) {
            for (Double ignored : srtTTCostlist) {
                normalizedList.add(0.0);
            }
        } else {
            for (Double d : srtTTCostlist) {
                normalizedList.add(d / totalSqrt);
            }
        }

        return normalizedList;
    }

    public static List<Double> normalizeSRTTTLengthCostListMinMax(List<Double> srtTTCostlist) {
        double min = Collections.min(srtTTCostlist);
        double max = Collections.max(srtTTCostlist);

        ArrayList<Double> normalizedList = new ArrayList<>();

        if (min == max && min == 0) {
            for (int i = 0; i < srtTTCostlist.size(); i++) {
                normalizedList.add(0.0);
            }
        } else if (min == max) {
            for (int i = 0; i < srtTTCostlist.size(); i++) {
                normalizedList.add(1.0);
            }
        } else {
            for (Double d : srtTTCostlist) {
                double normalizedValue = (d - min) / (max - min);
                normalizedList.add(normalizedValue);
            }
        }

        return normalizedList;
    }

    public static List<Unicast> getSRTTTLengthCostList(Bag bag, List<UnicastCandidate> sortedSRTUnicastCandidateList, List<Unicast> solution, Map<GCLEdge, Double> edgeDurationMap, BufferedWriter costsWriter) throws IOException {

        for (UnicastCandidate unicastCandidate : sortedSRTUnicastCandidateList) {
            List<Double> srtCostList = new ArrayList<>();
            List<Double> ttCostList = new ArrayList<>();
            List<Double> lengthList = new ArrayList<>();

            for (GraphPath<Node, GCLEdge> graphPath : unicastCandidate.getCandidatePathList()) {
                double srtCost = 0;

                for (Unicast unicast : solution) {
                    if (unicast.getApplication() instanceof SRTApplication) {
                        int sameEdgeNumber = getSameEdgeList(graphPath.getEdgeList(), unicast.getPath().getEdgeList()).size();
                        double sSSf = (unicastCandidate.getApplication().getFrameSizeByte() * unicastCandidate.getApplication().getNumber0fFrames()) * (unicast.getApplication().getFrameSizeByte() * unicast.getApplication().getNumber0fFrames());
                        double tSTf = unicastCandidate.getApplication().getCMI() * unicast.getApplication().getCMI();
                        double dSDf = (double) 1 / (unicastCandidate.getApplication().getDeadline() * unicast.getApplication().getDeadline());
                        srtCost += sameEdgeNumber * (sSSf / tSTf) * dSDf;
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

            List<Double> normalizedSRTCostList = null;
            List<Double> normalizedTTCostList = null;
            List<Double> normalizedLengthList = null;
            switch (bag.getWSMNormalization()) {
                case MCDMConstants.MIN_MAX -> {
                    normalizedSRTCostList = normalizeSRTTTLengthCostListMinMax(srtCostList);
                    normalizedTTCostList = normalizeSRTTTLengthCostListMinMax(ttCostList);
                    normalizedLengthList = normalizeSRTTTLengthCostListMinMax(lengthList);
                }
                case MCDMConstants.VECTOR -> {
                    normalizedSRTCostList = normalizeSRTTTLengthCostListVector(srtCostList);
                    normalizedTTCostList = normalizeSRTTTLengthCostListVector(ttCostList);
                    normalizedLengthList = normalizeSRTTTLengthCostListVector(lengthList);
                }
                case MCDMConstants.MAX -> {
                    {
                        normalizedSRTCostList = normalizeSRTTTLengthCostListMax(srtCostList);
                        normalizedTTCostList = normalizeSRTTTLengthCostListMax(ttCostList);
                        normalizedLengthList = normalizeSRTTTLengthCostListMax(lengthList);
                    }
                }
            }

            double maxCost = Double.MAX_VALUE;
            GraphPath<Node, GCLEdge> selectedGraphPath = null;

            double wSRT = bag.getWSRT();
            double wTT = bag.getWTT();
            double wLength = bag.getWLength();
            if (Objects.equals(bag.getCWR(), MCDMConstants.THREAD_LOCAL_RANDOM)){
                List<Double> weightList = RandomNumberGenerator.generateRandomWeightsAVBTTLengthThreadLocalRandom();
                wSRT = weightList.getFirst();
                wTT = weightList.get(1);
                wLength = weightList.getLast();
            }

            List<Double> resultList = null;
            if(logger.isDebugEnabled()){
                resultList = new ArrayList<>();
            }
            for (int i = 0; i < unicastCandidate.getCandidatePathList().size(); i++) {
                assert normalizedSRTCostList != null;
                double cost = wSRT * normalizedSRTCostList.get(i) + wTT * normalizedTTCostList.get(i) + wLength * normalizedLengthList.get(i);
                if(logger.isDebugEnabled()){
                    assert resultList != null;
                    resultList.add(cost);
                }
                if (cost < maxCost) {
                    maxCost = cost;
                    selectedGraphPath = unicastCandidate.getCandidatePathList().get(i);
                    if (maxCost == 0) {
                        break;
                    }
                }
            }

            if(logger.isDebugEnabled()){
                costsWriter.write(unicastCandidate.getApplication().getName() + "\n");
                costsWriter.write(normalizedSRTCostList + "\n");
                costsWriter.write(normalizedTTCostList + "\n");
                costsWriter.write(normalizedLengthList + "\n");
                costsWriter.write(resultList + "\n");
                costsWriter.newLine();
            }

            solution.add(new Unicast(unicastCandidate.getApplication(), unicastCandidate.getTarget(), selectedGraphPath));

        }

        return solution;
    }

    public static List<Unicast> srtTTLength(Bag bag, List<UnicastCandidate> srtUnicastCandidateList, List<Unicast> unicastList, BufferedWriter costsWriter) throws IOException {
        List<UnicastCandidate> sortedSRTUnicastCandidateList = null;
        if(Objects.equals(bag.getUnicastCandidateSortingMethod(), MCDMConstants.DEADLINE)){
            sortedSRTUnicastCandidateList = UnicastCandidateSortingMethods.sortUnicastCandidateListForDeadlineAscending(srtUnicastCandidateList);
        }

        List<Unicast> solution = new ArrayList<>();

        if(!unicastList.isEmpty()){
            solution.addAll(unicastList);
        }

        Map<GCLEdge, Double> edgeDurationMap = getEdgeTTDurationMap(unicastList);

        assert sortedSRTUnicastCandidateList != null;
        return getSRTTTLengthCostList(bag, sortedSRTUnicastCandidateList, solution, edgeDurationMap, costsWriter);
    }

    public static GraphPath<Node, GCLEdge> srtTTLengthForCandidatePathComputing(Bag bag, Application application, EndSystem target, List<GraphPath<Node, GCLEdge>> kShortestPathsGraphPathList, List<Unicast> unicastList, List<GraphPath<Node, GCLEdge>> mcdmGraphPathList, BufferedWriter costsWriter, int candidatePathIndex) throws IOException {
        List<Unicast> solution = new ArrayList<>();

        if(!unicastList.isEmpty()){
            solution.addAll(unicastList);
        }

        if(!mcdmGraphPathList.isEmpty()){
            for(GraphPath<Node, GCLEdge> graphPath: mcdmGraphPathList){
                solution.add(new Unicast(application, target, graphPath));
            }
        }

        Map<GCLEdge, Double> edgeDurationMap = getEdgeTTDurationMap(unicastList);

        List<List<Double>> srtTTLengthCostList = getSRTTTLengthCostListForCandidatePathComputation(kShortestPathsGraphPathList, solution, application, edgeDurationMap);

        double maxCost = Double.MAX_VALUE;
        GraphPath<Node, GCLEdge> selectedGraphPath = null;

        for (int i = 0; i < kShortestPathsGraphPathList.size(); i++) {
            double cost = Math.pow(srtTTLengthCostList.getFirst().get(i), bag.getWSRT()) * Math.pow(srtTTLengthCostList.get(1).get(i), bag.getWTT()) * Math.pow(srtTTLengthCostList.getLast().get(i), bag.getWLength());
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
