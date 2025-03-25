package ktu.kaganndemirr.application;

import ktu.kaganndemirr.architecture.EndSystem;
import ktu.kaganndemirr.architecture.GCLEdge;
import ktu.kaganndemirr.architecture.Node;
import org.jgrapht.GraphPath;

import java.util.List;
import java.util.Objects;

public class Application {
    protected String name;
    protected int pcp;
    protected String applicationType;
    protected int frameSizeByte;
    protected int number0fFrames;
    protected int messageSizeByte;
    protected double messageSizeMbps;
    protected double cmi;
    protected int deadline;
    protected EndSystem source;
    protected List<EndSystem> targetList;
    protected List<GraphPath<Node, GCLEdge>> explicitPathList;

    protected Application(String name, int pcp, String applicationType, int frameSizeByte, int number0fFrames, int messageSizeByte, double messageSizeMbps, double cmi, int deadline, EndSystem source, List<EndSystem> targetList, List<GraphPath<Node, GCLEdge>> explicitPathList) {
        this.name = name;
        this.pcp = pcp;
        this.applicationType = applicationType;
        this.frameSizeByte = frameSizeByte;
        this.number0fFrames = number0fFrames;
        this.messageSizeByte = messageSizeByte;
        this.messageSizeMbps = messageSizeMbps;
        this.cmi = cmi;
        this.deadline = deadline;
        this.source = source;
        this.targetList = targetList;
        this.explicitPathList = explicitPathList;
    }

    public String getName() {
        return name;
    }

    public int getPCP() {
        return pcp;
    }

    public String getApplicationType() {
        return applicationType;
    }

    public int getFrameSizeByte() {
        return frameSizeByte;
    }

    public int getNumber0fFrames() {
        return number0fFrames;
    }

    public int getMessageSizeByte() {
        return messageSizeByte;
    }

    public double getMessageSizeMbps() {
        return messageSizeMbps;
    }

    public double getCMI() {
        return cmi;
    }

    public int getDeadline() {
        return deadline;
    }

    public EndSystem getSource() {
        return source;
    }

    public List<EndSystem> getTargetList() {
        return targetList;
    }

    public List<GraphPath<Node, GCLEdge>> getExplicitPathList() {
        return explicitPathList;
    }

    public String toString() {
        return name + " (S = " + number0fFrames + "x" + frameSizeByte + "B / P = " + cmi + "us" + " / D = " + deadline + "us)" + " (" + source + " -> " + targetList + ")";
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        Application application = (Application) object;
        return Objects.equals(name, application.name);

    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
