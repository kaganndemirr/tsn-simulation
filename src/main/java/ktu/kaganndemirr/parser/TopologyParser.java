package ktu.kaganndemirr.parser;

import ktu.kaganndemirr.architecture.EndSystem;
import ktu.kaganndemirr.architecture.GCLEdge;
import ktu.kaganndemirr.architecture.Node;
import ktu.kaganndemirr.architecture.Switch;
import org.jgrapht.graph.AbstractBaseGraph;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.Map;

public class TopologyParser {
    private static final Logger logger = LoggerFactory.getLogger(TopologyParser.class.getSimpleName());

    public static AbstractBaseGraph<Node, GCLEdge> parse(File f, int rate, double idleSlope) {

        AbstractBaseGraph<Node, GCLEdge> graph = new SimpleDirectedWeightedGraph<>(GCLEdge.class);

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        Document dom;

        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            dom = db.parse(f);
            Element docEle = dom.getDocumentElement();

            Element graphEle = (Element) docEle.getElementsByTagName("graph").item(0);
            Map<String, Node> nodeMap = new HashMap<>();

            String edgeDefault = graphEle.getAttribute("edgedefault");
            boolean isDirected = switch (edgeDefault) {
                case "directed" -> true;
                case "undirected" -> false;
                default -> throw new InputMismatchException("edgeDefault " + edgeDefault + " is not supported");
            };

            //Parse nodes and create graph-vertices accordingly
            NodeList nl = graphEle.getElementsByTagName("node");
            if (nl.getLength() > 0) {
                for (int i = 0; i < nl.getLength(); i++) {
                    String nodeName = ((Element) nl.item(i)).getAttribute("id");
                    nodeName = nodeName.toUpperCase();
                    Node n;
                    if (nodeName.startsWith("ES")) {
                        n = new EndSystem(nodeName);
                    } else if (nodeName.startsWith("B") || nodeName.startsWith("SW")) {
                        n = new Switch(nodeName);
                    } else {
                        throw new InputMismatchException("Aborting : Node type of " + nodeName + " unrecognized.");
                    }
                    nodeMap.put(nodeName, n);
                    graph.addVertex(n);
                }
            }

            //Parse edges and create graph-edges accordingly
            nl = graphEle.getElementsByTagName("edge");
            if (nl.getLength() > 0) {
                for (int i = 0; i < nl.getLength(); i++) {
                    String source = ((Element) nl.item(i)).getAttribute("source");
                    source = source.toUpperCase();

                    String target = ((Element) nl.item(i)).getAttribute("target");
                    target = target.toUpperCase();

                    graph.addEdge(nodeMap.get(source), nodeMap.get(target), new GCLEdge(rate, idleSlope));
                    if (!isDirected) {
                        graph.addEdge(nodeMap.get(target), nodeMap.get(source), new GCLEdge(rate, idleSlope));
                    }
                }
            }
            nodeMap.clear();

        } catch (ParserConfigurationException | SAXException | IOException pce) {
            throw new RuntimeException(pce);

        }
        return graph;
    }
}
