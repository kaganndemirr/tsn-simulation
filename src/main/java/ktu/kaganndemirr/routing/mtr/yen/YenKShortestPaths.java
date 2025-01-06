package ktu.kaganndemirr.routing.mtr.yen;

import ktu.kaganndemirr.application.Application;
import ktu.kaganndemirr.application.SRTApplication;
import ktu.kaganndemirr.application.TTApplication;
import ktu.kaganndemirr.architecture.EndSystem;
import ktu.kaganndemirr.architecture.GCLEdge;
import ktu.kaganndemirr.architecture.Node;
import ktu.kaganndemirr.message.Unicast;
import ktu.kaganndemirr.message.UnicastCandidate;
import ktu.kaganndemirr.util.Constants;
import ktu.kaganndemirr.util.GraphMethods;
import ktu.kaganndemirr.util.MTRMethods;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.YenKShortestPath;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class YenKShortestPaths {
    private final List<Application> applicationList;
    private final List<Unicast> ttUnicastList;
    private final List<UnicastCandidate> srtUnicastCandidateList;

    public YenKShortestPaths(Graph<Node, GCLEdge> graph, List<Application> applicationList, String mtrName, int k) {
        srtUnicastCandidateList = new ArrayList<>();
        this.ttUnicastList = new ArrayList<>();
        this.applicationList = applicationList;

        List<Unicast> ttUnicastList = createTTUnicast();

        List<Graph<Node, GCLEdge>> virtualTopologyList = null;
        if (Objects.equals(mtrName, Constants.MTR_V1)){
            virtualTopologyList = MTRMethods.createVirtualTopologyListForV1(graph, ttUnicastList);
        } else if (Objects.equals(mtrName, Constants.MTR_AVERAGE)) {
            virtualTopologyList = MTRMethods.createVirtualTopologyListForAverage(graph, ttUnicastList);
        }

        assert virtualTopologyList != null;
        List<Integer> kValues = HelperMethods.findKForTopologies(k, virtualTopologyList.size());

        for (Application application : applicationList) {
            if (application instanceof SRTApplication) {
                for(EndSystem target: application.getTargetList()){
                    List<GraphPath<Node, GCLEdge>> yenKShortestPathGraphPathList = new ArrayList<>(k);
                    for(int i = 0; i < virtualTopologyList.size(); i++){
                        Graph<Node, GCLEdge> copyTopology = GraphMethods.copyGraph(virtualTopologyList.get(i));
                        Graph<Node, GCLEdge> copyTopologyWithoutUnnecessaryEndSystems = GraphMethods.discardUnnecessaryEndSystems(copyTopology, application.getSource(), target);

                        YenKShortestPath<Node, GCLEdge> allYenKShortestPathList = new YenKShortestPath<>(copyTopologyWithoutUnnecessaryEndSystems);

                        List<GraphPath<Node, GCLEdge>> yenKShortestPathList = allYenKShortestPathList.getPaths(application.getSource(), target, kValues.get(i));

                        yenKShortestPathGraphPathList.addAll(yenKShortestPathList);
                    }

                    srtUnicastCandidateList.add(new UnicastCandidate(application, target, yenKShortestPathGraphPathList));
                }
            }
        }
    }

    private List<Unicast> createTTUnicast() {
        for (Application application : applicationList) {
            if (application instanceof TTApplication) {
                for(int i = 0; i < application.getTargetList().size(); i++){
                    ttUnicastList.add(new Unicast(application, application.getTargetList().get(i), application.getExplicitPathList().get(i)));
                }
            }
        }
        return ttUnicastList;
    }

    public List<UnicastCandidate> getSRTUnicastCandidateList() {
        return srtUnicastCandidateList;
    }

    public List<Unicast> getTTUnicastList() {
        return ttUnicastList;
    }
}
