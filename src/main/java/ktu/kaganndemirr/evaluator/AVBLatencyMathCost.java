package ktu.kaganndemirr.evaluator;

import ktu.kaganndemirr.message.Multicast;
import ktu.kaganndemirr.util.holders.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class AVBLatencyMathCost implements Cost, Comparator<AVBLatencyMathCost> {

    private static Logger logger = LoggerFactory.getLogger(AVBLatencyMathCost.class.getSimpleName());

    private double o1;
    private double o2;
    private double o3;

    private boolean isUsed;

    private final Map<Multicast, Double> worstCaseDelayMap = new HashMap<>();

    public AVBLatencyMathCost() {
        reset();
    }

    public void setWCD(Multicast multicast, Double worstCaseDelay) {
        worstCaseDelayMap.put(multicast, worstCaseDelay);
    }

    public void add(O objective, double value) {
        isUsed = true;
        switch (objective) {
            case one -> o1 += value;
            case two -> o2 += value;
            case three -> o3 += value;
        }
    }

    @Override
    public double getTotalCost() {
        if (!isUsed) {
            return Double.MAX_VALUE;
        }
        double w1 = 10000;
        double w2 = 3.0;
        double w3 = 1.0;
        return w1 * o1 + w2 * o2 + w3 * o3;
    }

    @Override
    public void reset() {
        isUsed = false;
        o1 = 0.0;
        o2 = 0.0;
        o3 = 0.0;
    }

    @Override
    public int compare(AVBLatencyMathCost o1, AVBLatencyMathCost o2) {
        return (int) Math.round(o1.getTotalCost() - o2.getTotalCost());
    }

    public enum O {
        one, two, three;
    }

    public String toString() {
        return getTotalCost() + " (current o1 = " + o1 + ", o2 = " + o2 + ", o3 = " + o3 + ")";
    }

    public String toDetailedString() {
        return "Total : " + this + " | o1 " + o1 + ", o2 " + o2 + ", o3 " + o3 + " -- " + worstCaseDelayMap + " --";
    }

    @Override
    public Map<Multicast, Double> getWCDMap() {
        return worstCaseDelayMap;
    }

    @Override
    public void writePHYWPMv1ResultToFile(PHYWPMv1Holder phyWPMv1Holder) {
        String mainFolderOutputLocation = Paths.get("outputs", phyWPMv1Holder.getRouting(), phyWPMv1Holder.getPathFindingMethod(), phyWPMv1Holder.getAlgorithm(), String.valueOf(phyWPMv1Holder.getK()), phyWPMv1Holder.getWPMObjective(), String.valueOf(phyWPMv1Holder.getWSRT()), String.valueOf(phyWPMv1Holder.getWTT()), String.valueOf(phyWPMv1Holder.getWLength()), String.valueOf(phyWPMv1Holder.getWUtil()), phyWPMv1Holder.getWPMVersion(), phyWPMv1Holder.getTopologyName() + "_" + phyWPMv1Holder.getApplicationName()).toString();

        boolean isCreated = new File(mainFolderOutputLocation).mkdirs();

        if (isCreated) {
            String resultFileOutputLocation = Paths.get("outputs", phyWPMv1Holder.getRouting(), phyWPMv1Holder.getPathFindingMethod(), phyWPMv1Holder.getAlgorithm(), String.valueOf(phyWPMv1Holder.getK()), phyWPMv1Holder.getWPMObjective(), String.valueOf(phyWPMv1Holder.getWSRT()), String.valueOf(phyWPMv1Holder.getWTT()), String.valueOf(phyWPMv1Holder.getWLength()), String.valueOf(phyWPMv1Holder.getWUtil()), phyWPMv1Holder.getWPMVersion()).toString();

            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(Paths.get(resultFileOutputLocation, "Results.txt").toString(), true));
                writer.write(phyWPMv1Holder.getTopologyName() + "_" + phyWPMv1Holder.getApplicationName() + "\n");
                writer.write("cost = " + getTotalCost() + ", o1 = " + o1 + ", o2 = " + o2 + ", o3 = " + o3 + "\n");
                writer.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void writePHYWPMv2ResultToFile(PHYWPMv2Holder phyWPMv2Holder) {
        String mainFolderOutputLocation = Paths.get("outputs", phyWPMv2Holder.getRouting(), phyWPMv2Holder.getPathFindingMethod(), phyWPMv2Holder.getAlgorithm(), String.valueOf(phyWPMv2Holder.getK()), phyWPMv2Holder.getWPMObjective(), String.valueOf(phyWPMv2Holder.getWSRT()), String.valueOf(phyWPMv2Holder.getWTT()), String.valueOf(phyWPMv2Holder.getWLength()), String.valueOf(phyWPMv2Holder.getWUtil()), phyWPMv2Holder.getWPMVersion(), phyWPMv2Holder.getWPMValueType(), phyWPMv2Holder.getTopologyName() + "_" + phyWPMv2Holder.getApplicationName()).toString();

        boolean isCreated = new File(mainFolderOutputLocation).mkdirs();

        if(isCreated){
            String resultFileOutputLocation = Paths.get("outputs", phyWPMv2Holder.getRouting(), phyWPMv2Holder.getPathFindingMethod(), phyWPMv2Holder.getAlgorithm(), String.valueOf(phyWPMv2Holder.getK()), phyWPMv2Holder.getWPMObjective(), String.valueOf(phyWPMv2Holder.getWSRT()), String.valueOf(phyWPMv2Holder.getWTT()), String.valueOf(phyWPMv2Holder.getWLength()), String.valueOf(phyWPMv2Holder.getWUtil()), phyWPMv2Holder.getWPMVersion(), phyWPMv2Holder.getWPMValueType()).toString();

            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(Paths.get(resultFileOutputLocation, "Results.txt").toString(), true));
                writer.write(phyWPMv2Holder.getTopologyName() + "_" + phyWPMv2Holder.getApplicationName() + "\n");
                writer.write("cost = " + getTotalCost() + ", o1 = " + o1 + ", o2 = " + o2 + ", o3 = " + o3 + "\n");
                writer.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void writePHYWPMLWRv1ResultToFile(PHYWPMLWRv1Holder phyWPMLWRv1Holder) {
        String mainFolderOutputLocation = Paths.get("outputs", phyWPMLWRv1Holder.getRouting(), phyWPMLWRv1Holder.getPathFindingMethod(), phyWPMLWRv1Holder.getAlgorithm(), phyWPMLWRv1Holder.getLWR(), String.valueOf(phyWPMLWRv1Holder.getK()), phyWPMLWRv1Holder.getWPMObjective(), String.valueOf(phyWPMLWRv1Holder.getWSRT()), String.valueOf(phyWPMLWRv1Holder.getWTT()), String.valueOf(phyWPMLWRv1Holder.getWLength()), String.valueOf(phyWPMLWRv1Holder.getWUtil()), phyWPMLWRv1Holder.getWPMVersion(), phyWPMLWRv1Holder.getTopologyName() + "_" + phyWPMLWRv1Holder.getApplicationName()).toString();

        boolean isCreated = new File(mainFolderOutputLocation).mkdirs();

        if (isCreated) {
            String resultFileOutputLocation = Paths.get("outputs", phyWPMLWRv1Holder.getRouting(), phyWPMLWRv1Holder.getPathFindingMethod(), phyWPMLWRv1Holder.getAlgorithm(), phyWPMLWRv1Holder.getLWR(), String.valueOf(phyWPMLWRv1Holder.getK()), phyWPMLWRv1Holder.getWPMObjective(), String.valueOf(phyWPMLWRv1Holder.getWSRT()), String.valueOf(phyWPMLWRv1Holder.getWTT()), String.valueOf(phyWPMLWRv1Holder.getWLength()), String.valueOf(phyWPMLWRv1Holder.getWUtil()), phyWPMLWRv1Holder.getWPMVersion()).toString();

            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(Paths.get(resultFileOutputLocation, "Results.txt").toString(), true));
                writer.write(phyWPMLWRv1Holder.getTopologyName() + "_" + phyWPMLWRv1Holder.getApplicationName() + "\n");
                writer.write("cost = " + getTotalCost() + ", o1 = " + o1 + ", o2 = " + o2 + ", o3 = " + o3 + "\n");
                writer.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void writePHYWPMLWRv2ResultToFile(PHYWPMLWRv2Holder phyWPMLWRv2Holder) {
        String mainFolderOutputLocation = Paths.get("outputs", phyWPMLWRv2Holder.getRouting(), phyWPMLWRv2Holder.getPathFindingMethod(), phyWPMLWRv2Holder.getAlgorithm(), phyWPMLWRv2Holder.getLWR(), String.valueOf(phyWPMLWRv2Holder.getK()), phyWPMLWRv2Holder.getWPMObjective(), String.valueOf(phyWPMLWRv2Holder.getWSRT()), String.valueOf(phyWPMLWRv2Holder.getWTT()), String.valueOf(phyWPMLWRv2Holder.getWLength()), String.valueOf(phyWPMLWRv2Holder.getWUtil()), phyWPMLWRv2Holder.getWPMVersion(), phyWPMLWRv2Holder.getWPMValueType(), phyWPMLWRv2Holder.getTopologyName() + "_" + phyWPMLWRv2Holder.getApplicationName()).toString();

        boolean isCreated = new File(mainFolderOutputLocation).mkdirs();

        if (isCreated) {
            String resultFileOutputLocation = Paths.get("outputs", phyWPMLWRv2Holder.getRouting(), phyWPMLWRv2Holder.getPathFindingMethod(), phyWPMLWRv2Holder.getAlgorithm(), phyWPMLWRv2Holder.getLWR(), String.valueOf(phyWPMLWRv2Holder.getK()), phyWPMLWRv2Holder.getWPMObjective(), String.valueOf(phyWPMLWRv2Holder.getWSRT()), String.valueOf(phyWPMLWRv2Holder.getWTT()), String.valueOf(phyWPMLWRv2Holder.getWLength()), String.valueOf(phyWPMLWRv2Holder.getWUtil()), phyWPMLWRv2Holder.getWPMVersion(), phyWPMLWRv2Holder.getWPMValueType()).toString();

            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(Paths.get(resultFileOutputLocation, "Results.txt").toString(), true));
                writer.write(phyWPMLWRv2Holder.getTopologyName() + "_" + phyWPMLWRv2Holder.getApplicationName() + "\n");
                writer.write("cost = " + getTotalCost() + ", o1 = " + o1 + ", o2 = " + o2 + ", o3 = " + o3 + "\n");
                writer.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void writePHYWPMCWRv1ResultToFile(PHYWPMCWRv1Holder phyWPMCWRv1Holder) {
        String mainFolderOutputLocation = Paths.get("outputs", phyWPMCWRv1Holder.getRouting(), phyWPMCWRv1Holder.getPathFindingMethod(), phyWPMCWRv1Holder.getAlgorithm(), String.valueOf(phyWPMCWRv1Holder.getK()), phyWPMCWRv1Holder.getWPMObjective(), phyWPMCWRv1Holder.getCWR(), phyWPMCWRv1Holder.getWPMVersion(), phyWPMCWRv1Holder.getTopologyName() + "_" + phyWPMCWRv1Holder.getApplicationName()).toString();

        boolean isCreated = new File(mainFolderOutputLocation).mkdirs();

        if (isCreated) {
            String resultFileOutputLocation = Paths.get("outputs", phyWPMCWRv1Holder.getRouting(), phyWPMCWRv1Holder.getPathFindingMethod(), phyWPMCWRv1Holder.getAlgorithm(), String.valueOf(phyWPMCWRv1Holder.getK()), phyWPMCWRv1Holder.getWPMObjective(), phyWPMCWRv1Holder.getCWR(), phyWPMCWRv1Holder.getWPMVersion()).toString();


            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(Paths.get(resultFileOutputLocation, "Results.txt").toString(), true));
                writer.write(phyWPMCWRv1Holder.getTopologyName() + "_" + phyWPMCWRv1Holder.getApplicationName() + "\n");
                writer.write("cost = " + getTotalCost() + ", o1 = " + o1 + ", o2 = " + o2 + ", o3 = " + o3 + "\n");
                writer.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void writePHYWPMCWRv2ResultToFile(PHYWPMCWRv2Holder phyWPMCWRv2Holder) {
        String mainFolderOutputLocation = Paths.get("outputs", phyWPMCWRv2Holder.getRouting(), phyWPMCWRv2Holder.getPathFindingMethod(), phyWPMCWRv2Holder.getAlgorithm(), String.valueOf(phyWPMCWRv2Holder.getK()), phyWPMCWRv2Holder.getWPMObjective(), phyWPMCWRv2Holder.getCWR(), phyWPMCWRv2Holder.getWPMVersion(), phyWPMCWRv2Holder.getWPMValueType(), phyWPMCWRv2Holder.getTopologyName() + "_" + phyWPMCWRv2Holder.getApplicationName()).toString();

        boolean isCreated = new File(mainFolderOutputLocation).mkdirs();

        if (isCreated) {
            String resultFileOutputLocation = Paths.get("outputs", phyWPMCWRv2Holder.getRouting(), phyWPMCWRv2Holder.getPathFindingMethod(), phyWPMCWRv2Holder.getAlgorithm(), String.valueOf(phyWPMCWRv2Holder.getK()), phyWPMCWRv2Holder.getWPMObjective(), phyWPMCWRv2Holder.getCWR(), phyWPMCWRv2Holder.getWPMVersion(), phyWPMCWRv2Holder.getWPMValueType()).toString();


            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(Paths.get(resultFileOutputLocation, "Results.txt").toString(), true));
                writer.write(phyWPMCWRv2Holder.getTopologyName() + "_" + phyWPMCWRv2Holder.getApplicationName() + "\n");
                writer.write("cost = " + getTotalCost() + ", o1 = " + o1 + ", o2 = " + o2 + ", o3 = " + o3 + "\n");
                writer.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
