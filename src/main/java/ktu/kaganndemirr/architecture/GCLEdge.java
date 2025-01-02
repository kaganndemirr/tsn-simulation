package ktu.kaganndemirr.architecture;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class GCLEdge extends DefaultWeightedEdge {

    private static final Logger logger = LoggerFactory.getLogger(GCLEdge.class.getSimpleName());

    private final double idleSlope;
    private final List<GCL> gclList;
    private final int rate;

    public GCLEdge(int rate, double idleSlope) {
        this.rate = rate;
        this.idleSlope = idleSlope;
        this.gclList = new LinkedList<>();
    }

    public GCLEdge(GCLEdge edge) {
        this.rate = edge.rate;
        this.idleSlope = edge.idleSlope;
        this.gclList = edge.gclList;

    }

    public void addGCL(GCL gcl) {
        gclList.add(gcl);
    }

    public List<GCL> getGCL() {
        return gclList;
    }

    public int getRate() {
        return rate;
    }

    public double getIdleSlope() {
        return idleSlope;
    }

    @Override
    public Node getSource() {
        return (Node) super.getSource();
    }

    @Override
    public Node getTarget() {
        return (Node) super.getTarget();
    }

    @Override
    public double getWeight() {
        return super.getWeight();
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        GCLEdge edge = (GCLEdge) object;
        return Objects.equals(getSource().id, edge.getSource().id) && Objects.equals(getTarget().id, edge.getTarget().id);

    }

    @Override
    public int hashCode() {
        return Objects.hash(getSource().id) + Objects.hash(getTarget().id);
    }

    public double getTTMessageSizeMbps(double duration) {
        return (getMaxTTInterference(duration) / duration - 1) * rate;
    }

    public double getMaxTTInterference(double duration) {
        double interference = duration;
        //Use cached value if available
        if (!gclList.isEmpty()) {
            List<GCE> gateControlEventList = convertGCLToGateControlEventList(gclList);
            List<GCE> mergedGateControlEventList = mergeGateControlEventList(gateControlEventList);
            double maxInterference = getMaxInterference(duration, mergedGateControlEventList);
            interference = duration + maxInterference;
        }
        return interference;
    }

    private List<GCE> convertGCLToGateControlEventList(List<GCL> gclList) {
        List<GCE> gateControlEventList = new ArrayList<>();
        for (GCL gcl : gclList) {
            double period = 500.0 / gcl.getFrequency();
            for (int i = 0; i < gcl.getFrequency(); i++) {
                double start = gcl.getOffset() + i * period;
                gateControlEventList.add(new GCE(start, start + gcl.getDuration()));
            }
        }

        gateControlEventList.sort((o1, o2) -> {
            Double g1 = o1.getStart();
            Double g2 = o2.getStart();
            return g1.compareTo(g2);
        });

        return gateControlEventList;
    }

    private List<GCE> mergeGateControlEventList(List<GCE> gateControlEventList) {
        List<GCE> finalGateControlEventList = new ArrayList<>();

        while (!gateControlEventList.isEmpty()) {
            GCE element = gateControlEventList.removeFirst();
            List<GCE> mergedGateControlEventList = new ArrayList<>();
            mergedGateControlEventList.add(element);

            for (GCE otherElement : new ArrayList<>(gateControlEventList)) {
                if (element.getStart() == otherElement.getStart()) {
                    mergedGateControlEventList.add(otherElement);
                    gateControlEventList.remove(otherElement);
                }
            }

            if (mergedGateControlEventList.size() > 1) {
                double totalDuration = 0;
                for (GCE gce : mergedGateControlEventList) {
                    totalDuration += gce.getEnd() - gce.getStart();
                }

                GCE newGateControlEvent = new GCE(mergedGateControlEventList.getFirst().getStart(), mergedGateControlEventList.getFirst().getStart() + totalDuration);
                finalGateControlEventList.add(newGateControlEvent);
            } else {
                finalGateControlEventList.add(element);
            }
        }

        return finalGateControlEventList;
    }

    private double getSlack(GCE next, GCE current) {
        if (next.getStart() < current.getEnd()) {
            //Add 500 to compensate for modulus effect
            return (next.getStart() + 500.0 - current.getEnd());
        } else {
            return next.getStart() - current.getEnd();
        }
    }

    private double getMaxInterference(double duration, List<GCE> mergedGateControlEvents) {
        double interferenceMax = 0;
        for (int i = 0; i < mergedGateControlEvents.size(); i++) {
            double interferenceCurrent = 0, remaining = duration;
            int index = i;
            while (remaining > 0) {
                interferenceCurrent += mergedGateControlEvents.get(index).getDuration();
                remaining -= getSlack(mergedGateControlEvents.get((index + 1) % mergedGateControlEvents.size()), mergedGateControlEvents.get(index));
                index = (index + 1) % mergedGateControlEvents.size();
            }
            if (interferenceCurrent > interferenceMax) {
                interferenceMax = interferenceCurrent;
            }
        }
        return interferenceMax;
    }
}
