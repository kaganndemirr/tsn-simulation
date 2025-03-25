package ktu.kaganndemirr.evaluator;

import ktu.kaganndemirr.application.Application;
import ktu.kaganndemirr.application.SRTApplication;
import ktu.kaganndemirr.application.TTApplication;
import ktu.kaganndemirr.architecture.GCLEdge;
import ktu.kaganndemirr.message.Multicast;
import ktu.kaganndemirr.message.Unicast;
import ktu.kaganndemirr.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class AVBLatencyMathV2 implements Evaluator{
    private static final Logger logger = LoggerFactory.getLogger(AVBLatencyMathV2.class.getSimpleName());

    @Override
    public Cost evaluate(List<Unicast> unicastList) {
        Map<GCLEdge, List<Allocation>> ttAllocMap = new HashMap<>();
        List<Multicast> multicasts = Multicast.generateMulticastList(unicastList);
        AVBLatencyMathCost cost = new AVBLatencyMathCost();

        //region <O3>
        HashMap<Application, HashSet<GCLEdge>> edgeMap = new HashMap<>();
        for (Unicast unicast : unicastList) {
            if (!edgeMap.containsKey(unicast.getApplication())) {
                edgeMap.put(unicast.getApplication(), new HashSet<>());
            }
            edgeMap.get(unicast.getApplication()).addAll(unicast.getPath().getEdgeList());
        }

        HashSet<GCLEdge> disjointEdges = new HashSet<>();
        for (Map.Entry<Application, HashSet<GCLEdge>> entry : edgeMap.entrySet()) {
            if (entry.getKey() instanceof SRTApplication) {
                disjointEdges.addAll(entry.getValue());
            }
        }

        cost.add(AVBLatencyMathCost.O.three, disjointEdges.size());
        //endregion

        //region <O1 and O2>
        for (Multicast multicast : multicasts) {
            if (multicast.getApplication() instanceof TTApplication) {
                for (Unicast unicast : multicast.getUnicastList()) {
                    TTApplication uTT = ((TTApplication) unicast.getApplication());
                    for (GCLEdge edge : unicast.getPath().getEdgeList()) {
                        if(!ttAllocMap.containsKey(edge)){
                            ttAllocMap.put(edge, new ArrayList<>());
                            ttAllocMap.get(edge).add(new Allocation(true, uTT.getCMI(), uTT.getMessageSizeMbps()));
                        }
                        else {
                            for(Allocation allocation: ttAllocMap.get(edge)) {
                                allocation.setAllocationMbps(allocation.getAllocationMbps() + uTT.getMessageSizeMbps());
                            }

                        }
                    }
                }
            }
        }

        Map<GCLEdge,List<Allocation>> allocMap = new HashMap<>(ttAllocMap);

        for (Multicast multicast : multicasts) {
            if (multicast.getApplication() instanceof SRTApplication app) {
                //Run over all the unique edges in that application
                for (GCLEdge edge : edgeMap.get(app)) {
                    //If not already there, put it there
                    if (!allocMap.containsKey(edge)) {
                        allocMap.put(edge, new ArrayList<>());
                        allocMap.get(edge).add(new Allocation(false, app.getCMI(), 0));
                    }

                    double allocMbps = app.getMessageSizeMbps();
                    boolean isFound = false;
                    for(Allocation allocation: allocMap.get(edge)){
                        if(!allocation.getIsTT() && allocation.getCMI() == app.getCMI()){
                            allocation.setAllocationMbps(allocation.getAllocationMbps() + allocMbps);
                            isFound = true;
                            break;
                        }
                    }

                    if(!isFound){
                        allocMap.get(edge).add(new Allocation(false, app.getCMI(), allocMbps));
                    }
                }
            }
        }

        for (Multicast multicast : multicasts) {
            if (multicast.getApplication() instanceof SRTApplication app) {
                double maxLatency = 0;
                for (Unicast u : multicast.getUnicastList()) {
                    double latency = 0;
                    for (GCLEdge edge : u.getPath().getEdgeList()) {

                        double capacity = edge.getIdleSlope();
                        for(Allocation allocation: allocMap.get(edge)){
                            if(allocation.getIsTT()){
                                capacity -= allocation.getAllocationMbps() / edge.getRate();
                                break;
                            }
                        }

                        if (capacity < 0) {
                            capacity = 0.0000001;
                        }

                        latency += calculateMaxLatency(edge, allocMap.get(edge), app, capacity);
                    }
                    //For multicast routing, were only interested in the worst route
                    if (maxLatency < latency) {
                        maxLatency = latency;
                    }
                }
                cost.setWCD(multicast, maxLatency);
                if (maxLatency > app.getDeadline()) {
                    cost.add(AVBLatencyMathCost.O.one, 1);
                }
                cost.add(AVBLatencyMathCost.O.two, maxLatency / app.getDeadline());
            }
        }
        //endregion

        return cost;
    }

    private double calculateMaxLatency(GCLEdge edge, List<Allocation> totalAllocationList, SRTApplication app, double capacity) {
        double tDevice = (double) Constants.DEVICE_DELAY / edge.getRate();

        double tMaxPacketSFDIPG = (double) ((Constants.MAX_BE_FRAME_BYTES + Constants.SFD + Constants.IPG) * Constants.ONE_BYTE_TO_BIT) / edge.getRate();

        double tAllStreams = getTAllStreams(edge, totalAllocationList, app);

        double tStreamPacketSFDIPG = (double) ((app.getFrameSizeByte() + Constants.SFD + Constants.IPG) * Constants.ONE_BYTE_TO_BIT) / edge.getRate();

        double tStreamPacketSFD = (double) ((app.getFrameSizeByte() + Constants.SFD) * Constants.ONE_BYTE_TO_BIT) / edge.getRate();

        double evaluator = tDevice + tMaxPacketSFDIPG + (tAllStreams - tStreamPacketSFDIPG) * (edge.getRate() / (edge.getRate() * capacity)) + tStreamPacketSFD;

        return edge.getMaxTTInterference(evaluator);
    }

    private static double getTAllStreams(GCLEdge edge, List<Allocation> totalAllocationList, SRTApplication app) {
        double ttMessageSizeMbps = 0;
        for(Allocation allocation: totalAllocationList){
            if(allocation.getIsTT()){
                ttMessageSizeMbps = allocation.getAllocationMbps();
                break;
            }
        }

        double tAllStreams = 0;
        for(Allocation allocation: totalAllocationList){
            if(!allocation.getIsTT()){
                tAllStreams += (ttMessageSizeMbps + allocation.getAllocationMbps()) * app.getCMI() / edge.getRate();
            }
        }
        return tAllStreams;
    }

}
