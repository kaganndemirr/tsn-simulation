package ktu.kaganndemirr.util;

import ktu.kaganndemirr.application.Application;
import ktu.kaganndemirr.architecture.GCLEdge;
import ktu.kaganndemirr.architecture.Node;
import ktu.kaganndemirr.evaluator.Evaluator;
import org.jgrapht.Graph;

import java.util.List;

public class Bag {
    private String tsnSimulationVersion;
    private Graph<Node, GCLEdge> graph;
    private List<Application> applicationList;
    private String topologyName;
    private String applicationName;
    private String routing;
    private String mtrName;
    private int vtNumber;
    private String pathFindingMethod;
    private String algorithm;
    private String unicastCandidateSortingMethod;
    private String lwr;
    private int k;
    private String mcdmName;
    private String mcdmObjective;
    private String wsmNormalization;
    private String cwr;
    private double wSRT;
    private double wTT;
    private double wLength;
    private double wUtil;
    private int threadNumber;
    private int timeout;
    private String wpmVersion;
    private String wpmValueType;
    private String metaheuristicName;
    private Evaluator evaluator;
    private String evaluatorName;
    private int rate;

    public String getTSNSimulationVersion() {
        return tsnSimulationVersion;
    }

    public void setTSNSimulationVersion(String tsnSimulationVersion) {
        this.tsnSimulationVersion = tsnSimulationVersion;
    }

    public Graph<Node, GCLEdge> getGraph() {
        return graph;
    }

    public void setGraph(Graph<Node, GCLEdge> graph) {
        this.graph = graph;
    }

    public List<Application> getApplicationList() {
        return applicationList;
    }

    public void setApplicationList(List<Application> applicationList) {
        this.applicationList = applicationList;
    }

    public String getTopologyName() {
        return topologyName;
    }

    public void setTopologyName(String topologyName) {
        this.topologyName = topologyName;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getRouting() {
        return routing;
    }

    public void setRouting(String routing) {
        this.routing = routing;
    }

    public String getMTRName() {
        return mtrName;
    }

    public void setMTRName(String mtrName) {
        this.mtrName = mtrName;
    }

    public int getVTNumber() {
        return vtNumber;
    }

    public void setVTNumber(int vtNumber) {
        this.vtNumber = vtNumber;
    }

    public String getPathFindingMethod() {
        return pathFindingMethod;
    }

    public void setPathFindingMethod(String pathFindingMethod) {
        this.pathFindingMethod = pathFindingMethod;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public String getUnicastCandidateSortingMethod() {
        return unicastCandidateSortingMethod;
    }

    public void setUnicastCandidateSortingMethod(String unicastCandidateSortingMethod) {
        this.unicastCandidateSortingMethod = unicastCandidateSortingMethod;
    }

    public String getLWR(){
        return lwr;
    }

    public void setLWR(String lwr){
        this.lwr = lwr;
    }

    public int getK() {
        return k;
    }

    public void setK(int k) {
        this.k = k;
    }

    public String getMCDMName() {
        return mcdmName;
    }

    public void setMCDMName(String mcdmName) {
        this.mcdmName = mcdmName;
    }

    public String getMCDMObjective() {
        return mcdmObjective;
    }

    public void setMCDMObjective(String mcdmObjective) {
        this.mcdmObjective = mcdmObjective;
    }

    public String getWSMNormalization() {
        return wsmNormalization;
    }

    public void setWSMNormalization(String wsmNormalization) {
        this.wsmNormalization = wsmNormalization;
    }

    public String getCWR(){
        return cwr;
    }

    public void setCWR(String cwr){
        this.cwr = cwr;
    }

    public double getWSRT() {
        return wSRT;
    }

    public void setWSRT(double wSRT) {
        this.wSRT = wSRT;
    }

    public double getWTT() {
        return wTT;
    }

    public void setWTT(double wTT) {
        this.wTT = wTT;
    }

    public double getWLength() {
        return wLength;
    }

    public void setWLength(double wLength) {
        this.wLength = wLength;
    }

    public double getWUtil() {
        return wUtil;
    }

    public void setWUtil(double wUtil) {
        this.wUtil = wUtil;
    }

    public int getThreadNumber() {
        return threadNumber;
    }

    public void setThreadNumber(int threadNumber) {
        this.threadNumber = threadNumber;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public String getWPMVersion() {
        return wpmVersion;
    }

    public void setWPMVersion(String wpmVersion) {
        this.wpmVersion = wpmVersion;
    }

    public String getWPMValueType() {
        return wpmValueType;
    }

    public void setWPMValueType(String wpmValueType) {
        this.wpmValueType = wpmValueType;
    }

    public String getMetaheuristicName() {
        return metaheuristicName;
    }

    public void setMetaheuristicName(String metaheuristicName) {
        this.metaheuristicName = metaheuristicName;
    }

    public Evaluator getEvaluator() {
        return evaluator;
    }

    public void setEvaluator(Evaluator evaluator) {
        this.evaluator = evaluator;
    }

    public String getEvaluatorName() {
        return evaluatorName;
    }

    public void setEvaluatorName(String evaluatorName) {
        this.evaluatorName = evaluatorName;
    }

    public int getRate() {
        return rate;
    }

    public void setRate(int rate) {
        this.rate = rate;
    }

}




