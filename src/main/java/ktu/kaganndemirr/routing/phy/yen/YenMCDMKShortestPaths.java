package ktu.kaganndemirr.routing.phy.yen;

import ktu.kaganndemirr.application.Application;
import ktu.kaganndemirr.application.SRTApplication;
import ktu.kaganndemirr.application.TTApplication;
import ktu.kaganndemirr.architecture.EndSystem;
import ktu.kaganndemirr.architecture.GCLEdge;
import ktu.kaganndemirr.architecture.Node;
import ktu.kaganndemirr.message.Unicast;
import ktu.kaganndemirr.message.UnicastCandidate;
import ktu.kaganndemirr.util.Bag;
import ktu.kaganndemirr.util.PathFindingMethods;
import org.jgrapht.GraphPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;


public class YenMCDMKShortestPaths {
    private static final Logger logger = LoggerFactory.getLogger(YenMCDMKShortestPaths.class.getSimpleName());

    private final List<UnicastCandidate> ttUnicastCandidateList;
    private final List<UnicastCandidate> srtUnicastCandidateList;

    private final List<Unicast> ttUnicastList;
    private final List<Unicast> srtUnicastList;

    public YenMCDMKShortestPaths(Bag bag){
        ttUnicastCandidateList = null;
        srtUnicastCandidateList = null;

        ttUnicastList = new ArrayList<>();
        srtUnicastList = new ArrayList<>();

        for (Application application : bag.getApplicationList()) {
            if(application instanceof TTApplication){
                for(int j = 0; j < application.getTargetList().size(); j++){
                    if(!application.getExplicitPathList().isEmpty()){
                        ttUnicastList.add(new Unicast(application, application.getTargetList().get(j), application.getExplicitPathList().get(j)));
                    }
                }
            } else if (application instanceof SRTApplication) {
                for(int j = 0; j < application.getTargetList().size(); j++){
                    if(!application.getExplicitPathList().isEmpty()){
                        srtUnicastList.add(new Unicast(application, application.getTargetList().get(j), application.getExplicitPathList().get(j)));
                    }

                }

            }
        }

    }

    public YenMCDMKShortestPaths(Bag bag, List<Unicast> unicastList, int k, String scenarioOutputPath, String threadName, int i) throws IOException {
        ttUnicastCandidateList = new ArrayList<>();
        srtUnicastCandidateList = new ArrayList<>();

        ttUnicastList = null;
        srtUnicastList = null;

        BufferedWriter costsWriter = null;
        if(logger.isDebugEnabled()){
            costsWriter = new BufferedWriter(new FileWriter(Paths.get(scenarioOutputPath, "Costs.txt").toString(), true));
            costsWriter.write("############## ThreadName:" + threadName + " Iteration:" + i + " ##############\n");
        }

        for (Application application : bag.getApplicationList()) {
            if(application instanceof TTApplication){
                for(EndSystem target: application.getTargetList()){
                    if(application.getExplicitPathList().isEmpty()){
                        List<GraphPath<Node, GCLEdge>> mcdmGraphPathList = PathFindingMethods.YenMCDMKShortestPathsV2(bag, k, application, target, unicastList, costsWriter);
                        ttUnicastCandidateList.add(new UnicastCandidate(application, target, mcdmGraphPathList));
                    }

                }
            } else if (application instanceof SRTApplication) {
                for(EndSystem target: application.getTargetList()){
                    if(application.getExplicitPathList().isEmpty()){
                        List<GraphPath<Node, GCLEdge>> mcdmGraphPathList = PathFindingMethods.YenMCDMKShortestPathsV2(bag, k, application, target, unicastList, costsWriter);
                        srtUnicastCandidateList.add(new UnicastCandidate(application, target, mcdmGraphPathList));
                    }

                }

            }
        }

        if(logger.isDebugEnabled()){
            assert costsWriter != null;
            costsWriter.close();
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

