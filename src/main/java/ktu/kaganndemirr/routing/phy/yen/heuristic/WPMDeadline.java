package ktu.kaganndemirr.routing.phy.yen.heuristic;

import ktu.kaganndemirr.application.Application;
import ktu.kaganndemirr.architecture.GCLEdge;
import ktu.kaganndemirr.architecture.Node;
import ktu.kaganndemirr.evaluator.Cost;
import ktu.kaganndemirr.evaluator.Evaluator;
import ktu.kaganndemirr.message.Unicast;
import ktu.kaganndemirr.message.UnicastCandidate;
import ktu.kaganndemirr.routing.phy.yen.YenKShortestPaths;
import ktu.kaganndemirr.solver.Solution;
import ktu.kaganndemirr.util.Constants;
import org.jgrapht.Graph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class WPMDeadline {
    private static final Logger logger = LoggerFactory.getLogger(WPMDeadline.class.getSimpleName());

    private final int k;

    public WPMDeadline(int k) {
        this.k = k;
    }

    Cost cost;
    List<Unicast> solution;

    private List<UnicastCandidate> srtUnicastCandidateList;
    private List<Unicast> ttUnicastList;

    private Map<Double, Double> durationMap;

    String wpmObjective;

    public Solution solve(Graph<Node, GCLEdge> graph, List<Application> applicationList, String wpmObjective, double wSRT, double wTT, double wLength, double wUtil, int rate,  String wpmType, String wpmValueType, final Evaluator evaluator){
        this.wpmObjective = wpmObjective;
        this.durationMap = new HashMap<>();

        Instant graphPathsStartTime = Instant.now();
        YenKShortestPaths yenKShortestPaths = new YenKShortestPaths(graph, applicationList, k);
        Instant graphPathsEndTime = Instant.now();
        long graphPathsDuration = Duration.between(graphPathsStartTime, graphPathsEndTime).toMillis();

        srtUnicastCandidateList = yenKShortestPaths.getSRTUnicastCandidateList();
        ttUnicastList = yenKShortestPaths.getTTUnicastList();

        Instant solutionStartTime = Instant.now();
        if (Objects.equals(wpmObjective, Constants.SRT_TT)){
            solution = null;
        } else if (Objects.equals(wpmObjective, Constants.SRT_TT_LENGTH)) {
            solution = MCDMSpecificMethods.WPMLWRDAVBTTLength(avbUnicastCandidates, ttUnicasts, wAVB, wTT, wLength, wpmType, wpmValueType);
        } else if (Objects.equals(wpmObjective, Constants.SRT_TT_LENGTH_UTIL)) {
            solution = null;
        }


    }
}
