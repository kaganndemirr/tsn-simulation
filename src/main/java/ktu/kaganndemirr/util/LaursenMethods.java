package ktu.kaganndemirr.util;

import ktu.kaganndemirr.evaluator.AVBLatencyMathCost;
import ktu.kaganndemirr.evaluator.Cost;
import ktu.kaganndemirr.evaluator.Evaluator;
import ktu.kaganndemirr.message.Unicast;
import ktu.kaganndemirr.message.UnicastCandidate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class LaursenMethods {
    public static List<Unicast> constructInitialSolution(List<UnicastCandidate> srtUnicastCandidateList, List<Unicast> unicastList, int k, Evaluator eval) {

        List<Unicast> initialSolution = new ArrayList<>(unicastList);

        List<UnicastCandidate> tempSRTUnicastCandidateList = new ArrayList<>(srtUnicastCandidateList);
        List<UnicastCandidate> randomizedSRTUnicastCandidateList = new ArrayList<>(srtUnicastCandidateList.size());
        while(!tempSRTUnicastCandidateList.isEmpty()){
            int index = ThreadLocalRandom.current().nextInt(tempSRTUnicastCandidateList.size());
            randomizedSRTUnicastCandidateList.add(tempSRTUnicastCandidateList.remove(index));
        }

        //Then within an application, we select the
        for (UnicastCandidate unicastCandidate : randomizedSRTUnicastCandidateList) {
            Cost currentBestCost = new AVBLatencyMathCost();
            Unicast currentUnicast;
            Unicast currentBestUnicast = null;
            for (int u = 0; u < Math.max(3, k/4); u++) {
                currentUnicast = new Unicast(unicastCandidate.getApplication(), unicastCandidate.getTarget(), unicastCandidate.getCandidatePathList().get(u));
                initialSolution.add(currentUnicast);
                Cost cost = eval.evaluate(initialSolution);
                if (cost.getTotalCost() < currentBestCost.getTotalCost()) {
                    currentBestCost = cost;
                    currentBestUnicast = currentUnicast;
                }
                //Remove it again
                initialSolution.remove(currentUnicast);

            }
            initialSolution.add(currentBestUnicast);
        }
        return initialSolution;
    }
}
