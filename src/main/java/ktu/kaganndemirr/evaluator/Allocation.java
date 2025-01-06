package ktu.kaganndemirr.evaluator;

public class Allocation {
    private final boolean isTT;
    private final double cmi;
    private double allocationMbps;

    public Allocation(boolean isTT, double cmi, double allocationMbps){
        this.isTT = isTT;
        this.cmi = cmi;
        this.allocationMbps = allocationMbps;
    }

    public boolean getIsTT() {
        return isTT;
    }

    public double getAllocationMbps() {
        return allocationMbps;
    }

    public double getCMI() {
        return cmi;
    }

    public void setAllocationMbps(double allocationMbps) {
        this.allocationMbps = allocationMbps;
    }
}
