package ktu.kaganndemirr.routing.phy.yen;

import ktu.kaganndemirr.application.Application;
import ktu.kaganndemirr.application.SRTApplication;
import ktu.kaganndemirr.application.TTApplication;
import ktu.kaganndemirr.architecture.EndSystem;
import ktu.kaganndemirr.architecture.GCL;
import ktu.kaganndemirr.architecture.GCLEdge;
import ktu.kaganndemirr.architecture.Node;
import ktu.kaganndemirr.message.Unicast;
import ktu.kaganndemirr.message.UnicastCandidate;
import ktu.kaganndemirr.util.Bag;
import ktu.kaganndemirr.util.GraphMethods;
import ktu.kaganndemirr.util.mcdm.WSMMethods;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.YenKShortestPath;

import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;

import static ktu.kaganndemirr.routing.phy.yen.HelperMethods.fillYenKShortestPathGraphPathList;
import static ktu.kaganndemirr.util.GraphMethods.copyGraph;
import static ktu.kaganndemirr.util.GraphMethods.discardUnnecessaryEndSystems;

public class YenMCDMKShortestPaths {
    private final List<UnicastCandidate> srtUnicastCandidateList;

    public YenMCDMKShortestPaths(Graph<Node, GCLEdge> graph, List<Application> applicationList, List<Unicast> ttUnicastList, Bag bag, int k) {
        srtUnicastCandidateList = new ArrayList<>();

        for (Application application : applicationList) {
            if (application instanceof SRTApplication) {
                for(EndSystem target: application.getTargetList()){
                    List<GraphPath<Node, GCLEdge>> mcdmGraphPathList = new ArrayList<>();
                    for (int i = 0; i < k; i++){
                        GraphMethods.randomizeGraph(graph, bag.getLWR());
                        Graph<Node, GCLEdge> newGraph = copyGraph(graph);
                        Graph<Node, GCLEdge> graphWithoutUnnecessaryEndSystems = discardUnnecessaryEndSystems(newGraph, application.getSource(), target);

                        YenKShortestPath<Node, GCLEdge> allYenKShortestPathList = new YenKShortestPath<>(graphWithoutUnnecessaryEndSystems);

                        List<GraphPath<Node, GCLEdge>> yenKShortestPathGraphPathList = new ArrayList<>(k);

                        List<GraphPath<Node, GCLEdge>> yenKShortestPathList = allYenKShortestPathList.getPaths(application.getSource(), target, k);

                        if (yenKShortestPathList == null) {
                            throw new InputMismatchException("Aborting, could not find a path from " + application.getSource() + " to " + target);
                        } else {

                            yenKShortestPathGraphPathList.addAll(fillYenKShortestPathGraphPathList(yenKShortestPathList, k));

                            GraphPath<Node, GCLEdge> seletedGraphPath = WSMMethods.srtTTLengthGraphPathV1(application, yenKShortestPathGraphPathList, ttUnicastList, bag);

                            mcdmGraphPathList.add(seletedGraphPath);
                        }
                    }
                    srtUnicastCandidateList.add(new UnicastCandidate(application, target, mcdmGraphPathList));
                }

            }
        }
    }

    public List<UnicastCandidate> getSRTUnicastCandidateList() {
        return srtUnicastCandidateList;
    }

    public static List<Unicast> getTTUnicastList(List<Application> applicationList) {
        List<Unicast> ttUnicastList = new ArrayList<>();
        for (Application application : applicationList) {
            if (application instanceof TTApplication) {
                for(int i = 0; i < application.getTargetList().size(); i++){
                    ttUnicastList.add(new Unicast(application, application.getTargetList().get(i), application.getExplicitPathList().get(i)));
                }
            }
        }
        return ttUnicastList;
    }
}

