package ktu.kaganndemirr.util;

import ktu.kaganndemirr.architecture.EndSystem;
import ktu.kaganndemirr.architecture.GCLEdge;
import ktu.kaganndemirr.architecture.Node;
import ktu.kaganndemirr.architecture.Switch;
import org.jgrapht.Graph;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;

import java.util.HashSet;
import java.util.Set;

public class GraphMethods {
    public static Graph<Node, GCLEdge> copyGraph(Graph<Node, GCLEdge> graph) {
        Graph<Node, GCLEdge> newGraph = new SimpleDirectedWeightedGraph<>(GCLEdge.class);

        for (Node node : graph.vertexSet()) {
            if (node instanceof EndSystem) {
                newGraph.addVertex(new EndSystem(node));
            } else {
                newGraph.addVertex(new Switch(node));
            }
        }

        for (GCLEdge edge : graph.edgeSet()) {
            newGraph.addEdge(graph.getEdgeSource(edge), graph.getEdgeTarget(edge), new GCLEdge(edge));
            newGraph.setEdgeWeight(graph.getEdgeSource(edge),  graph.getEdgeTarget(edge), graph.getEdgeWeight(edge));
        }

        return newGraph;
    }

    public static Graph<Node, GCLEdge> discardUnnecessaryEndSystems(Graph<Node, GCLEdge> graph, EndSystem source, EndSystem target) {
        Set<EndSystem> endSystemsToKeep = new HashSet<>();
        endSystemsToKeep.add(source);
        endSystemsToKeep.add(target);

        Set<EndSystem> verticesToRemove = new HashSet<>();
        for (Node node : graph.vertexSet()) {
            if (node instanceof EndSystem endSystem && (!endSystemsToKeep.contains(endSystem))) {
                verticesToRemove.add(endSystem);
            }
        }

        for (EndSystem e : verticesToRemove) {
            graph.removeVertex(e);
        }

        return graph;
    }
}
