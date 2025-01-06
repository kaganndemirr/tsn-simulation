package ktu.kaganndemirr.routing.phy.pathpenalization;

import ktu.kaganndemirr.application.Application;
import ktu.kaganndemirr.application.SRTApplication;
import ktu.kaganndemirr.application.TTApplication;
import ktu.kaganndemirr.architecture.EndSystem;
import ktu.kaganndemirr.architecture.GCLEdge;
import ktu.kaganndemirr.architecture.Node;
import ktu.kaganndemirr.message.Unicast;
import ktu.kaganndemirr.message.UnicastCandidate;
import ktu.kaganndemirr.util.GraphMethods;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.alg.shortestpath.YenKShortestPath;

import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;

import static ktu.kaganndemirr.routing.phy.yen.HelperMethods.fillYenKShortestPathGraphPathList;
import static ktu.kaganndemirr.util.GraphMethods.copyGraph;
import static ktu.kaganndemirr.util.GraphMethods.discardUnnecessaryEndSystems;

public class PathPenalizationKShortestPaths {
    private final List<Application> applicationList;
    private final List<UnicastCandidate> srtUnicastCandidateList;

    public PathPenalizationKShortestPaths(final Graph<Node, GCLEdge> graph, final List<Application> applicationList, final int k) {
        srtUnicastCandidateList = new ArrayList<>();
        this.applicationList = applicationList;

        for (Application application : applicationList) {
            if (application instanceof SRTApplication) {
                for(EndSystem target: application.getTargetList()){
                    Graph<Node, GCLEdge> newGraph = copyGraph(graph);
                    Graph<Node, GCLEdge> graphWithoutUnnecessaryEndSystems = discardUnnecessaryEndSystems(newGraph, application.getSource(), target);

                    List<GraphPath<Node, GCLEdge>> shortestPathGraphPathList = new ArrayList<>(k);

                    for (int i = 0; i < k; i++){
                        DijkstraShortestPath<Node, GCLEdge> allShortestPaths = new DijkstraShortestPath<>(graphWithoutUnnecessaryEndSystems);

                        GraphPath<Node, GCLEdge> shortestGraphPath = allShortestPaths.getPath(application.getSource(), target);

                        if (shortestGraphPath == null) {
                            throw new InputMismatchException("Aborting, could not find a path from " + application.getSource() + " to " + target);
                        }

                        else{
                            shortestPathGraphPathList.add(shortestGraphPath);

                            GraphMethods.pathPenalization(graph, graphWithoutUnnecessaryEndSystems, shortestGraphPath);

                        }

                    }

                    srtUnicastCandidateList.add(new UnicastCandidate(application, target, shortestPathGraphPathList));
                }
            }
        }
    }

    public List<UnicastCandidate> getSRTUnicastCandidateList() {
        return srtUnicastCandidateList;
    }

    public List<Unicast> getTTUnicastList() {
        List<Unicast> ttUnicastList = new ArrayList<>();
        for (Application app : applicationList) {
            if (app instanceof TTApplication) {
                for(int i = 0; i < app.getTargetList().size(); i++){
                    ttUnicastList.add(new Unicast(app, app.getTargetList().get(i), app.getExplicitPathList().get(i)));
                }
            }
        }
        return ttUnicastList;
    }
}

