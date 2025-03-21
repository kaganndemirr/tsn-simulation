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
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static ktu.kaganndemirr.util.mcdm.HelperMethods.*;

public class WPMMethods {
    private static final Logger logger = LoggerFactory.getLogger(WPMMethods.class.getSimpleName());

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

            if(Objects.equals(bag.getWPMVersion(), MCDMConstants.WPM_VERSION_V1)) {
                List<Double> srtCostList = null;
                List<Double> ttCostList = null;
                List<Double> lengthList = null;
                List<Double> costList = null;
                if(logger.isDebugEnabled()){
                    srtCostList = new ArrayList<>();
                    ttCostList = new ArrayList<>();
                    lengthList = new ArrayList<>();
                    costList = new ArrayList<>();
                    costsWriter.write(unicastCandidate.getApplication().getName() + "\n");
                }
                for (CandidatePathHolder candidatePathHolder : candidatePathHolderList) {
                    double cost = Math.pow(candidatePathHolder.getSRTCost(), wSRT) * Math.pow(candidatePathHolder.getTTCost(), wTT) * Math.pow(candidatePathHolder.getLength(), wLength);
                    if(logger.isDebugEnabled()){
                        assert srtCostList != null;
                        srtCostList.add(candidatePathHolder.getSRTCost());
                        ttCostList.add(candidatePathHolder.getTTCost());
                        lengthList.add(candidatePathHolder.getLength());
                        costList.add(cost);
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
                    costsWriter.write(srtCostList + "\n");
                    costsWriter.write(ttCostList + "\n");
                    costsWriter.write(lengthList + "\n");
                    costsWriter.write(costList + "\n");
                    costsWriter.newLine();
                }
            }

            else if (Objects.equals(bag.getWPMVersion(), MCDMConstants.WPM_VERSION_V2)) {
                List<Double> srtCostList = null;
                List<Double> ttCostList = null;
                List<Double> lengthList = null;
                List<Integer> winnerNumberList = null;
                if(logger.isDebugEnabled()){
                    srtCostList = new ArrayList<>();
                    ttCostList = new ArrayList<>();
                    lengthList = new ArrayList<>();
                    winnerNumberList = new ArrayList<>();
                    costsWriter.write(unicastCandidate.getApplication().getName() + "\n");
                }

                Map<CandidatePathHolder, Integer> candidatePathScoreMap = new HashMap<>();

                for(CandidatePathHolder candidatePathHolder: candidatePathHolderList){
                    int winnerNumber = getSRTTTLengthWPMV2Cost(bag, candidatePathHolder, candidatePathHolderList);
                    candidatePathScoreMap.put(candidatePathHolder, winnerNumber);
                }

                if(logger.isDebugEnabled()){
                    for(Map.Entry<CandidatePathHolder, Integer> entry: candidatePathScoreMap.entrySet()){
                        assert srtCostList != null;
                        srtCostList.add(entry.getKey().getSRTCost());
                        ttCostList.add(entry.getKey().getTTCost());
                        lengthList.add(entry.getKey().getLength());
                        winnerNumberList.add(entry.getValue());
                    }

                    if(logger.isDebugEnabled()){
                        costsWriter.write(srtCostList + "\n");
                        costsWriter.write(ttCostList + "\n");
                        costsWriter.write(lengthList + "\n");
                        costsWriter.write(winnerNumberList + "\n");
                        costsWriter.newLine();
                    }
                }

                Map<CandidatePathHolder, Integer> sortedcandidatePathScoreMap = candidatePathScoreMap.entrySet()
                        .stream()
                        .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                Map.Entry::getValue,
                                (i1, i2) -> i1,
                                LinkedHashMap::new
                        ));

                selectedGraphPath = sortedcandidatePathScoreMap.entrySet().stream().findFirst().get().getKey().getCandidatePath();
            }

            solution.add(new Unicast(unicastCandidate.getApplication(), unicastCandidate.getTarget(), selectedGraphPath));
        }

        return solution;
    }

    private static int getSRTTTLengthWPMV2Cost(Bag bag, CandidatePathHolder candidatePathHolder, List<CandidatePathHolder> candidatePathHolderList) {
        int winnerNumber = 0;
        if(candidatePathHolder.getSRTCost() == 0 || candidatePathHolder.getTTCost() == 0){
            return candidatePathHolderList.size() - 1;
        }
        else {
            double cost = 0;
            for(CandidatePathHolder candidatePathHolderForLoop: candidatePathHolderList){
                if(candidatePathHolder != candidatePathHolderForLoop){
                    if(candidatePathHolderForLoop.getSRTCost() == 0 || candidatePathHolderForLoop.getTTCost() == 0) {
                        cost = MCDMConstants.LOST_COST;
                    }
                    else {
                        if(Objects.equals(bag.getWPMValueType(), MCDMConstants.ACTUAL)){
                            cost = Math.pow((candidatePathHolder.getSRTCost() / candidatePathHolderForLoop.getSRTCost()), bag.getWSRT()) * Math.pow((candidatePathHolder.getTTCost() / candidatePathHolderForLoop.getTTCost()), bag.getWTT()) * Math.pow((candidatePathHolder.getLength() / candidatePathHolderForLoop.getLength()), bag.getWLength());
                        }else if (Objects.equals(bag.getWPMValueType(), MCDMConstants.RELATIVE)){
                            double relativeSRTCostCandidatePathHolder = candidatePathHolder.getSRTCost() / (candidatePathHolder.getSRTCost() + candidatePathHolder.getTTCost() + candidatePathHolder.getLength());
                            double relativeTTCostCandidatePathHolder =  candidatePathHolder.getTTCost() / (candidatePathHolder.getSRTCost() + candidatePathHolder.getTTCost() + candidatePathHolder.getLength());
                            double relativeLengthCostCandidatePathHolder =  candidatePathHolder.getLength() / (candidatePathHolder.getSRTCost() + candidatePathHolder.getTTCost() + candidatePathHolder.getLength());

                            double relativeSRTCostCandidatePathHolderForLoop = candidatePathHolderForLoop.getSRTCost() / (candidatePathHolderForLoop.getSRTCost() + candidatePathHolderForLoop.getTTCost() + candidatePathHolderForLoop.getLength());
                            double relativeTTCostCandidatePathHolderForLoop =  candidatePathHolderForLoop.getTTCost() / (candidatePathHolderForLoop.getSRTCost() + candidatePathHolderForLoop.getTTCost() + candidatePathHolderForLoop.getLength());
                            double relativeLengthCCandidatePathHolderForLoop =  candidatePathHolderForLoop.getLength() / (candidatePathHolderForLoop.getSRTCost() + candidatePathHolderForLoop.getTTCost() + candidatePathHolderForLoop.getLength());

                            cost = Math.pow((relativeSRTCostCandidatePathHolder / relativeSRTCostCandidatePathHolderForLoop), bag.getWSRT()) * Math.pow((relativeTTCostCandidatePathHolder / relativeTTCostCandidatePathHolderForLoop), bag.getWTT()) * Math.pow((relativeLengthCostCandidatePathHolder / relativeLengthCCandidatePathHolderForLoop), bag.getWLength());
                        }
                    }
                    if(cost < MCDMConstants.WPM_THRESHOLD){
                        winnerNumber++;
                    }
                }
            }
        }
        return winnerNumber;
    }

}
