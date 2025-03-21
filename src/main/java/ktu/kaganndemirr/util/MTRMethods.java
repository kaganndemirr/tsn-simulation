package ktu.kaganndemirr.util;

import ktu.kaganndemirr.architecture.GCL;
import ktu.kaganndemirr.architecture.GCLEdge;
import ktu.kaganndemirr.architecture.Node;
import ktu.kaganndemirr.message.Unicast;
import org.jgrapht.Graph;
import weka.clusterers.HierarchicalClusterer;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

import java.util.*;

import static ktu.kaganndemirr.util.GraphMethods.copyGraph;

public class MTRMethods {
    public static List<Graph<Node, GCLEdge>> createVirtualTopologyListForV1(Bag bag, List<Unicast> ttUnicastList) {
        //Physical Topology
        Graph<Node, GCLEdge> physicalTopology = copyGraph(bag.getGraph());

        //Virtual Topology
        Graph<Node, GCLEdge> virtualTopology = copyGraph(bag.getGraph());

        for (Unicast ttUnicast : ttUnicastList) {
            for(GCLEdge edge: ttUnicast.getPath().getEdgeList()){
                virtualTopology.setEdgeWeight(edge.getSource(), edge.getTarget(), bag.getGraph().edgeSet().size());
            }
        }

        List<Graph<Node, GCLEdge>> virtualTopologyList = new ArrayList<>();
        virtualTopologyList.add(physicalTopology);
        virtualTopologyList.add(virtualTopology);

        return virtualTopologyList;
    }

    private static Map<GCLEdge, Double> getEdgeDurationMap(List<Unicast> ttUnicastList) {
        Map<GCLEdge, Double> edgeDurationMap = new HashMap<>();
        for (Unicast unicast : ttUnicastList) {
            for (GCLEdge edge : unicast.getPath().getEdgeList()) {
                for (GCL gcl : edge.getGCL()) {
                    if (!edgeDurationMap.containsKey(edge)) {
                        edgeDurationMap.put(edge, gcl.getDuration());
                    } else {
                        edgeDurationMap.put(edge, edgeDurationMap.get(edge) + gcl.getDuration());
                    }
                }

            }
        }
        return edgeDurationMap;
    }

    public static List<Graph<Node, GCLEdge>> createVirtualTopologyListForAverage(Bag bag, List<Unicast> ttUnicastList) {


        //Physical Topology
        Graph<Node, GCLEdge> physicalTopology = copyGraph(bag.getGraph());

        //Virtual Topologies
        Graph<Node, GCLEdge> virtualTopology1 = copyGraph(bag.getGraph());
        Graph<Node, GCLEdge> virtualTopology2 = copyGraph(bag.getGraph());

        Map<GCLEdge, Double> edgeDurationMap = getEdgeDurationMap(ttUnicastList);

        List<Double> durationList = new ArrayList<>();
        for (Map.Entry<GCLEdge, Double> entry : edgeDurationMap.entrySet()) {
            durationList.add(entry.getValue());
        }

        double sum = durationList.stream().mapToDouble(Double::doubleValue).sum();
        double average = sum / durationList.size();

        for(Map.Entry<GCLEdge, Double> entry: edgeDurationMap.entrySet()){
            if (edgeDurationMap.get(entry.getKey()) > average) {
                virtualTopology1.setEdgeWeight(entry.getKey().getSource(), entry.getKey().getTarget(), bag.getGraph().edgeSet().size());
                virtualTopology2.setEdgeWeight(entry.getKey().getSource(), entry.getKey().getTarget(), bag.getGraph().edgeSet().size());
            } else {
                virtualTopology2.setEdgeWeight(entry.getKey().getSource(), entry.getKey().getTarget(), bag.getGraph().edgeSet().size());
            }
        }

        List<Graph<Node, GCLEdge>> virtualTopologyList = new ArrayList<>();
        virtualTopologyList.add(physicalTopology);
        virtualTopologyList.add(virtualTopology1);
        virtualTopologyList.add(virtualTopology2);

        return virtualTopologyList;
    }

    private List<Graph<Node, GCLEdge>> createVirtualTopologyListForHierarchicalMin(Bag bag, List<Unicast> ttUnicastList) {
        try {
            //Physical Topology
            Graph<Node, GCLEdge> physicalTopology = copyGraph(bag.getGraph());

            Map<GCLEdge, Double> edgeDurationMap = getEdgeDurationMap(ttUnicastList);

            List<Double> durationList = new ArrayList<>(edgeDurationMap.values());

            //WEKA
            ArrayList<Attribute> attributes = new ArrayList<>();
            attributes.add(new Attribute("value"));

            Instances data = new Instances("Durations", attributes, 0);

            for (double value : durationList) {
                Instance instance = new DenseInstance(1);
                instance.setValue(attributes.getFirst(), value);
                data.add(instance);
            }

            HierarchicalClusterer clusterer = new HierarchicalClusterer();
            clusterer.buildClusterer(data);

            Map<Double, Integer> durationAndClusterMap = new HashMap<>();
            Set<Integer> differentCluster = new HashSet<>();

            for (int i = 0; i < data.numInstances(); i++) {
                Instance instance = data.instance(i);
                // Cluster Id
                int cluster = clusterer.clusterInstance(instance);
                differentCluster.add(cluster);
                // Value
                double value = instance.value(0);

                durationAndClusterMap.put(value, cluster);
            }

            List<Integer> differentClusters = new ArrayList<>();

            for (Map.Entry<Double, Integer> entry : durationAndClusterMap.entrySet()) {
                if (!differentClusters.contains(entry.getValue())) {
                    differentClusters.add(entry.getValue());
                }
            }

            List<Double> durationsSorted = new ArrayList<>(edgeDurations);
            durationsSorted.sort(Comparator.reverseOrder());

            Map<Double, Integer> durationAndClusterMapFixed = new HashMap<>();

            int j = 0;
            List<Double> visited = new ArrayList<>();
            while (j < differentClusters.size()) {
                for (Double duration : durationsSorted) {
                    if (!visited.contains(duration)) {
                        Integer clusterID = durationAndClusterMap.get(duration);
                        List<Double> durations = getDuration(durationAndClusterMap, clusterID);
                        for (Double durInner : durations) {
                            durationAndClusterMapFixed.put(durInner, j);
                            visited.add(durInner);
                        }
                        j++;
                        break;
                    }
                }
            }

            List<Graph<Node, GCLEdge>> virtualTopologyList = new ArrayList<>();

            virtualTopologyList.add(physicalTopology);

            for (int i = 0; i < differentCluster.size(); i++) {
                virtualTopologyList.add(copyGraph(aTopology));
            }

            for (int i = 1; i < virtualTopologyList.size(); i++) {
                for (GCLEdge edge : virtualTopologyList.get(i).edgeSet()) {
                    if (edgeDurationMap.containsKey(edge) && (durationAndClusterMapFixed.get(edgeDurationMap.get(edge)) + 1 == i)) {
                        virtualTopologyList.get(i).setEdgeWeight(edge.getSource(), edge.getTarget(), Double.MAX_VALUE);
                    }
                }
            }

            return virtualTopologyList;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Collections.emptyList();
    }
}
