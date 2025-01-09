package ktu.kaganndemirr.routing.phy.pathpenalization;

import ktu.kaganndemirr.application.Application;
import ktu.kaganndemirr.application.SRTApplication;
import ktu.kaganndemirr.application.TTApplication;
import ktu.kaganndemirr.architecture.EndSystem;
import ktu.kaganndemirr.architecture.GCLEdge;
import ktu.kaganndemirr.architecture.Node;
import ktu.kaganndemirr.message.Unicast;
import ktu.kaganndemirr.message.UnicastCandidate;
import ktu.kaganndemirr.util.Bag;
import ktu.kaganndemirr.util.GraphMethods;
import ktu.kaganndemirr.util.mcdm.WSMMethods;
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

public class PathPenalizationMCDMKShortestPaths {
    private final List<Application> applicationList;
    private final List<UnicastCandidate> srtUnicastCandidateList;
    private final List<Unicast> ttUnicastList;

    public PathPenalizationMCDMKShortestPaths(Graph<Node, GCLEdge> graph, List<Application> applicationList, Bag bag, int k) {
        this.applicationList = applicationList;
        srtUnicastCandidateList = new ArrayList<>();
        ttUnicastList = new ArrayList<>();

        createTTUnicast();

        for (Application application : applicationList) {
            if (application instanceof SRTApplication) {
                List<GraphPath<Node, GCLEdge>> mcdmGraphPathList = new ArrayList<>();
                for(EndSystem target: application.getTargetList()){
                    for (int i = 0; i < k; i++){
                        GraphMethods.randomizeGraph(graph, bag.getLWR());

                        Graph<Node, GCLEdge> newGraph = copyGraph(graph);
                        Graph<Node, GCLEdge> graphWithoutUnnecessaryEndSystems = discardUnnecessaryEndSystems(newGraph, application.getSource(), target);

                        List<GraphPath<Node, GCLEdge>> shortestPathGraphPathList = new ArrayList<>(k);

                        for (int j = 0; j < k; j++){
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

                        GraphPath<Node, GCLEdge> selectedGraphPath = WSMMethods.srtTTLengthGraphPathV1(bag, application, target, shortestPathGraphPathList, ttUnicastList, mcdmGraphPathList, null);

                        mcdmGraphPathList.add(selectedGraphPath);
                    }

                    srtUnicastCandidateList.add(new UnicastCandidate(application, target, mcdmGraphPathList));
                }
            }
        }
    }

    private void createTTUnicast() {
        for (Application application : applicationList) {
            if (application instanceof TTApplication) {
                for(int i = 0; i < application.getTargetList().size(); i++){
                    ttUnicastList.add(new Unicast(application, application.getTargetList().get(i), application.getExplicitPathList().get(i)));
                }
            }
        }
    }

    public List<UnicastCandidate> getSRTUnicastCandidateList() {
        return srtUnicastCandidateList;
    }

    public List<Unicast> getTTUnicastList() {
        return ttUnicastList;
    }
}


