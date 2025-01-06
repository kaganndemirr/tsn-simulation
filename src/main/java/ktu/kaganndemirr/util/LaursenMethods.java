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
    public static List<Unicast> constructInitialSolution(List<UnicastCandidate> avbList, List<Unicast> ttRoutes, int k, Evaluator eval) {

        List<Unicast> initialSolution = new ArrayList<>(ttRoutes);

        List<UnicastCandidate> shuffledAvbList = new ArrayList<>(avbList);
        Collections.shuffle(shuffledAvbList);

        //Then within an application, we select the
        for (UnicastCandidate uc : shuffledAvbList) {
            Cost currBestCost = new AVBLatencyMathCost();
            Unicast currUnicast;
            Unicast currBestUnicast = null;
            for (int u = 0; u < k; u++) {
                currUnicast = new Unicast(uc.getApplication(), uc.getTarget(), uc.getCandidatePathList().get(u));
                //Add solution and evaluate
                initialSolution.add(currUnicast);
                Cost cost = eval.evaluate(initialSolution);
                if (cost.getTotalCost() < currBestCost.getTotalCost()) {
                    currBestCost = cost;
                    currBestUnicast = currUnicast;
                }
                //Remove it again
                initialSolution.remove(currUnicast);

            }
            initialSolution.add(currBestUnicast);
        }
        return initialSolution;
    }
}
