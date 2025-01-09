package ktu.kaganndemirr.util;

import ktu.kaganndemirr.application.SRTApplication;
import ktu.kaganndemirr.architecture.GCLEdge;
import ktu.kaganndemirr.message.Unicast;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Constants {
    public static final String AVB_LATENCY_MATH_VERSION_TSNCF = "avbLatencyMathTSNCF";
    public static final String AVB_LATENCY_MATH_VERSION_TSNRO = "avbLatencyMathTSNRO";
    public static final String NETWORK_CALCULUS = "networkCalculus";

    public static final String DEBUG = "debug";

    public static final String TSNCF = "TSNCF";
    public static final String TSNCF_V2 = "TSNCF_V2";
    public static final String TSN_TSNSCHED = "TSN_TSNSCHED";
    public static final String TSNNC = "TSNNC";

    public static HashMap<Integer, String> applicationTypeMap;

    static {
        applicationTypeMap = new HashMap<>();
        applicationTypeMap.put(1, "CLASS_F");
        applicationTypeMap.put(2, "CLASS_E");
        applicationTypeMap.put(3, "CLASS_D");
        applicationTypeMap.put(4, "CLASS_C");
        applicationTypeMap.put(5, "CLASS_B");
        applicationTypeMap.put(6, "CLASS_A");
        applicationTypeMap.put(7, "TT");
    }

    public static final int CLASS_A_PCP = 6;
    public static final int TT_PCP = 7;

    public static final int ONE_BYTE_TO_BIT = 8;



    public static final int TSN_CONFIGURATION_FRAMEWORK_CMI = 500;

    public static final int ONE_SECOND = 1_000_000;

    public static final double UNIT_WEIGHT = 1;

    public static final String SRT_TT = "srtTT";
    public static final String SRT_TT_LENGTH = "srtTTLength";
    public static final String SRT_TT_LENGTH_UTIL = "srtTTLengthUtil";

    public static final String WPM_VERSION_V1 = "v1";
    public static final String WPM_VERSION_V2 = "v2";

    public static final String ACTUAL = "actual";
    public static final String RELATIVE = "relative";

    public static final double NEW_COST = 2;

    public static final int WPM_THRESHOLD = 1;

    public static final String NO_SOLUTION_COULD_BE_FOUND = "No solution could be found!";

    public static double findAveragePathLengthIncludingES(List<Unicast> solution) {
        double total = 0;
        int size = 0;
        for (Unicast unicast : solution) {
            if (unicast.getApplication() instanceof SRTApplication) {
                total += unicast.getPath().getEdgeList().size();
                size++;
            }
        }

        return total / size;
    }

    public static double findAveragePathLengthWithoutES(List<Unicast> solution) {
        List<List<GCLEdge>> onlySwitchLinkList = solution.stream()
                .map(item -> item.getPath().getEdgeList().subList(1, item.getPath().getEdgeList().size() - 1)).toList();

        Map<Integer, Integer> lengthMap = new HashMap<>();

        for (int i = 0; i < onlySwitchLinkList.size(); i++) {
            List<GCLEdge> subList = onlySwitchLinkList.get(i);
            lengthMap.put(i, subList.size());
        }

        double total = 0;
        for (int i = 0; i < lengthMap.size(); i++) {
            total += lengthMap.get(i);
        }

        return total / lengthMap.size();
    }

    public static final int DEVICE_DELAY = 512;
    public static final int IPG = 12;
    public static final int SFD = 8;;

    public static final int MAX_BE_FRAME_BYTES = 1522;

    public static final String RANDOMIZE_WITH_HEADS_OR_TAILS_USING_SECURE_RANDOM = "headsOrTailsSecureRandom";

    public static final String RANDOMIZE_WITH_HEADS_OR_TAILS_USING_THREAD_LOCAL_RANDOM = "headsOrTailsThreadLocalRandom";

    public static final int PROGRESS_PERIOD_SECOND = 10_000;

    public static final String THREAD_LOCAL_RANDOM = "threadLocalRandom";

    public static final String GRASP = "GRASP";
    public static final String ALO = "ALO";
    public static final String CONSTRUCT_INITIAL_SOLUTION = "constructInitialSolution";

    public static final String MTR_V1 = "v1";
    public static final String MTR_AVERAGE = "average";

    public static final String MIN_MAX = "minMax";
    public static final String MAX = "max";
    public static final String VECTOR = "vector";


}
