package ktu.kaganndemirr.routing;

import ktu.kaganndemirr.application.Application;
import ktu.kaganndemirr.application.SRTApplication;
import ktu.kaganndemirr.application.TTApplication;
import ktu.kaganndemirr.architecture.GCLEdge;
import ktu.kaganndemirr.architecture.Node;
import ktu.kaganndemirr.message.Unicast;
import ktu.kaganndemirr.message.UnicastCandidate;
import ktu.kaganndemirr.util.Bag;
import ktu.kaganndemirr.util.Constants;
import ktu.kaganndemirr.util.PathFindingMethods;
import org.jgrapht.GraphPath;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class KShortestPaths {
    private final List<UnicastCandidate> ttUnicastCandidateList;
    private final List<UnicastCandidate> srtUnicastCandidateList;

    private final List<Unicast> ttUnicastList;
    private final List<Unicast> srtUnicastList;

    public KShortestPaths(Bag bag) {
        ttUnicastCandidateList = new ArrayList<>();
        srtUnicastCandidateList = new ArrayList<>();

        ttUnicastList = new ArrayList<>();
        srtUnicastList = new ArrayList<>();

        for (Application application : bag.getApplicationList()) {
            if (application instanceof TTApplication){
                for(int i = 0; i < application.getTargetList().size(); i++){
                    if(!application.getExplicitPathList().isEmpty()){
                        ttUnicastList.add(new Unicast(application, application.getTargetList().get(i), application.getExplicitPathList().get(i)));
                    }
                }
            } else if (application instanceof SRTApplication) {
                for(int i = 0; i < application.getTargetList().size(); i++){
                    if(!application.getExplicitPathList().isEmpty()){
                        srtUnicastList.add(new Unicast(application, application.getTargetList().get(i), application.getExplicitPathList().get(i)));
                    }
                }
            }
        }


        for (Application application : bag.getApplicationList()) {
            if (application instanceof TTApplication){
                for(int i = 0; i < application.getTargetList().size(); i++){
                    if(application.getExplicitPathList().isEmpty()){
                        if(Objects.equals(bag.getRouting(), Constants.PHY)){
                            if(Objects.equals(bag.getPathFindingMethod(), Constants.YEN)){
                                if(bag.getLWR() == null){
                                    List<GraphPath<Node, GCLEdge>> yenKShortestPathGraphPathList = PathFindingMethods.yenKShortestPathsPHY(bag, application, application.getTargetList().get(i));
                                    ttUnicastCandidateList.add(new UnicastCandidate(application, application.getTargetList().get(i), yenKShortestPathGraphPathList));
                                }
                                else {
                                    List<GraphPath<Node, GCLEdge>> yenKShortestPathGraphPathListLWR = PathFindingMethods.yenKShortestPathsPHYLWR(bag, application, application.getTargetList().get(i));
                                    ttUnicastCandidateList.add(new UnicastCandidate(application, application.getTargetList().get(i), yenKShortestPathGraphPathListLWR));
                                }
                            } else if (Objects.equals(bag.getPathFindingMethod(), Constants.PATH_PENALIZATION)) {
                                if(bag.getLWR() == null){
                                    List<GraphPath<Node, GCLEdge>> pathPenalizationGraphPathList = PathFindingMethods.pathPenalizationKShortestPathsPHY(bag, application, application.getTargetList().get(i));
                                    ttUnicastCandidateList.add(new UnicastCandidate(application, application.getTargetList().get(i), pathPenalizationGraphPathList));
                                }
                                else {
                                    List<GraphPath<Node, GCLEdge>> pathPenalizationGraphPathListLWR = PathFindingMethods.pathPenalizationKShortestPathsPHYLWR(bag, application, application.getTargetList().get(i));
                                    ttUnicastCandidateList.add(new UnicastCandidate(application, application.getTargetList().get(i), pathPenalizationGraphPathListLWR));
                                }
                            }
                        } else if (Objects.equals(bag.getRouting(), Constants.MTR)) {
                            if(Objects.equals(bag.getPathFindingMethod(), Constants.YEN)){
                                if(bag.getLWR() == null){
                                    //TODO:
                                }
                                else {
                                    //TODO:
                                }
                            } else if (Objects.equals(bag.getPathFindingMethod(), Constants.PATH_PENALIZATION)) {
                                if(bag.getLWR() == null){
                                    //TODO:
                                }
                                else {
                                    //TODO:
                                }
                            }
                        }
                    }
                }
            } else if (application instanceof SRTApplication) {
                for(int i = 0; i < application.getTargetList().size(); i++){
                    if(application.getExplicitPathList().isEmpty()){
                        if(Objects.equals(bag.getRouting(), Constants.PHY)){
                            if(Objects.equals(bag.getPathFindingMethod(), Constants.YEN)){
                                if(bag.getLWR() == null){
                                    List<GraphPath<Node, GCLEdge>> yenKShortestPathGraphPathList = PathFindingMethods.yenKShortestPathsPHY(bag, application, application.getTargetList().get(i));
                                    srtUnicastCandidateList.add(new UnicastCandidate(application, application.getTargetList().get(i), yenKShortestPathGraphPathList));
                                }
                                else {
                                    List<GraphPath<Node, GCLEdge>> yenKShortestPathGraphPathListLWR = PathFindingMethods.yenKShortestPathsPHYLWR(bag, application, application.getTargetList().get(i));
                                    srtUnicastCandidateList.add(new UnicastCandidate(application, application.getTargetList().get(i), yenKShortestPathGraphPathListLWR));
                                }
                            } else if (Objects.equals(bag.getPathFindingMethod(), Constants.PATH_PENALIZATION)) {
                                if(bag.getLWR() == null){
                                    List<GraphPath<Node, GCLEdge>> pathPenalizationGraphPathList = PathFindingMethods.pathPenalizationKShortestPathsPHY(bag, application, application.getTargetList().get(i));
                                    srtUnicastCandidateList.add(new UnicastCandidate(application, application.getTargetList().get(i), pathPenalizationGraphPathList));
                                }
                                else {
                                    List<GraphPath<Node, GCLEdge>> pathPenalizationGraphPathListLWR = PathFindingMethods.pathPenalizationKShortestPathsPHYLWR(bag, application, application.getTargetList().get(i));
                                    srtUnicastCandidateList.add(new UnicastCandidate(application, application.getTargetList().get(i), pathPenalizationGraphPathListLWR));
                                }
                            }
                        } else if (Objects.equals(bag.getRouting(), Constants.MTR)) {
                            if(Objects.equals(bag.getPathFindingMethod(), Constants.YEN)){
                                if(bag.getLWR() == null){
                                    List<GraphPath<Node, GCLEdge>> yenKShortestPathGraphPathList = PathFindingMethods.yenKShortestPathsMTR(bag, application, ttUnicastList, application.getTargetList().get(i));
                                    srtUnicastCandidateList.add(new UnicastCandidate(application, application.getTargetList().get(i), yenKShortestPathGraphPathList));
                                }
                                else {
                                    //TODO:
                                }
                            } else if (Objects.equals(bag.getPathFindingMethod(), Constants.PATH_PENALIZATION)) {
                                if(bag.getLWR() == null){
                                    //TODO:
                                }
                                else {
                                    //TODO:
                                }
                            }
                        }

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

