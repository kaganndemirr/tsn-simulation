package ktu.kaganndemirr.solver;

import ktu.kaganndemirr.application.Application;
import ktu.kaganndemirr.application.SRTApplication;
import ktu.kaganndemirr.application.TTApplication;
import ktu.kaganndemirr.architecture.EndSystem;
import ktu.kaganndemirr.architecture.GCLEdge;
import ktu.kaganndemirr.architecture.Node;
import ktu.kaganndemirr.message.Unicast;
import ktu.kaganndemirr.message.UnicastCandidate;
import ktu.kaganndemirr.util.GraphMethods;
import ktu.kaganndemirr.util.holders.Bag;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;

import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;

import static ktu.kaganndemirr.util.GraphMethods.copyGraph;
import static ktu.kaganndemirr.util.GraphMethods.discardUnnecessaryEndSystems;

public class PathPenalizationKShortestPaths {
    private final List<Application> applicationList;
    private final List<UnicastCandidate> srtUnicastCandidateList;

    public PathPenalizationKShortestPaths(Bag bag) {
        srtUnicastCandidateList = new ArrayList<>();
        this.applicationList = bag.getApplicationList();

        for (Application application : applicationList) {
            if (application instanceof SRTApplication) {
                for(EndSystem target: application.getTargetList()){
                    Graph<Node, GCLEdge> newGraph = copyGraph(bag.getGraph());
                    Graph<Node, GCLEdge> graphWithoutUnnecessaryEndSystems = discardUnnecessaryEndSystems(newGraph, application.getSource(), target);

                    List<GraphPath<Node, GCLEdge>> dijkstraGraphPathList = new ArrayList<>(bag.getK());

                    for (int i = 0; i < k; i++){
                        DijkstraShortestPath<Node, GCLEdge> allDijkstraShortestPathList = new DijkstraShortestPath<>(graphWithoutUnnecessaryEndSystems);

                        GraphPath<Node, GCLEdge> dijkstraGraphPath = allDijkstraShortestPathList.getPath(application.getSource(), target);

                        if (dijkstraGraphPath == null) {
                            throw new InputMismatchException("Aborting, could not find a path from " + application.getSource() + " to " + target);
                        }

                        else{
                            dijkstraGraphPathList.add(dijkstraGraphPath);

                            GraphMethods.pathPenalization(bag.getGraph(), graphWithoutUnnecessaryEndSystems, dijkstraGraphPath);

                        }

                    }

                    srtUnicastCandidateList.add(new UnicastCandidate(application, target, dijkstraGraphPathList));
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

