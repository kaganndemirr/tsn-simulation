package ktu.kaganndemirr.parser;

import ktu.kaganndemirr.application.Application;
import ktu.kaganndemirr.application.SRTApplication;
import ktu.kaganndemirr.application.TTApplication;
import ktu.kaganndemirr.architecture.*;
import ktu.kaganndemirr.util.Constants;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.graph.GraphWalk;
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
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class ApplicationParser {
    private static final Logger logger = LoggerFactory.getLogger(ApplicationParser.class.getSimpleName());

    public static List<Application> parse(File f, String TSNSimulationVersion, int rate, Graph<Node, GCLEdge> graph) {

        List<Application> applications = new ArrayList<>();

        if(Objects.equals(TSNSimulationVersion, Constants.TSNCF) || Objects.equals(TSNSimulationVersion, Constants.TSNCF_V2) || Objects.equals(TSNSimulationVersion, Constants.TSN_TSNSCHED)){
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            Document dom;

            try {
                DocumentBuilder db = dbf.newDocumentBuilder();
                dom = db.parse(f);
                Element docEle = dom.getDocumentElement();

                //Get node list of AVBApplicationElements
                NodeList nl = docEle.getElementsByTagName("AVBApplication");
                if (nl.getLength() > 0) {
                    for (int i = 0; i < nl.getLength(); i++) {
                        //Get the SRTApplication element
                        Element srtAppEle = (Element) nl.item(i);
                        //Get the SRTApplication object
                        Application avbApp = getSRTApplication(srtAppEle, i);
                        //Add it to the application list
                        applications.add(avbApp);
                    }
                }

                //Get node list of TTApplicationElements
                nl = docEle.getElementsByTagName("TTApplication");
                if (nl.getLength() > 0) {
                    for (int i = 0; i < nl.getLength(); i++) {
                        //Get the TTApplication element
                        Element ttAppEle = (Element) nl.item(i);
                        //Get the TTApplication object
                        Application ttApp = getTTApplication(ttAppEle, rate, graph, i);
                        //Add it to the application list
                        applications.add(ttApp);
                    }
                }

            } catch (ParserConfigurationException | SAXException | IOException pce) {
                throw new RuntimeException();
            }

            return applications;
        }

        return null;
    }

    private static SRTApplication getSRTApplication(Element srtAppEle, int i) {
        String name = srtAppEle.getAttribute("name");
        int pcp = Constants.CLASS_A_PCP;
        String applicationType = Constants.applicationTypeMap.get(pcp);
        int frameSizeByte = parsePayloadSize(srtAppEle);
        int numberOfFrames = parseNoOfFrames(srtAppEle);
        int messageSizeByte = frameSizeByte * numberOfFrames;
        double cmi = parseInterval(srtAppEle);
        double messageSizeMbps = getMessageSizeMbps(frameSizeByte, numberOfFrames, cmi);
        int deadline = parseDeadline(srtAppEle);
        EndSystem source = parseSource(srtAppEle);
        List<EndSystem> targetList = parseTargetList(srtAppEle);

        //TODO
        List<GraphPath<Node, GCLEdge>> graphPathList = new ArrayList<>();

        return new SRTApplication(name, pcp, applicationType, frameSizeByte, numberOfFrames, messageSizeByte, messageSizeMbps, cmi, deadline, source, targetList, graphPathList);
    }

    private static TTApplication getTTApplication(Element ttAppEle, int rate, Graph<Node, GCLEdge> graph, int i) {
        String name = ttAppEle.getAttribute("name");
        int pcp = Constants.TT_PCP;
        String applicationType = Constants.applicationTypeMap.get(pcp);
        int frameSizeByte = 0;
        int messageSizeByte = 0;
        double cmi = Constants.TSN_CONFIGURATION_FRAMEWORK_CMI;
        int deadline = 0;
        EndSystem source = parseSource(ttAppEle);
        List<EndSystem> targetList = parseTargetList(ttAppEle);
        List<List<Node>> explicitPathRawList = parseExplicitPathRaw(source, ttAppEle);
        List<List<GCLEdge>> gclEdgeListList = createExplicitPathEdgeList(explicitPathRawList, graph);

        GCL gcl = getGCL(ttAppEle);

        List<GraphPath<Node, GCLEdge>> graphPathList = createExplicitPathGraphPathListForTT(source, gclEdgeListList, targetList, graph, gcl);

        int numberOfFrames = gcl.getFrequency();

        double messageSizeMbps = getTTMessageSizeMbps(gcl.getDuration(), numberOfFrames, Constants.TSN_CONFIGURATION_FRAMEWORK_CMI, rate);

        return new TTApplication(name, pcp, applicationType, frameSizeByte, numberOfFrames, messageSizeByte, messageSizeMbps, cmi, deadline, source, targetList, graphPathList);
    }

    private static int parsePayloadSize(Element appEle) {
        return Integer.parseInt(appEle.getElementsByTagName("PayloadSize").item(0).getFirstChild().getNodeValue());
    }

    private static int parseNoOfFrames(Element appEle) {
        return Integer.parseInt(appEle.getElementsByTagName("NoOfFrames").item(0).getFirstChild().getNodeValue());
    }

    private static double parseInterval(Element appEle) {
        return Integer.parseInt(appEle.getElementsByTagName("Interval").item(0).getFirstChild().getNodeValue());
    }

    private static double getMessageSizeMbps(int frameSizeByte, int numberOfFrames, double cmi){
        return (frameSizeByte * Constants.ONE_BYTE_TO_BIT) * numberOfFrames / cmi;
    }

    private static int parseDeadline(Element appEle) {
        return Integer.parseInt(appEle.getElementsByTagName("Deadline").item(0).getFirstChild().getNodeValue());
    }

    private static EndSystem parseSource(Element appEle) {
        return new EndSystem(((Element) appEle.getElementsByTagName("Source").item(0)).getAttribute("name"));
    }

    private static List<EndSystem> parseTargetList(Element appEle) {
        List<EndSystem> targetList = new ArrayList<>();
        Element el = (Element) appEle.getElementsByTagName("Destinations").item(0);
        NodeList nl = el.getElementsByTagName("Dest");
        if (nl.getLength() > 0) {
            for (int i = 0; i < nl.getLength(); i++) {
                targetList.add(new EndSystem(((Element) nl.item(i)).getAttribute("name")));
            }
        }
        return targetList;
    }

    private static double getTTMessageSizeMbps(double gclDuration, int numberOfFrames, int hyperPeriod, int rate){
        double gateOpenTimeAsSecond = (Constants.ONE_SECOND * (gclDuration * numberOfFrames)) / hyperPeriod;

        return (rate * gateOpenTimeAsSecond) / Constants.ONE_SECOND;
    }

    private static List<List<Node>> parseExplicitPathRaw(EndSystem source,Element ele) {
        List<List<Node>> path = new ArrayList<>();
        Element destEl = (Element) ele.getElementsByTagName("Destinations").item(0);
        NodeList destNL = destEl.getElementsByTagName("Dest");

        if (destNL.getLength() > 0) {
            path = new ArrayList<>(destNL.getLength());
            for (int i = 0; i < destNL.getLength(); i++) {
                EndSystem target = new EndSystem(((Element) destNL.item(i)).getAttribute("name"));
                Element routeEL = (Element) ele.getElementsByTagName("Route").item(0);
                if (routeEL != null) {
                    NodeList routeNL = routeEL.getElementsByTagName("Bridge");
                    if (routeNL.getLength() > 0) {
                        path.add(i, new LinkedList<>());
                        path.get(i).add(source);
                        for (int u = 0; u < routeNL.getLength(); u++) {
                            path.get(i).add(new Switch(((Element) routeNL.item(u)).getAttribute("name")));
                        }
                    }
                }
                path.get(i).add(target);
            }
        }
        return path;
    }

    private static GCL getGCL(Element ttAppEle) {
        Element destEl = (Element) ttAppEle.getElementsByTagName("Destinations").item(0);
        NodeList gclNL = destEl.getElementsByTagName("GCL");
        GCL gcl = null;
        if (gclNL.getLength() > 0) {
            for(int i = 0; i < gclNL.getLength(); i++){
                for (int j = 0; j < gclNL.getLength(); j++) {
                    double offset = Double.parseDouble(((Element) gclNL.item(j)).getAttribute("offset"));
                    double dur = Double.parseDouble(((Element) gclNL.item(j)).getAttribute("duration"));
                    int freq = Integer.parseInt(((Element) gclNL.item(j)).getAttribute("frequency"));

                    gcl = new GCL(offset, dur, freq);
                }

            }

        }
        return gcl;
    }

    public static List<List<GCLEdge>> createExplicitPathEdgeList(List<List<Node>> explicitPathRawList, Graph<Node, GCLEdge> graph) {
        List<List<GCLEdge>> gclEdgeListList = new ArrayList<>();

        for (List<Node> explicitPathRaw : explicitPathRawList) {
            List<GCLEdge> edgeList = new ArrayList<>();
            for (int i = 0; i < explicitPathRaw.size() - 1; i++) {
                Node fromNode = explicitPathRaw.get(i);
                Node toNode = explicitPathRaw.get(i + 1);
                edgeList.add(graph.getEdge(fromNode, toNode));
            }
            gclEdgeListList.add(edgeList);
        }

        return gclEdgeListList;
    }

    private static List<GraphPath<Node, GCLEdge>> createExplicitPathGraphPathListForTT(EndSystem source, List<List<GCLEdge>> gclEdgeListList, List<EndSystem> targetList, Graph<Node, GCLEdge> graph, GCL gcl) {
        List<GraphPath<Node, GCLEdge>> graphPathList = new ArrayList<>();
        for (int i = 0; i < targetList.size(); i++){
            GraphPath<Node, GCLEdge> graphPath = new GraphWalk<>(graph, source, targetList.get(i), gclEdgeListList.get(i), gclEdgeListList.size() * Constants.UNIT_WEIGHT);
            graphPathList.add(graphPath);
        }

        for(GraphPath<Node, GCLEdge> graphPath: graphPathList ){
            for(int i = 0; i < graphPath.getEdgeList().size(); i++){
                graphPath.getEdgeList().get(i).addGCL(new GCL(gcl.getOffset() + gcl.getDuration() * i, gcl.getDuration(), gcl.getFrequency()));
            }
        }

        return graphPathList;
    }
}
