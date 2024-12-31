package ktu.kaganndemirr.evaluator;

import ktu.kaganndemirr.message.Multicast;
import ktu.kaganndemirr.util.holders.*;

import java.util.Map;

public interface Cost {

    void reset();

    double getTotalCost();

    String toDetailedString();

    Map<Multicast, Double> getWCDMap();

    void writePHYWPMv1ResultToFile(PHYWPMv1Holder phyWPMv1Holder);

    void writePHYWPMv2ResultToFile(PHYWPMv2Holder phyWPv2MHolder);

    void writePHYWPMLWRv1ResultToFile(PHYWPMLWRv1Holder phyWPMLWRv1Holder);

    void writePHYWPMLWRv2ResultToFile(PHYWPMLWRv2Holder phyWPMLWRv2MHolder);

    void writePHYWPMCWRv1ResultToFile(PHYWPMCWRv1Holder phyWPMCWRv1Holder);

    void writePHYWPMCWRv2ResultToFile(PHYWPMCWRv2Holder phyWPMCWRv2MHolder);

    void writePHYWPMLWRCWRv1ResultToFile(PHYWPMLWRCWRv1Holder phyWPMLWRCWRv1Holder);

    void writePHYWPMLWRCWRv2ResultToFile(PHYWPMLWRCWRv2Holder phyWPMLWRCWRv2MHolder);

}
