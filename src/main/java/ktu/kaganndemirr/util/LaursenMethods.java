package ktu.kaganndemirr.util;

import ktu.kaganndemirr.evaluator.AVBLatencyMathCost;
import ktu.kaganndemirr.evaluator.Cost;
import ktu.kaganndemirr.evaluator.Evaluator;
import ktu.kaganndemirr.message.Unicast;
import ktu.kaganndemirr.message.UnicastCandidate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LaursenMethods {
    public static List<Unicast> constructInitialSolution(List<UnicastCandidate> srtUnicastCandidateList, List<Unicast> ttUnicastList, int k, Evaluator eval) {

        List<Unicast> initialSolution = new ArrayList<>(ttUnicastList);

        List<UnicastCandidate> shuffledAvbList = new ArrayList<>(srtUnicastCandidateList);
        Collections.shuffle(shuffledAvbList);

        //Then within an application, we select the
        for (UnicastCandidate unicastCandidate : shuffledAvbList) {
            Cost currentBestCost = new AVBLatencyMathCost();
            Unicast currentUnicast;
            Unicast currentBestUnicast = null;
            for (int u = 0; u < k; u++) {
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
