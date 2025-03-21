package ktu.kaganndemirr.application;

import ktu.kaganndemirr.architecture.EndSystem;
import ktu.kaganndemirr.architecture.GCLEdge;
import ktu.kaganndemirr.architecture.Node;
import org.jgrapht.GraphPath;

import java.util.List;

public class SRTApplication extends Application{
    public SRTApplication(String name, int pcp, String applicationType, int frameSizeByte, int number0fFrames, int messageSizeByte, double messageSizeMbps, double cmi, int deadline, EndSystem source, List<EndSystem> targetList,List<GraphPath<Node, GCLEdge>> explicitPathList) {
        super(name, pcp, applicationType, frameSizeByte, number0fFrames, messageSizeByte, messageSizeMbps, cmi, deadline, source, targetList, explicitPathList);
    }
}
