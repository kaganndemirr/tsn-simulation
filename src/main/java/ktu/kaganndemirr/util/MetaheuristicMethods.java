package ktu.kaganndemirr.util;

import ktu.kaganndemirr.architecture.GCLEdge;
import ktu.kaganndemirr.architecture.Node;
import ktu.kaganndemirr.evaluator.Cost;
import ktu.kaganndemirr.evaluator.Evaluator;
import ktu.kaganndemirr.message.Route;
import ktu.kaganndemirr.message.Unicast;
import ktu.kaganndemirr.message.UnicastCandidate;
import org.jgrapht.GraphPath;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class MetaheuristicMethods {
    public static List<Unicast> GRASP(List<Unicast> initialSolution, Evaluator evaluator, List<UnicastCandidate> srtUnicastCandidateList, Cost globalBestCost) {
        List<Unicast> solution = new ArrayList<>(initialSolution);
        Cost cost = evaluator.evaluate(solution);
        Cost bestCost = cost;

        Map<Route, Route> mapping = new HashMap<>(solution.size());
        for (UnicastCandidate unicastCandidate : srtUnicastCandidateList) {
            mapping.put(unicastCandidate, unicastCandidate);
        }
        for (int sample = 0; sample < solution.size() / 2; sample++) {
            int index = ThreadLocalRandom.current().nextInt(solution.size());

            Unicast oldUnicast = solution.get(index);
            Route oldUnicastCandidate = mapping.get(oldUnicast);
            if (oldUnicastCandidate instanceof UnicastCandidate unicastCandidate) {
                for (int j = 0; j < unicastCandidate.getCandidatePathList().size(); j++) {
                    Unicast tempUnicast = new Unicast(oldUnicastCandidate.getApplication(), oldUnicastCandidate.getTarget(), unicastCandidate.getCandidatePathList().get(j));
                    solution.set(index, tempUnicast);
                    cost = evaluator.evaluate(solution);
                    if (cost.getTotalCost() < bestCost.getTotalCost()) {
                        bestCost = cost;
                        sample--;
                        if (cost.getTotalCost() < globalBestCost.getTotalCost()) {
                            sample -= solution.size() / 2;
                        }
                        break;
                    } else {
                        solution.set(index, oldUnicast);
                    }
                }

            }
        }
        return solution;
    }

    public static List<Unicast> ALO(List<Unicast> antSolution, List<Unicast> antLionSolution, List<UnicastCandidate> srtUnicastCandidateList, int k, Evaluator evaluator) {
        List<Unicast> eliteSolution = new ArrayList<>(antLionSolution);
        Cost eliteSolutionCost = evaluator.evaluate(antLionSolution);

        for (UnicastCandidate unicastCandidate : srtUnicastCandidateList) {
            int randomAntLionIndex = ThreadLocalRandom.current().nextInt(unicastCandidate.getCandidatePathList().size());
            int antIndex = createRandomIndexWithExcludedNumber(k, randomAntLionIndex);
            GraphPath<Node, GCLEdge> antPath = unicastCandidate.getCandidatePathList().get(antIndex);

            for (Unicast ant: antSolution){
                if(Objects.equals(unicastCandidate.getApplication().getName(), ant.getApplication().getName())){
                    ant.setPath(antPath);
                }
            }
        }

        if (evaluator.evaluate(antSolution).getTotalCost() < evaluator.evaluate(antLionSolution).getTotalCost()) {
            antLionSolution = antSolution;
        }

        if (evaluator.evaluate(antLionSolution).getTotalCost() < eliteSolutionCost.getTotalCost()) {
            eliteSolution = antLionSolution;
        }

        return eliteSolution;
    }

    private static int createRandomIndexWithExcludedNumber(int k, int excludedNumber) {
        int index;
        do {
            index = ThreadLocalRandom.current().nextInt(k);
        } while (index == excludedNumber);

        return index;
    }
}

