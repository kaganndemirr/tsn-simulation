package ktu.kaganndemirr.routing.phy.yen;

import ktu.kaganndemirr.application.Application;
import ktu.kaganndemirr.application.SRTApplication;
import ktu.kaganndemirr.application.TTApplication;
import ktu.kaganndemirr.architecture.GCLEdge;
import ktu.kaganndemirr.architecture.Node;
import ktu.kaganndemirr.message.Unicast;
import ktu.kaganndemirr.message.UnicastCandidate;
import ktu.kaganndemirr.util.Bag;
import ktu.kaganndemirr.util.PathFindingMethods;
import org.jgrapht.GraphPath;

import java.util.ArrayList;
import java.util.List;

public class YenKShortestPaths {
    private final List<UnicastCandidate> ttUnicastCandidateList;
    private final List<UnicastCandidate> srtUnicastCandidateList;

    private final List<Unicast> ttUnicastList;
    private final List<Unicast> srtUnicastList;

    public YenKShortestPaths(Bag bag) {
        ttUnicastCandidateList = new ArrayList<>();
        srtUnicastCandidateList = new ArrayList<>();

        ttUnicastList = new ArrayList<>();
        srtUnicastList = new ArrayList<>();


        for (Application application : bag.getApplicationList()) {
            if (application instanceof TTApplication){
                for(int i = 0; i < application.getTargetList().size(); i++){
                    if(application.getExplicitPathList().isEmpty()){
                        List<GraphPath<Node, GCLEdge>> yenKShortestPathGraphPathList = PathFindingMethods.yenKShortestPaths(bag, application, application.getTargetList().get(i));
                        ttUnicastCandidateList.add(new UnicastCandidate(application, application.getTargetList().get(i), yenKShortestPathGraphPathList));
                    }
                    else {
                        ttUnicastList.add(new Unicast(application, application.getTargetList().get(i), application.getExplicitPathList().get(i)));
                    }
                }
            } else if (application instanceof SRTApplication) {
                for(int i = 0; i < application.getTargetList().size(); i++){
                    if(application.getExplicitPathList().isEmpty()){
                        List<GraphPath<Node, GCLEdge>> yenKShortestPathGraphPathList = PathFindingMethods.yenKShortestPaths(bag, application, application.getTargetList().get(i));
                        srtUnicastCandidateList.add(new UnicastCandidate(application, application.getTargetList().get(i), yenKShortestPathGraphPathList));
                    }
                    else {
                        srtUnicastList.add(new Unicast(application, application.getTargetList().get(i), application.getExplicitPathList().get(i)));
                    }
                }
            }
        }
    }

    public List<UnicastCandidate> getTTUnicastCandidateList() {
        return ttUnicastCandidateList;
    }

    public List<UnicastCandidate> getSRTUnicastCandidateList() {
        return srtUnicastCandidateList;
    }

    public List<Unicast> getTTUnicastList() {
        return ttUnicastList;
    }

    public List<Unicast> getSRTUnicastList() {
        return srtUnicastList;
    }
}
