package ktu.kaganndemirr.util;

import ktu.kaganndemirr.architecture.GCL;
import ktu.kaganndemirr.architecture.GCLEdge;
import ktu.kaganndemirr.architecture.Node;
import ktu.kaganndemirr.message.Unicast;
import org.jgrapht.Graph;
import weka.clusterers.HierarchicalClusterer;
import weka.clusterers.SimpleKMeans;
import weka.core.Attribute;
import weka.core.DenseInstance;
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

        bag.setVTNumber(Constants.MTR_V1_VT_NUMBER);

        return virtualTopologyList;
    }

    public static List<Graph<Node, GCLEdge>> createVirtualTopologyListForAverage(Bag bag, List<Unicast> ttUnicastList) {


        //Physical Topology
        Graph<Node, GCLEdge> physicalTopology = copyGraph(bag.getGraph());

        //Virtual Topologies
        Graph<Node, GCLEdge> virtualTopology1 = copyGraph(bag.getGraph());
        Graph<Node, GCLEdge> virtualTopology2 = copyGraph(bag.getGraph());

        Map<GCLEdge, Double> edgeDurationMap = getEdgeTotalDurationMap(ttUnicastList);

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

        bag.setVTNumber(Constants.MTR_AVERAGE_VT_NUMBER);

        return virtualTopologyList;
    }

    public static List<Graph<Node, GCLEdge>> createVirtualTopologyListForHierarchical(Bag bag, Map<Integer, List<Double>> clusterAndDurationListMap, List<Unicast> ttUnicastList) {
        List<Graph<Node, GCLEdge>> topologyList = new ArrayList<>();
        for (int i = 0; i < clusterAndDurationListMap.size(); i++) {
            topologyList.add(copyGraph(bag.getGraph()));
        }

        bag.setVTNumber(clusterAndDurationListMap.size());

        Map<Integer, List<Double>> sortedClusterAndDurationListMap = sortClusterAndDurationListMap(clusterAndDurationListMap);

        Map<GCLEdge, Double> edgeDurationMap = getEdgeTotalDurationMap(ttUnicastList);

        Set<Double> cumulativeDurationSet = new HashSet<>();
        for(Map.Entry<Integer, List<Double>> entry: sortedClusterAndDurationListMap.entrySet()){
            cumulativeDurationSet.addAll(entry.getValue());
            for(Double duration: cumulativeDurationSet){
                List<GCLEdge> edgeList = getGCLEdgeListByDuration(edgeDurationMap, duration);
                for(GCLEdge edge: edgeList){
                    topologyList.get(entry.getKey()).setEdgeWeight(topologyList.get(entry.getKey()).getEdgeSource(edge), topologyList.get(entry.getKey()).getEdgeTarget(edge), topologyList.get(entry.getKey()).edgeSet().size());
                }
            }
        }

        topologyList.add(copyGraph(bag.getGraph()));

        return topologyList;
    }

    private static Map<GCLEdge, Double> getEdgeTotalDurationMap(List<Unicast> ttUnicastList) {
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

    // Do not sum duration of same link
    private static Map<GCLEdge, List<Double>> getEdgeDurationMap(List<Unicast> ttUnicastList) {
        Map<GCLEdge, List<Double>> edgeDurationMap = new HashMap<>();
        for (Unicast unicast : ttUnicastList) {
            for (GCLEdge edge : unicast.getPath().getEdgeList()) {
                for (GCL gcl : edge.getGCL()) {
                    if (!edgeDurationMap.containsKey(edge)) {
                        edgeDurationMap.put(edge, new ArrayList<>());
                        edgeDurationMap.get(edge).add(gcl.getDuration());
                    } else {
                        edgeDurationMap.get(edge).add(gcl.getDuration());
                    }
                }

            }
        }
        return edgeDurationMap;
    }

    public static Map<Integer, List<Double>> runClusterAlgorithm(Bag bag, List<Unicast> ttUnicastList) {
        Map<Integer, List<Double>> clusterAndDurationListMap = new HashMap<>();
        try {
            Map<GCLEdge, Double> edgeDurationMap = getEdgeTotalDurationMap(ttUnicastList);

            List<Double> durationList = edgeDurationMap.values().stream().toList();

            //WEKA
            ArrayList<Attribute> attributes = new ArrayList<>();
            attributes.add(new Attribute("value"));

            Instances dataset = new Instances("Durations", attributes, 0);

            for (double duration: durationList) {
                dataset.add(new DenseInstance(1.0, new double[]{duration}));
            }

            if(Objects.equals(bag.getMTRName(), Constants.MTR_HIERARCHICAL)){
                HierarchicalClusterer clusterer = new HierarchicalClusterer();
                if (bag.getVTNumber() != 0){
                    clusterer.setNumClusters(bag.getVTNumber());
                }
                clusterer.buildClusterer(dataset);

                for (int i = 0; i < dataset.numInstances(); i++) {
                    int cluster = clusterer.clusterInstance(dataset.instance(i));
                    double duration = dataset.instance(i).value(0);

                    if(!clusterAndDurationListMap.containsKey(cluster)){
                        clusterAndDurationListMap.put(cluster, new ArrayList<>());
                        clusterAndDurationListMap.get(cluster).add(duration);
                    }else {
                        clusterAndDurationListMap.get(cluster).add(duration);
                    }
                }

            } else if (Objects.equals(bag.getMTRName(), Constants.MTR_KMEANS)) {
                SimpleKMeans clusterer = new SimpleKMeans();
                if (bag.getVTNumber() != 0){
                    clusterer.setNumClusters(bag.getVTNumber());
                }
                clusterer.buildClusterer(dataset);

                for (int i = 0; i < dataset.numInstances(); i++) {
                    int cluster = clusterer.clusterInstance(dataset.instance(i));
                    double duration = dataset.instance(i).value(0);

                    if(!clusterAndDurationListMap.containsKey(cluster)){
                        clusterAndDurationListMap.put(cluster, new ArrayList<>());
                        clusterAndDurationListMap.get(cluster).add(duration);
                    }else {
                        clusterAndDurationListMap.get(cluster).add(duration);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return clusterAndDurationListMap;
    }

    private static Map<Integer, List<Double>> sortClusterAndDurationListMap(Map<Integer, List<Double>> clusterAndDurationListMap){
        List<Map.Entry<Integer, List<Double>>> entryList = new ArrayList<>(clusterAndDurationListMap.entrySet());

        entryList.sort(Comparator.comparingDouble(e -> Collections.max(e.getValue())));
        Collections.reverse(entryList);

        Map<Integer, List<Double>> sortedClusterAndDurationListMap = new LinkedHashMap<>();
        int newKey = 0;
        for (Map.Entry<Integer, List<Double>> entry : entryList) {
            sortedClusterAndDurationListMap.put(newKey++, entry.getValue());
        }

        return sortedClusterAndDurationListMap;
    }

    private static List<GCLEdge> getGCLEdgeListByDuration(Map<GCLEdge, Double> edgeDurationMap, Double duration){
        List<GCLEdge> gclEdgeList = new ArrayList<>();

        for (Map.Entry<GCLEdge, Double> entry : edgeDurationMap.entrySet()) {
            if (entry.getValue().equals(duration)) {
                gclEdgeList.add(entry.getKey());
            }
        }

        return gclEdgeList;
    }

    public static List<Integer> findKForTopologies(int k, int virtualTopologyListSize) {
        List<Integer> kList = new ArrayList<>();
        if (k % virtualTopologyListSize == 0) {
            for (int j = 0; j < virtualTopologyListSize; j++) {
                kList.add(k / virtualTopologyListSize);
            }
        } else {
            for (int j = 0; j < virtualTopologyListSize; j++) {
                kList.add(k / virtualTopologyListSize);
            }
            int z = 0;
            while (z < k % virtualTopologyListSize) {
                kList.set(z, kList.get(z) + 1);
                z++;
            }
        }
        return kList;
    }
}
