package ktu.kaganndemirr.util.holders.phy;

public class PHYWSMv2LWRHolder {
    private String topologyName;
    private String applicationName;
    private String routing;
    private String pathFindingMethod;
    private String algorithm;
    private String lwr;
    private int k;
    private String mcdmObjective;
    private String wsmNormalization;
    private double wSRT;
    private double wTT;
    private double wLength;
    private double wUtil;

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

}


