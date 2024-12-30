package ktu.kaganndemirr.message;

import ktu.kaganndemirr.application.Application;
import ktu.kaganndemirr.architecture.GCLEdge;
import ktu.kaganndemirr.architecture.Node;
import org.jgrapht.GraphPath;

public class Unicast extends Route{
    private final GraphPath<Node, GCLEdge> path;

    public Unicast(Application application, Node target, GraphPath<Node, GCLEdge> path) {
        this.application = application;
        this.target = target;
        this.path = path;
    }

    public GraphPath<Node, GCLEdge> getPath() {
        return path;
    }
}
