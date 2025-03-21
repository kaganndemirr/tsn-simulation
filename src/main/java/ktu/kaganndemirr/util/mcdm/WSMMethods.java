package ktu.kaganndemirr.util.mcdm;

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
import java.io.IOException;
import java.util.*;

import static ktu.kaganndemirr.util.mcdm.HelperMethods.createSRTTTLengthCandidatePathHolder;
import static ktu.kaganndemirr.util.mcdm.HelperMethods.getEdgeTTDurationMap;

public class WSMMethods {
    private static final Logger logger = LoggerFactory.getLogger(WSMMethods.class.getSimpleName());

    public static void normalizeCandidatePathHolderCostListMax(List<CandidatePathHolder> candidatePathHolderList) {
        double maxSRTCost = Collections.max(candidatePathHolderList, Comparator.comparingDouble(CandidatePathHolder::getSRTCost)).getSRTCost();
        double maxTTCost = Collections.max(candidatePathHolderList, Comparator.comparingDouble(CandidatePathHolder::getTTCost)).getTTCost();
        double maxLength = Collections.max(candidatePathHolderList, Comparator.comparingDouble(CandidatePathHolder::getLength)).getLength();

        for(CandidatePathHolder candidatePathHolder: candidatePathHolderList){
            if(maxSRTCost == 0){
                candidatePathHolder.setSRTCost(0.0);
            } else {
                double normalizedValue = candidatePathHolder.getSRTCost() / maxSRTCost;
                candidatePathHolder.setSRTCost(normalizedValue);
            }

            if(maxTTCost == 0){
                candidatePathHolder.setTTCost(0.0);
            } else {
                double normalizedValue = candidatePathHolder.getTTCost() / maxTTCost;
                candidatePathHolder.setTTCost(normalizedValue);
            }

            double normalizedValue = candidatePathHolder.getLength() / maxLength;
            candidatePathHolder.setLength(normalizedValue);

        }
    }

    public static void normalizeCandidatePathHolderCostListVector(List<CandidatePathHolder> candidatePathHolderList) {
        double sumOfSRTCostSquares = candidatePathHolderList.stream()
                .mapToDouble(c -> c.getSRTCost() * c.getSRTCost())
                .sum();

        double sqrtSumOfSRTCostSquares = Math.sqrt(sumOfSRTCostSquares);

        double sumOfTTCostSquares = candidatePathHolderList.stream()
                .mapToDouble(c -> c.getTTCost() * c.getTTCost())
                .sum();

        double sqrtSumOfTTCostSquares = Math.sqrt(sumOfTTCostSquares);

        double sumOfLengthSquares = candidatePathHolderList.stream()
                .mapToDouble(c -> c.getLength() * c.getLength())
                .sum(); // Kareleri topla

        double sqrtSumOfLengthSquares = Math.sqrt(sumOfLengthSquares);

        for(CandidatePathHolder candidatePathHolder: candidatePathHolderList){
            if(sqrtSumOfSRTCostSquares == 0){
                candidatePathHolder.setSRTCost(0.0);
            }else {
                double normalizedValue = candidatePathHolder.getSRTCost() / sqrtSumOfSRTCostSquares;
                candidatePathHolder.setSRTCost(normalizedValue);
            }

            if(sqrtSumOfTTCostSquares == 0){
                candidatePathHolder.setTTCost(0.0);
            }else {
                double normalizedValue = candidatePathHolder.getTTCost() / sqrtSumOfTTCostSquares;
                candidatePathHolder.setTTCost(normalizedValue);
            }

            double normalizedValue = candidatePathHolder.getLength() / sqrtSumOfLengthSquares;
            candidatePathHolder.setLength(normalizedValue);
        }
    }

    public static void normalizeCandidatePathHolderCostListMinMax(List<CandidatePathHolder> candidatePathHolderList) {
        double maxSRTCost = Collections.max(candidatePathHolderList, Comparator.comparingDouble(CandidatePathHolder::getSRTCost)).getSRTCost();
        double maxTTCost = Collections.max(candidatePathHolderList, Comparator.comparingDouble(CandidatePathHolder::getTTCost)).getTTCost();
        double maxLength = Collections.max(candidatePathHolderList, Comparator.comparingDouble(CandidatePathHolder::getLength)).getLength();

        double minSRTCost = Collections.min(candidatePathHolderList, Comparator.comparingDouble(CandidatePathHolder::getSRTCost)).getSRTCost();
        double minTTCost = Collections.min(candidatePathHolderList, Comparator.comparingDouble(CandidatePathHolder::getTTCost)).getTTCost();
        double minLength = Collections.min(candidatePathHolderList, Comparator.comparingDouble(CandidatePathHolder::getLength)).getLength();

        for(CandidatePathHolder candidatePathHolder: candidatePathHolderList){
            if(minSRTCost == 0 && minSRTCost == maxSRTCost){
                candidatePathHolder.setSRTCost(0.0);
            } else if (minSRTCost == maxSRTCost) {
                candidatePathHolder.setSRTCost(1.0);
            }else {
                double normalizedValue = (candidatePathHolder.getSRTCost() - minSRTCost) / (maxSRTCost - minSRTCost);
                candidatePathHolder.setSRTCost(normalizedValue);
            }

            if(minTTCost == 0 && minTTCost == maxTTCost){
                candidatePathHolder.setTTCost(0.0);
            } else if (minTTCost == maxTTCost) {
                candidatePathHolder.setTTCost(1.0);
            }else {
                double normalizedValue = (candidatePathHolder.getTTCost() - minTTCost) / (maxTTCost - minTTCost);
                candidatePathHolder.setTTCost(normalizedValue);
            }

            if(minLength == maxLength){
                candidatePathHolder.setLength(1.0);
            }else {
                double normalizedValue = (candidatePathHolder.getLength() - minLength) / (maxLength - minLength);
                candidatePathHolder.setLength(normalizedValue);
            }

        }
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

    public static List<Unicast> getSRTTTLengthCostList(Bag bag, List<UnicastCandidate> sortedSRTUnicastCandidateList, List<Unicast> solution, Map<GCLEdge, Double> edgeDurationMap, BufferedWriter costsWriter) throws IOException {

        for (UnicastCandidate unicastCandidate : sortedSRTUnicastCandidateList) {
            List<CandidatePathHolder> candidatePathHolderList = createSRTTTLengthCandidatePathHolder(unicastCandidate, solution, edgeDurationMap);

            switch (bag.getWSMNormalization()) {
                case MCDMConstants.MIN_MAX -> normalizeCandidatePathHolderCostListMinMax(candidatePathHolderList);
                case MCDMConstants.VECTOR -> normalizeCandidatePathHolderCostListVector(candidatePathHolderList);
                case MCDMConstants.MAX -> normalizeCandidatePathHolderCostListMax(candidatePathHolderList);
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
            for (CandidatePathHolder candidatePathHolder : candidatePathHolderList) {
                double cost = wSRT * candidatePathHolder.getSRTCost() + wTT * candidatePathHolder.getTTCost() + wLength * candidatePathHolder.getLength();
                if(logger.isDebugEnabled()){
                    assert resultList != null;
                    resultList.add(cost);
                }
                if (cost < maxCost) {
                    maxCost = cost;
                    selectedGraphPath = candidatePathHolder.getCandidatePath();
                    if (maxCost == 0) {
                        break;
                    }
                }
            }

            if(logger.isDebugEnabled()){
                List<Double> normalizedSRTCostList  = candidatePathHolderList.stream()
                        .map(CandidatePathHolder::getSRTCost)
                        .toList();

                List<Double> normalizedTTCostList  = candidatePathHolderList.stream()
                        .map(CandidatePathHolder::getTTCost)
                        .toList();

                List<Double> normalizedLengthList  = candidatePathHolderList.stream()
                        .map(CandidatePathHolder::getLength)
                        .toList();

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

}
