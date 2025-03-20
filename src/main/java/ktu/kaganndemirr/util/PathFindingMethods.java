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
import java.util.Objects;

import static ktu.kaganndemirr.util.GraphMethods.copyGraph;
import static ktu.kaganndemirr.util.GraphMethods.discardUnnecessaryEndSystems;
import static ktu.kaganndemirr.util.HelperMethods.fillKShortestPathGraphPathList;

public class PathFindingMethods {
    public static List<GraphPath<Node, GCLEdge>> yenKShortestPathsPHY(Bag bag, Application application, EndSystem target){
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

    public static List<GraphPath<Node, GCLEdge>> pathPenalizationKShortestPathsPHY(Bag bag, Application application, EndSystem target){
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

    public static List<GraphPath<Node, GCLEdge>> yenKShortestPathsPHYLWR(Bag bag, Application application, EndSystem target){
        Graph<Node, GCLEdge> newGraph = copyGraph(bag.getGraph());
        GraphMethods.randomizeGraph(newGraph, bag.getLWR());
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

    public static List<GraphPath<Node, GCLEdge>> pathPenalizationKShortestPathsPHYLWR(Bag bag, Application application, EndSystem target){
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

    public static List<GraphPath<Node, GCLEdge>> yenKShortestPathsMTR(Bag bag, Application application, List<Unicast> ttUnicastList, EndSystem target){

        List<Graph<Node, GCLEdge>> virtualTopologyList = null;
        if (Objects.equals(bag.getMTRName(), Constants.MTR_V1)){
            virtualTopologyList = MTRMethods.createVirtualTopologyListForV1(bag.getGraph(), ttUnicastList);
        } else if (Objects.equals(bag.getMTRName(), Constants.MTR_AVERAGE)) {
            virtualTopologyList = MTRMethods.createVirtualTopologyListForAverage(bag.getGraph(), ttUnicastList);
        }

        assert virtualTopologyList != null;
        List<Integer> kValues = HelperMethods.findKForTopologies(bag.getK(), virtualTopologyList.size());

        List<GraphPath<Node, GCLEdge>> yenKShortestPathGraphPathList = new ArrayList<>(bag.getK());

        for(int i = 0; i < virtualTopologyList.size(); i++){
            Graph<Node, GCLEdge> newGraph = copyGraph(virtualTopologyList.get(i));
            Graph<Node, GCLEdge> graphWithoutUnnecessaryEndSystems = discardUnnecessaryEndSystems(newGraph, application.getSource(), target);

            YenKShortestPath<Node, GCLEdge> allYenKShortestPathList = new YenKShortestPath<>(graphWithoutUnnecessaryEndSystems);

            List<GraphPath<Node, GCLEdge>> yenKShortestPathList = allYenKShortestPathList.getPaths(application.getSource(), target, kValues.get(i));

            if (yenKShortestPathList == null) {
                throw new InputMismatchException("Aborting, could not find a path from " + application.getSource() + " to " + target);
            } else {
                yenKShortestPathGraphPathList.addAll(fillKShortestPathGraphPathList(yenKShortestPathList, kValues.get(i)));
            }
        }

        return yenKShortestPathGraphPathList;
    }
}
