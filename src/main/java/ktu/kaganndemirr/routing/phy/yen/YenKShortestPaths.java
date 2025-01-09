package ktu.kaganndemirr.routing.phy.yen;

import ktu.kaganndemirr.application.Application;
import ktu.kaganndemirr.application.SRTApplication;
import ktu.kaganndemirr.application.TTApplication;
import ktu.kaganndemirr.architecture.EndSystem;
import ktu.kaganndemirr.architecture.GCLEdge;
import ktu.kaganndemirr.architecture.Node;
import ktu.kaganndemirr.message.Unicast;
import ktu.kaganndemirr.message.UnicastCandidate;
import ktu.kaganndemirr.util.Bag;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.YenKShortestPath;

import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;

import static ktu.kaganndemirr.routing.phy.yen.HelperMethods.fillYenKShortestPathGraphPathList;
import static ktu.kaganndemirr.util.GraphMethods.copyGraph;
import static ktu.kaganndemirr.util.GraphMethods.discardUnnecessaryEndSystems;

public class YenKShortestPaths {
    private final List<Application> applicationList;
    private final List<UnicastCandidate> srtUnicastCandidateList;

    public YenKShortestPaths(Bag bag, int k) {
        srtUnicastCandidateList = new ArrayList<>();
        this.applicationList = bag.getApplicationList();

        for (Application app : applicationList) {
            if (app instanceof SRTApplication) {
                for(EndSystem target: app.getTargetList()){
                    Graph<Node, GCLEdge> newGraph = copyGraph(bag.getGraph());
                    Graph<Node, GCLEdge> graphWithoutUnnecessaryEndSystems = discardUnnecessaryEndSystems(newGraph, app.getSource(), target);

                    YenKShortestPath<Node, GCLEdge> allYenKShortestPathList = new YenKShortestPath<>(graphWithoutUnnecessaryEndSystems);

                    List<GraphPath<Node, GCLEdge>> yenKShortestPathGraphPathList = new ArrayList<>(k);

                    List<GraphPath<Node, GCLEdge>> yenKShortestPathList = allYenKShortestPathList.getPaths(app.getSource(), target, k);


                    if (yenKShortestPathList == null) {
                        throw new InputMismatchException("Aborting, could not find a path from " + app.getSource() + " to " + target);
                    } else {

                        yenKShortestPathGraphPathList.addAll(fillYenKShortestPathGraphPathList(yenKShortestPathList, k));

                        srtUnicastCandidateList.add(new UnicastCandidate(app, target, yenKShortestPathGraphPathList));
                    }
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
