package ktu.kaganndemirr.util;

import ktu.kaganndemirr.application.Application;
import ktu.kaganndemirr.application.TTApplication;
import ktu.kaganndemirr.architecture.EndSystem;
import ktu.kaganndemirr.architecture.GCLEdge;
import ktu.kaganndemirr.architecture.Node;
import ktu.kaganndemirr.message.Unicast;
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
import static ktu.kaganndemirr.util.HelperMethods.fillKShortestPathGraphPathList;

public class PathFindingMethods {
    public static List<GraphPath<Node, GCLEdge>> yenKShortestPaths(Bag bag, Application application, EndSystem target){
        Graph<Node, GCLEdge> newGraph = copyGraph(bag.getGraph());
        Graph<Node, GCLEdge> graphWithoutUnnecessaryEndSystems = discardUnnecessaryEndSystems(newGraph, application.getSource(), target);

        YenKShortestPath<Node, GCLEdge> allYenKShortestPathList = new YenKShortestPath<>(graphWithoutUnnecessaryEndSystems);

        List<GraphPath<Node, GCLEdge>> yenKShortestPathGraphPathList = new ArrayList<>(bag.getK());

        List<GraphPath<Node, GCLEdge>> yenKShortestPathList = allYenKShortestPathList.getPaths(application.getSource(), target, bag.getK());

        if (yenKShortestPathList == null) {
            throw new InputMismatchException("Aborting, could not find a path from " + application.getSource() + " to " + target);
        } else {
            yenKShortestPathGraphPathList.addAll(fillKShortestPathGraphPathList(yenKShortestPathList, bag.getK()));
        }

        return yenKShortestPathGraphPathList;
    }

    public static List<GraphPath<Node, GCLEdge>> pathPenalizationKShortestPaths(Bag bag, Application application, EndSystem target){
        Graph<Node, GCLEdge> newGraph = copyGraph(bag.getGraph());
        Graph<Node, GCLEdge> graphWithoutUnnecessaryEndSystems = discardUnnecessaryEndSystems(newGraph, application.getSource(), target);

        List<GraphPath<Node, GCLEdge>> pathPenalizationKShortestPathGraphPathList = new ArrayList<>();
        for (int i = 0; i < bag.getK(); i++){
            DijkstraShortestPath<Node, GCLEdge> allPathPenalizationShortestPaths = new DijkstraShortestPath<>(graphWithoutUnnecessaryEndSystems);

            GraphPath<Node, GCLEdge> dijkstraShortestPath = allPathPenalizationShortestPaths.getPath(application.getSource(), target);

            if (dijkstraShortestPath == null) {
                throw new InputMismatchException("Aborting, could not find a path from " + application.getSource() + " to " + target);
            }

            else{
                pathPenalizationKShortestPathGraphPathList.add(dijkstraShortestPath);

                GraphMethods.pathPenalization(bag.getGraph(), graphWithoutUnnecessaryEndSystems, dijkstraShortestPath);

            }

        }

        return fillKShortestPathGraphPathList(pathPenalizationKShortestPathGraphPathList, bag.getK());
    }

    public static List<GraphPath<Node, GCLEdge>> yenKShortestPathsLWR(Bag bag, Application application, EndSystem target){
        Graph<Node, GCLEdge> newGraph = copyGraph(bag.getGraph());
        GraphMethods.randomizeGraph(newGraph, bag.getLWR());
        Graph<Node, GCLEdge> graphWithoutUnnecessaryEndSystems = discardUnnecessaryEndSystems(newGraph, application.getSource(), target);

        YenKShortestPath<Node, GCLEdge> allYenKShortestPathList = new YenKShortestPath<>(graphWithoutUnnecessaryEndSystems);

        List<GraphPath<Node, GCLEdge>> yenKShortestPathGraphPathList = new ArrayList<>(bag.getK());

        List<GraphPath<Node, GCLEdge>> yenKShortestPathList = allYenKShortestPathList.getPaths(application.getSource(), target, bag.getK());

        if (yenKShortestPathList == null) {
            throw new InputMismatchException("Aborting, could not find a path from " + application.getSource() + " to " + target);
        } else {
            yenKShortestPathGraphPathList.addAll(fillYenKShortestPathGraphPathList(yenKShortestPathList, bag.getK()));
        }

        return yenKShortestPathGraphPathList;
    }

    public static List<GraphPath<Node, GCLEdge>> pathPenalizationKShortestPathsLWR(Bag bag, Application application, EndSystem target){
        Graph<Node, GCLEdge> newGraph = copyGraph(bag.getGraph());
        GraphMethods.randomizeGraph(newGraph, bag.getLWR());
        Graph<Node, GCLEdge> graphWithoutUnnecessaryEndSystems = discardUnnecessaryEndSystems(newGraph, application.getSource(), target);

        List<GraphPath<Node, GCLEdge>> pathPenalizationKShortestPathGraphPathList = new ArrayList<>();
        for (int i = 0; i < bag.getK(); i++){
            DijkstraShortestPath<Node, GCLEdge> allPathPenalizationShortestPaths = new DijkstraShortestPath<>(graphWithoutUnnecessaryEndSystems);

            GraphPath<Node, GCLEdge> dijkstraShortestPath = allPathPenalizationShortestPaths.getPath(application.getSource(), target);

            if (dijkstraShortestPath == null) {
                throw new InputMismatchException("Aborting, could not find a path from " + application.getSource() + " to " + target);
            }

            else{
                pathPenalizationKShortestPathGraphPathList.add(dijkstraShortestPath);

                GraphMethods.pathPenalization(bag.getGraph(), graphWithoutUnnecessaryEndSystems, dijkstraShortestPath);

            }

        }

        return fillKShortestPathGraphPathList(pathPenalizationKShortestPathGraphPathList, bag.getK());
    }

    public static List<Unicast> getTTUnicastList(List<Application> applicationList) {
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
