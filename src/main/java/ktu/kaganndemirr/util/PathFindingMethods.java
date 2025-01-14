package ktu.kaganndemirr.util;

import ktu.kaganndemirr.application.Application;
import ktu.kaganndemirr.architecture.EndSystem;
import ktu.kaganndemirr.architecture.GCLEdge;
import ktu.kaganndemirr.architecture.Node;
import ktu.kaganndemirr.message.Unicast;
import ktu.kaganndemirr.util.mcdm.MCDMConstants;
import ktu.kaganndemirr.util.mcdm.WPMMethods;
import ktu.kaganndemirr.util.mcdm.WSMMethods;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.YenKShortestPath;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Objects;

import static ktu.kaganndemirr.routing.phy.yen.HelperMethods.fillYenKShortestPathGraphPathList;
import static ktu.kaganndemirr.util.GraphMethods.copyGraph;
import static ktu.kaganndemirr.util.GraphMethods.discardUnnecessaryEndSystems;

public class PathFindingMethods {
    public static List<GraphPath<Node, GCLEdge>> YenKShortestPaths(Bag bag, Application application, EndSystem target, int k){
        Graph<Node, GCLEdge> newGraph = copyGraph(bag.getGraph());
        Graph<Node, GCLEdge> graphWithoutUnnecessaryEndSystems = discardUnnecessaryEndSystems(newGraph, application.getSource(), target);

        YenKShortestPath<Node, GCLEdge> allYenKShortestPathList = new YenKShortestPath<>(graphWithoutUnnecessaryEndSystems);

        List<GraphPath<Node, GCLEdge>> yenKShortestPathGraphPathList = new ArrayList<>(k);

        List<GraphPath<Node, GCLEdge>> yenKShortestPathList = allYenKShortestPathList.getPaths(application.getSource(), target, k);

        if (yenKShortestPathList == null) {
            throw new InputMismatchException("Aborting, could not find a path from " + application.getSource() + " to " + target);
        } else {
            yenKShortestPathGraphPathList.addAll(fillYenKShortestPathGraphPathList(yenKShortestPathList, k));
        }

        return yenKShortestPathGraphPathList;
    }

    public static List<GraphPath<Node, GCLEdge>> YenMCDMKShortestPathsV1(Bag bag, int k, Application application, EndSystem target, List<Unicast> unicastList, BufferedWriter costsWriter) throws IOException {
        List<GraphPath<Node, GCLEdge>> seletedGraphPathList = new ArrayList<>();
        for (int i = 0; i < k; i++){
            GraphMethods.randomizeGraph(bag.getGraph(), bag.getLWR());
            Graph<Node, GCLEdge> newGraph = copyGraph(bag.getGraph());
            Graph<Node, GCLEdge> graphWithoutUnnecessaryEndSystems = discardUnnecessaryEndSystems(newGraph, application.getSource(), target);

            YenKShortestPath<Node, GCLEdge> allYenKShortestPathList = new YenKShortestPath<>(graphWithoutUnnecessaryEndSystems);

            List<GraphPath<Node, GCLEdge>> yenKShortestPathGraphPathList = new ArrayList<>(k);

            List<GraphPath<Node, GCLEdge>> yenKShortestPathList = allYenKShortestPathList.getPaths(application.getSource(), target, k);

            if (yenKShortestPathList == null) {
                throw new InputMismatchException("Aborting, could not find a path from " + application.getSource() + " to " + target);
            } else {

                yenKShortestPathGraphPathList.addAll(fillYenKShortestPathGraphPathList(yenKShortestPathList, k));

                GraphPath<Node, GCLEdge> seletedGraphPath = null;
                if(Objects.equals(bag.getMCDMName(), MCDMConstants.WSM)){
                    seletedGraphPath = WSMMethods.srtTTLengthGraphPathV2(bag, application, target, yenKShortestPathGraphPathList, unicastList, seletedGraphPathList, costsWriter, i);
                } else if (Objects.equals(bag.getMCDMName(), MCDMConstants.WPM)) {
                    if(bag.getCWR() != null){
                        seletedGraphPath = WPMMethods.srtTTLengthV2(bag, application, target, yenKShortestPathGraphPathList, unicastList, seletedGraphPathList, costsWriter, i);
                    }
                    else {
                        seletedGraphPath = WPMMethods.srtTTLengthV2(bag, application, target, yenKShortestPathGraphPathList, unicastList, seletedGraphPathList, costsWriter, i);
                    }

                }


                seletedGraphPathList.add(seletedGraphPath);

            }
        }

        return seletedGraphPathList;
    }

    public static List<GraphPath<Node, GCLEdge>> YenMCDMKShortestPathsV2(Bag bag, int k, Application application, EndSystem target, List<Unicast> unicastList, BufferedWriter costsWriter) throws IOException {
        List<GraphPath<Node, GCLEdge>> seletedGraphPathList = new ArrayList<>();
        for (int i = 0; i < k; i++){
            GraphMethods.randomizeGraph(bag.getGraph(), bag.getLWR());
            Graph<Node, GCLEdge> newGraph = copyGraph(bag.getGraph());
            Graph<Node, GCLEdge> graphWithoutUnnecessaryEndSystems = discardUnnecessaryEndSystems(newGraph, application.getSource(), target);

            YenKShortestPath<Node, GCLEdge> allYenKShortestPathList = new YenKShortestPath<>(graphWithoutUnnecessaryEndSystems);

            List<GraphPath<Node, GCLEdge>> yenKShortestPathGraphPathList = new ArrayList<>(k);

            List<GraphPath<Node, GCLEdge>> yenKShortestPathList = allYenKShortestPathList.getPaths(application.getSource(), target, k);

            if (yenKShortestPathList == null) {
                throw new InputMismatchException("Aborting, could not find a path from " + application.getSource() + " to " + target);
            } else {

                yenKShortestPathGraphPathList.addAll(fillYenKShortestPathGraphPathList(yenKShortestPathList, k));

                GraphPath<Node, GCLEdge> seletedGraphPath = null;
                if(Objects.equals(bag.getMCDMName(), MCDMConstants.WSM)){
                    seletedGraphPath = WSMMethods.srtTTLengthGraphPathV2(bag, application, target, yenKShortestPathGraphPathList, unicastList, seletedGraphPathList, costsWriter, i);
                } else if (Objects.equals(bag.getMCDMName(), MCDMConstants.WPM)) {
                    if(bag.getCWR() != null){
                        seletedGraphPath = WPMMethods.srtTTLengthV2(bag, application, target, yenKShortestPathGraphPathList, unicastList, seletedGraphPathList, costsWriter, i);
                    }
                    else {
                        seletedGraphPath = WPMMethods.srtTTLengthV2(bag, application, target, yenKShortestPathGraphPathList, unicastList, seletedGraphPathList, costsWriter, i);
                    }

                }


                seletedGraphPathList.add(seletedGraphPath);

            }
        }

        return seletedGraphPathList;
    }
}
