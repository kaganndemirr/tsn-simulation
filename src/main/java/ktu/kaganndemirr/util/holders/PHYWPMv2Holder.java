package ktu.kaganndemirr.util.holders;

public class PHYWPMv2Holder {
    private String topologyName = null;
    private String applicationName = null;
    private String routing;
    private String pathFindingMethod;
    private String algorithm;
    private int k;
    private String wpmObjective;
    private double wSRT;
    private double wTT;
    private double wLength;
    private double wUtil;
    private String wpmVersion;
    private String wpmValueType;

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

    public int getK() {
        return k;
    }

    public void setK(int k) {
        this.k = k;
    }

    public String getWPMObjective() {
        return wpmObjective;
    }

    public void setWPMObjective(String wpmObjective) {
        this.wpmObjective = wpmObjective;
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

}
