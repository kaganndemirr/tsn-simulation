package ktu.kaganndemirr.util.mcdm;

import ktu.kaganndemirr.architecture.GCLEdge;
import ktu.kaganndemirr.architecture.Node;
import org.jgrapht.GraphPath;

public class CandidatePathHolder {
    private GraphPath<Node, GCLEdge> candidatePath;
    private Double srtCost;
    private Double ttCost;
    private Double length;

    public CandidatePathHolder(){
        candidatePath = null;
        srtCost = 0.0;
        ttCost = 0.0;
        length = 0.0;
    }

    public GraphPath<Node, GCLEdge> getCandidatePath() {
        return candidatePath;
    }

    public void setCandidatePath(GraphPath<Node, GCLEdge> candidatePath) {
        this.candidatePath = candidatePath;
    }

    public Double getSRTCost() {
        return srtCost;
    }

    public void setSRTCost(Double srtCost) {
        this.srtCost = srtCost;
    }

    public Double getTTCost() {
        return ttCost;
    }

    public void setTTCost(Double ttCost) {
        this.ttCost = ttCost;
    }

    public Double getLength() {
        return length;
    }

    public void setLength(Double length) {
        this.length = length;
    }
}
