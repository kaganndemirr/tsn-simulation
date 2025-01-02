package ktu.kaganndemirr.evaluator;

import ktu.kaganndemirr.application.Application;
import ktu.kaganndemirr.application.SRTApplication;
import ktu.kaganndemirr.application.TTApplication;
import ktu.kaganndemirr.architecture.GCLEdge;
import ktu.kaganndemirr.message.Unicast;
import ktu.kaganndemirr.message.Multicast;
import ktu.kaganndemirr.util.constants.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static ktu.kaganndemirr.util.constants.Constants.MAX_BE_FRAME_BYTES;

public class AVBLatencyMathTSNCF implements Evaluator {

    private static final Logger logger = LoggerFactory.getLogger(AVBLatencyMathTSNCF.class.getSimpleName());

    @Override
    public Cost evaluate(List<Unicast> unicastList) {
        Map<GCLEdge, Double> ttAllocationMap = new HashMap<>();
        List<Multicast> multicastList = Multicast.generateMulticastList(unicastList);
        AVBLatencyMathCost cost = new AVBLatencyMathCost();

        for (Multicast multicast : multicastList) {
            if (multicast.getApplication() instanceof TTApplication) {
                for (Unicast unicast : multicast.getUnicastList()) {
                    for (GCLEdge edge : unicast.getPath().getEdgeList()) {
                        double ttMessageSizeMbps = edge.getTTMessageSizeMbps(unicast.getApplication().getCMI());
                        ttAllocationMap.put(edge, ttMessageSizeMbps);
                    }
                }
            }
        }

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

        Map<GCLEdge, Double> allocationMap = new HashMap<>(ttAllocationMap);

        for (Multicast multicast : multicastList) {
            if (multicast.getApplication() instanceof SRTApplication) {
                for (GCLEdge edge : edgeMap.get(multicast.getApplication())) {
                    if (!allocationMap.containsKey(edge)) {
                        allocationMap.put(edge, 0.0);
                    }

                    double allocMbps = multicast.getApplication().getMessageSizeMbps();
                    double totalAllocMbps = allocationMap.get(edge) + allocMbps;

                    allocationMap.put(edge, totalAllocMbps);
                }
            }
        }

        for (Multicast multicast : multicastList) {
            if (multicast.getApplication() instanceof SRTApplication) {
                double maxLatency = 0;
                for (Unicast u : multicast.getUnicastList()) {
                    double latency = 0;
                    for (GCLEdge edge : u.getPath().getEdgeList()) {
                        double capacity = edge.getIdleSlope() - (edge.getMaxTTInterference(multicast.getApplication().getCMI()) / multicast.getApplication().getCMI() - 1);

                        if (capacity < 0) {
                            capacity = 0.1;
                        }

                        latency += calculateMaxLatency(edge, allocationMap.get(edge), multicast.getApplication(), capacity);
                    }
                    //For multicast routing, were only interested in the worst route
                    if (maxLatency < latency) {
                        maxLatency = latency;
                    }
                }
                cost.setWCD(multicast, maxLatency);
                if (maxLatency > multicast.getApplication().getDeadline()) {
                    cost.add(AVBLatencyMathCost.O.one, 1);
                }
                cost.add(AVBLatencyMathCost.O.two, maxLatency / multicast.getApplication().getDeadline());
            }
        }

        return cost;
    }

    private double calculateMaxLatency(GCLEdge edge, double totalAllocationMbps, Application application, double capacity) {
        double tDevice = (double) Constants.DEVICE_DELAY / edge.getRate();

        double tMaxPacketIPGSFD = (double) ((MAX_BE_FRAME_BYTES + Constants.IPG + Constants.SFD) * Constants.ONE_BYTE_TO_BIT) / edge.getRate();

        double tStreamPacketSFDIPG = (double) ((application.getFrameSizeByte() + Constants.IPG + Constants.SFD) * Constants.ONE_BYTE_TO_BIT) / edge.getRate();

        double tStreamPacketSFD = (double) ((application.getFrameSizeByte() + Constants.SFD) * Constants.ONE_BYTE_TO_BIT) / edge.getRate();

        double tAllStreams = totalAllocationMbps * application.getCMI() / edge.getRate();

        double avbLatencyMathResult = tDevice + (tMaxPacketIPGSFD + tAllStreams - tStreamPacketSFDIPG) * (edge.getRate() / capacity) / 100 + tStreamPacketSFD;

        double maxTTInterference = edge.getMaxTTInterference(avbLatencyMathResult);

        return maxTTInterference;
    }

}

