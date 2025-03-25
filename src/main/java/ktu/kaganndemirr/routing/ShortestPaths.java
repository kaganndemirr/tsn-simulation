package ktu.kaganndemirr.routing;

import ktu.kaganndemirr.application.Application;
import ktu.kaganndemirr.application.SRTApplication;
import ktu.kaganndemirr.application.TTApplication;
import ktu.kaganndemirr.architecture.GCLEdge;
import ktu.kaganndemirr.architecture.Node;
import ktu.kaganndemirr.message.Unicast;
import ktu.kaganndemirr.util.Bag;
import ktu.kaganndemirr.util.Constants;
import ktu.kaganndemirr.util.PathFindingMethods;
import org.jgrapht.GraphPath;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ShortestPaths {
    private final List<Unicast> ttUnicastList;
    private final List<Unicast> srtUnicastList;

    public ShortestPaths(Bag bag) {
        ttUnicastList = new ArrayList<>();
        srtUnicastList = new ArrayList<>();

        for (Application application : bag.getApplicationList()) {
            if (application instanceof TTApplication){
                for(int i = 0; i < application.getTargetList().size(); i++){
                    if(!application.getExplicitPathList().isEmpty()){
                        ttUnicastList.add(new Unicast(application, application.getTargetList().get(i), application.getExplicitPathList().get(i)));
                    }
                    else{
                        if(Objects.equals(bag.getAlgorithm(), Constants.DIJKSTRA)){
                            GraphPath<Node, GCLEdge> shortestPathGraphPath = PathFindingMethods.dijkstraShortestPath(bag, application, application.getTargetList().get(i));
                            ttUnicastList.add(new Unicast(application, application.getTargetList().get(i), shortestPathGraphPath));
                        }

                    }
                }
            } else if (application instanceof SRTApplication) {
                for(int i = 0; i < application.getTargetList().size(); i++){
                    if(!application.getExplicitPathList().isEmpty()){
                        srtUnicastList.add(new Unicast(application, application.getTargetList().get(i), application.getExplicitPathList().get(i)));
                    }else {
                        if(Objects.equals(bag.getAlgorithm(), Constants.DIJKSTRA)){
                            GraphPath<Node, GCLEdge> shortestPathGraphPath = PathFindingMethods.dijkstraShortestPath(bag, application, application.getTargetList().get(i));
                            srtUnicastList.add(new Unicast(application, application.getTargetList().get(i), shortestPathGraphPath));
                        }

                    }
                }
            }
        }
    }

    public List<Unicast> getTTUnicastList() {
        return ttUnicastList;
    }

    public List<Unicast> getSRTUnicastList() {
        return srtUnicastList;
    }
}


