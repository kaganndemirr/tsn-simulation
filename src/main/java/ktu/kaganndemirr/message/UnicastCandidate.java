package ktu.kaganndemirr.message;

import ktu.kaganndemirr.application.Application;
import ktu.kaganndemirr.architecture.GCLEdge;
import ktu.kaganndemirr.architecture.Node;
import org.jgrapht.GraphPath;

import java.util.List;

public class UnicastCandidate extends Route{
    private final List<GraphPath<Node, GCLEdge>> candidatePathList;

    public UnicastCandidate(Application application, Node target, List<GraphPath<Node, GCLEdge>> candidatePathList) {
        this.application = application;
        this.target = target;
        this.candidatePathList = candidatePathList;
    }

    public List<GraphPath<Node, GCLEdge>> getCandidatePathList() {
        return candidatePathList;
    }
}
