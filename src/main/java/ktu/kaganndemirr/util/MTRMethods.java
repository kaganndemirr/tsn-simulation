package ktu.kaganndemirr.util;

import ktu.kaganndemirr.architecture.GCL;
import ktu.kaganndemirr.architecture.GCLEdge;
import ktu.kaganndemirr.architecture.Node;
import ktu.kaganndemirr.message.Unicast;
import org.jgrapht.Graph;

import java.util.*;

import static ktu.kaganndemirr.util.GraphMethods.copyGraph;

public class MTRMethods {
    public static List<Graph<Node, GCLEdge>> createVirtualTopologyListForV1(Graph<Node, GCLEdge> graph, List<Unicast> ttUnicastList) {
        //Physical Topology
        Graph<Node, GCLEdge> physicalTopology = copyGraph(graph);

        //Virtual Topology
        Graph<Node, GCLEdge> virtualTopology = copyGraph(graph);

        for (Unicast ttUnicast : ttUnicastList) {
            for(GCLEdge edge: ttUnicast.getPath().getEdgeList()){
                virtualTopology.setEdgeWeight(edge.getSource(), edge.getTarget(), graph.edgeSet().size());
            }
        }

        List<Graph<Node, GCLEdge>> virtualTopologyList = new ArrayList<>();
        virtualTopologyList.add(physicalTopology);
        virtualTopologyList.add(virtualTopology);

        return virtualTopologyList;
    }

    private static Map<GCLEdge, Double> getEdgeDurationMap(List<Unicast> ttUnicastList) {
        Map<GCLEdge, Double> edgeDurationMap = new HashMap<>();
        for (Unicast unicast : ttUnicastList) {
            for (GCLEdge edge : unicast.getPath().getEdgeList()) {
                for (GCL gcl : edge.getGCL()) {
                    if (!edgeDurationMap.containsKey(edge)) {
                        edgeDurationMap.put(edge, gcl.getDuration());
                    } else {
                        edgeDurationMap.put(edge, edgeDurationMap.get(edge) + gcl.getDuration());
                    }
                }

            }
        }
        return edgeDurationMap;
    }

    public static List<Graph<Node, GCLEdge>> createVirtualTopologyListForAverage(Graph<Node, GCLEdge> graph, List<Unicast> ttUnicastList) {


        //Physical Topology
        Graph<Node, GCLEdge> physicalTopology = copyGraph(graph);

        //Virtual Topologies
        Graph<Node, GCLEdge> virtualTopology1 = copyGraph(graph);
        Graph<Node, GCLEdge> virtualTopology2 = copyGraph(graph);

        Map<GCLEdge, Double> edgeDurationMap = getEdgeDurationMap(ttUnicastList);

        List<Double> durationList = new ArrayList<>();
        for (Map.Entry<GCLEdge, Double> entry : edgeDurationMap.entrySet()) {
            durationList.add(entry.getValue());
        }

        double sum = durationList.stream().mapToDouble(Double::doubleValue).sum();
        double average = sum / durationList.size();

        for(Map.Entry<GCLEdge, Double> entry: edgeDurationMap.entrySet()){
            if (edgeDurationMap.get(entry.getKey()) > average) {
                virtualTopology1.setEdgeWeight(entry.getKey().getSource(), entry.getKey().getTarget(), graph.edgeSet().size());
                virtualTopology2.setEdgeWeight(entry.getKey().getSource(), entry.getKey().getTarget(), graph.edgeSet().size());
            } else {
                virtualTopology2.setEdgeWeight(entry.getKey().getSource(), entry.getKey().getTarget(), graph.edgeSet().size());
            }
        }

        List<Graph<Node, GCLEdge>> virtualTopologyList = new ArrayList<>();
        virtualTopologyList.add(physicalTopology);
        virtualTopologyList.add(virtualTopology1);
        virtualTopologyList.add(virtualTopology2);

        return virtualTopologyList;
    }
}
