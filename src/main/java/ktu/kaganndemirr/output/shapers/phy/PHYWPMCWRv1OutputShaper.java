package ktu.kaganndemirr.output.shapers.phy;

import ktu.kaganndemirr.application.SRTApplication;
import ktu.kaganndemirr.application.TTApplication;
import ktu.kaganndemirr.architecture.GCLEdge;
import ktu.kaganndemirr.architecture.Node;
import ktu.kaganndemirr.message.Multicast;
import ktu.kaganndemirr.message.Unicast;
import ktu.kaganndemirr.message.UnicastCandidate;
import ktu.kaganndemirr.util.holders.phy.PHYWPMCWRv1Holder;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

import static ktu.kaganndemirr.util.Constants.*;

public class PHYWPMCWRv1OutputShaper {
    private static final Logger logger = LoggerFactory.getLogger(PHYWPMCWRv1OutputShaper.class.getSimpleName());

    private final String topologyOutputLocation;
    private final String mainOutputLocation;

    private final Map<GCLEdge, Double> utilizationMap;

    public PHYWPMCWRv1OutputShaper(PHYWPMCWRv1Holder phyWPMCWRv1Holder) {
        topologyOutputLocation = Paths.get("outputs", phyWPMCWRv1Holder.getRouting(), phyWPMCWRv1Holder.getPathFindingMethod(), phyWPMCWRv1Holder.getAlgorithm(), String.valueOf(phyWPMCWRv1Holder.getK()), phyWPMCWRv1Holder.getMCDMObjective(), phyWPMCWRv1Holder.getCWR(), phyWPMCWRv1Holder.getWPMVersion(), phyWPMCWRv1Holder.getTopologyName() + "_" + phyWPMCWRv1Holder.getApplicationName()).toString();

        mainOutputLocation = Paths.get("outputs", phyWPMCWRv1Holder.getRouting(), phyWPMCWRv1Holder.getPathFindingMethod(), phyWPMCWRv1Holder.getAlgorithm(), String.valueOf(phyWPMCWRv1Holder.getK()), phyWPMCWRv1Holder.getMCDMObjective(), phyWPMCWRv1Holder.getCWR(), phyWPMCWRv1Holder.getWPMVersion()).toString();

        utilizationMap = new HashMap<>();

    }

    public void writeSolutionToFile(List<Unicast> solution) {
        try {
            BufferedWriter writer = new BufferedWriter(new java.io.FileWriter(Paths.get(topologyOutputLocation, "Routes.txt").toString()));

            for (Unicast unicast : solution) {
                if (unicast.getApplication() instanceof SRTApplication) {
                    writer.write(unicast.getApplication().getName() + ": ");
                    writer.write(unicast.getPath().getEdgeList() + ", ");
                    writer.write("Length(weight non-aware): " + unicast.getPath().getEdgeList().size());
                    writer.newLine();
                }
            }

            writer.write("Average Length (ESs included): " + findAveragePathLengthIncludingES(solution) + ", ");
            writer.write("Average Length (between switches): " + findAveragePathLengthWithoutES(solution));

            writer.write("\n");
            writer.write("\n");

            for (Unicast unicast : solution) {
                if (unicast.getApplication() instanceof TTApplication) {
                    writer.write(unicast.getApplication().getName() + ": ");
                    writer.write(String.valueOf(unicast.getPath().getEdgeList()));
                    writer.newLine();
                }
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        logger.info(createSolutionInfoString(topologyOutputLocation));
    }

    public void writeWCDsToFile(Map<Multicast, Double> wcdMap) {
        try {
            double total = 0;
            BufferedWriter wcdWriter = new BufferedWriter(new java.io.FileWriter(Paths.get(topologyOutputLocation, "WCDs.txt").toString()));
            BufferedWriter mainResultWriter = new BufferedWriter(new java.io.FileWriter(Paths.get(mainOutputLocation, "Results.txt").toString(), true));

            for (Map.Entry<Multicast, Double> entry : wcdMap.entrySet()) {
                total += entry.getValue();
                wcdWriter.write(entry.getKey().getApplication().getName() + "\t" + entry.getKey().getApplication().getDeadline() + "\t" + entry.getValue().toString());
                wcdWriter.newLine();
            }
            double mean = total / wcdMap.size();
            wcdWriter.write("Average WCD: " + mean + "\t");

            // The variance
            double variance = 0;
            for (Map.Entry<Multicast, Double> entry : wcdMap.entrySet()) {
                variance += Math.pow(entry.getValue() - mean, 2);
            }
            variance /= wcdMap.size();
            wcdWriter.write("Variance: " + variance + "\t");

            // Standard Deviation
            double std = Math.sqrt(variance);
            wcdWriter.write("Std: " + std);

            mainResultWriter.write("Average WCD: " + mean + "\t" + "Variance: " + variance + "\t" + "Std: " + std + "\n");

            wcdWriter.close();
            mainResultWriter.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        logger.info(createWCDInfoString(topologyOutputLocation));
        logger.info(createWCDResultString(mainOutputLocation));
    }

    public void writeLinkUtilizationsToFile(List<Unicast> solution, Graph<Node, GCLEdge> graph, int rate) {
        try {
            for (Unicast unicast : solution) {
                for (GCLEdge edge : unicast.getPath().getEdgeList()) {
                    if (!utilizationMap.containsKey(edge)) {
                        utilizationMap.put(edge, unicast.getApplication().getMessageSizeMbps() / rate);
                    } else {
                        utilizationMap.put(edge, utilizationMap.get(edge) + unicast.getApplication().getMessageSizeMbps() / rate);
                    }
                }
            }

            double total = 0;
            for (Map.Entry<GCLEdge, Double> entry : utilizationMap.entrySet()) {
                total += entry.getValue();
            }

            int unusedLinks = graph.edgeSet().size() - utilizationMap.size();

            Map<String, Double> utilizationMapString = new HashMap<>();

            for (GCLEdge edge : graph.edgeSet()) {
                utilizationMapString.put(edge.toString(), utilizationMap.getOrDefault(edge, (double) 0));
            }

            BufferedWriter sortedByNamesWriter = new BufferedWriter(new java.io.FileWriter(Paths.get(topologyOutputLocation, "LinkUtilsSortedByNames.txt").toString()));
            BufferedWriter sortedByUtilsWriter = new BufferedWriter(new java.io.FileWriter(Paths.get(topologyOutputLocation, "LinkUtilsSortedByUtils.txt").toString()));

            Map<String, Double> treeMap = new TreeMap<>(utilizationMapString);
            for (Map.Entry<String, Double> entry : treeMap.entrySet()) {
                sortedByNamesWriter.write(entry.getKey() + "\t" + entry.getValue().toString());
                sortedByNamesWriter.newLine();
            }

            for (GCLEdge edge : graph.edgeSet()) {
                if (!utilizationMap.containsKey(edge)) {
                    utilizationMap.put(edge, 0.0);
                }
            }

            LinkedHashMap<GCLEdge, Double> sortedMapbyUtilization = new LinkedHashMap<>();
            utilizationMap.entrySet()
                    .stream()
                    .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                    .forEachOrdered(x -> sortedMapbyUtilization.put(x.getKey(), x.getValue()));

            for (Map.Entry<GCLEdge, Double> entry : sortedMapbyUtilization.entrySet()) {
                sortedByUtilsWriter.write(entry.getKey().toString() + "\t" + entry.getValue().toString());
                sortedByUtilsWriter.newLine();
            }

            double maxUtilization = 0;
            for (Map.Entry<GCLEdge, Double> entry : sortedMapbyUtilization.entrySet()) {
                maxUtilization = entry.getValue();
                break;
            }

            int maxLoadedLinkCounter = 0;
            for (Map.Entry<GCLEdge, Double> entry : utilizationMap.entrySet()) {
                if (entry.getValue().equals(maxUtilization)) {
                    maxLoadedLinkCounter++;
                }
            }

            double mean = total / graph.edgeSet().size();

            double variance = 0;
            for (Map.Entry<GCLEdge, Double> entry : utilizationMap.entrySet()) {
                variance += Math.pow(entry.getValue() - mean, 2);
            }

            for (int i = 0; i < unusedLinks; i++) {
                variance += Math.pow(0 - mean, 2);
            }

            variance /= graph.edgeSet().size();

            double std = Math.sqrt(variance);

            BufferedWriter mainResultWriter = new BufferedWriter(new java.io.FileWriter(Paths.get(mainOutputLocation, "Results.txt").toString(), true));

            mainResultWriter.write("Unused Links: " + unusedLinks + "/" + graph.edgeSet().size() + ", ");
            mainResultWriter.write("Max Loaded Link Number: " + maxLoadedLinkCounter + ", ");
            mainResultWriter.write("Max Loaded Link Utilization: " + maxUtilization + ", ");
            mainResultWriter.write("Average Link Utilization: " + mean + ", ");
            mainResultWriter.write("Variance: " + variance + ", ");
            mainResultWriter.write("Std: " + std + "\n");

            sortedByNamesWriter.write("Unused Links: " + unusedLinks + "/" + graph.edgeSet().size() + ", ");
            sortedByNamesWriter.write("Max Loaded Link Number: " + maxLoadedLinkCounter + ", ");
            sortedByNamesWriter.write("Max Loaded Link Utilization: " + maxUtilization + ", ");
            sortedByNamesWriter.write("Average Link Utilization: " + mean + ", ");
            sortedByNamesWriter.write("Variance: " + variance + ", ");
            sortedByNamesWriter.write("Std: " + std + "\n");

            sortedByUtilsWriter.write("Unused Links: " + unusedLinks + "/" + graph.edgeSet().size() + ", ");
            sortedByUtilsWriter.write("Max Loaded Link Number: " + maxLoadedLinkCounter + ", ");
            sortedByUtilsWriter.write("Max Loaded Link Utilization: " + maxUtilization + ", ");
            sortedByUtilsWriter.write("Average Link Utilization: " + mean + ", ");
            sortedByUtilsWriter.write("Variance: " + variance + ", ");
            sortedByUtilsWriter.write("Std: " + std + "\n");

            sortedByNamesWriter.close();
            sortedByUtilsWriter.close();
            mainResultWriter.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        logger.info(createLinkUtilizationNameInfoString(topologyOutputLocation));
        logger.info(createLinkUtilizationUtilInfoString(topologyOutputLocation));
        logger.info(createLinkUtilizationResultString(mainOutputLocation));
    }

    public void writeDurationMap(Map<Double, Double> durationMap) {
        try {
            LinkedHashMap<Double, Double> sortedDurationMap = new LinkedHashMap<>();
            durationMap.entrySet()
                    .stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEachOrdered(x -> sortedDurationMap.put(x.getKey(), x.getValue()));

            BufferedWriter writer2 = new BufferedWriter(new java.io.FileWriter(Paths.get(mainOutputLocation, "Results.txt").toString(), true));
            writer2.write("Costs and computation times(sec): " + sortedDurationMap + "\n");
            writer2.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        logger.info(createDurationResultString(mainOutputLocation));
    }

    public void writeSRTCandidateRoutesToFile(List<UnicastCandidate> srtUnicastCandidateList) {
        try {
            BufferedWriter writer = new BufferedWriter(new java.io.FileWriter(Paths.get(topologyOutputLocation, "SRTCandidateRoutes.txt").toString()));
            for (UnicastCandidate unicastCandidate : srtUnicastCandidateList) {
                int candidatePathIndex = 0;
                for (GraphPath<Node, GCLEdge> gp : unicastCandidate.getCandidatePathList()) {
                    writer.write(unicastCandidate.getApplication().getName() + "_" + candidatePathIndex + "\t" + gp.getEdgeList() + "\tLength(weight aware): " + gp.getWeight() + "\tLength(weight non-aware): " + gp.getEdgeList().size());
                    writer.newLine();
                    candidatePathIndex++;
                }
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        logger.info(createSRTCandidateInfoString(topologyOutputLocation));
    }
}

