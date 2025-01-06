package ktu.kaganndemirr.routing.mtr.yen;

import java.util.ArrayList;
import java.util.List;

public class HelperMethods {
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
