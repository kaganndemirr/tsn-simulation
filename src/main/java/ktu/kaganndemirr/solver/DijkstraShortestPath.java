package ktu.kaganndemirr.solver;

import ktu.kaganndemirr.application.Application;
import ktu.kaganndemirr.application.SRTApplication;
import ktu.kaganndemirr.application.TTApplication;
import ktu.kaganndemirr.architecture.EndSystem;
import ktu.kaganndemirr.architecture.GCLEdge;
import ktu.kaganndemirr.architecture.Node;
import ktu.kaganndemirr.message.Unicast;
import ktu.kaganndemirr.util.holders.Bag;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;

import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;

import static ktu.kaganndemirr.util.GraphMethods.copyGraph;
import static ktu.kaganndemirr.util.GraphMethods.discardUnnecessaryEndSystems;

public class DijkstraShortestPath {
    private final List<Application> applicationList;
    private final List<Unicast> srtUnicastList;

    public DijkstraShortestPath(Bag bag) {
        srtUnicastList = new ArrayList<>();
        this.applicationList = bag.getApplicationList();

        for (Application app : applicationList) {
            if (app instanceof SRTApplication) {
                for(EndSystem target: app.getTargetList()){
                    Graph<Node, GCLEdge> newGraph = copyGraph(bag.getGraph());
                    Graph<Node, GCLEdge> graphWithoutUnnecessaryEndSystems = discardUnnecessaryEndSystems(newGraph, app.getSource(), target);

                    org.jgrapht.alg.shortestpath.DijkstraShortestPath<Node, GCLEdge> allDijkstraShortestPathList = new org.jgrapht.alg.shortestpath.DijkstraShortestPath<>(graphWithoutUnnecessaryEndSystems);

                    GraphPath<Node, GCLEdge> dijkstraGraphPath = allDijkstraShortestPathList.getPath(app.getSource(), target);

                    if (dijkstraGraphPath == null) {
                        throw new InputMismatchException("Aborting, could not find a path from " + app.getSource() + " to " + target);
                    } else {
                        srtUnicastList.add(new Unicast(app, target, dijkstraGraphPath));
                    }
                }
            }
        }
    }

    public List<Unicast> getSRTUnicastList() {
        return srtUnicastList;
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

