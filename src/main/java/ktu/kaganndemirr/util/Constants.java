package ktu.kaganndemirr.util;

import java.util.HashMap;

public class Constants {
    public static final String TSNCF = "tsncf";
    public static final String TSN_TSNSCHED = "tsnsched";

    public static final String AVB_LATENCY_MATH_V1= "avbLatencyMathV1";
    public static final String AVB_LATENCY_MATH_V2 = "avbLatencyMathV2";

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

    public static final String PHY = "phy";
    public static final String MTR = "mtr";
    public static final String YEN = "yen";
    public static final String PATH_PENALIZATION = "pathPenalization";
    public static final String WSM = "wsm";
    public static final String WPM = "wpm";
    public static final String MTR_V1 = "v1";
    public static final String MTR_AVERAGE = "average";
    public static final String MTR_HIERARCHICAL = "hierarchical";
    public static final String MTR_KMEANS= "kmeans";
    public static final String SHORTEST_PATH = "shortestPath";
    public static final String DIJKSTRA = "dijkstra";

    public static final int MTR_V1_VT_NUMBER = 1;
    public static final int MTR_AVERAGE_VT_NUMBER = 2;

    public static final int CLASS_A_PCP = 6;
    public static final int TT_PCP = 7;

    public static final int ONE_BYTE_TO_BIT = 8;



    public static final int TSNCF_TT_CMI = 500;

    public static final int ONE_SECOND = 1_000_000;

    public static final double UNIT_WEIGHT = 1;

    public static final String SRT_TT = "srtTT";
    public static final String SRT_TT_LENGTH = "srtTTLength";
    public static final String SRT_TT_LENGTH_UTIL = "srtTTLengthUtil";

    public static final String NO_SOLUTION_COULD_BE_FOUND = "No solution could be found!";

    public static final int DEVICE_DELAY = 512;
    public static final int IPG = 12;
    public static final int SFD = 8;;

    public static final int MAX_BE_FRAME_BYTES = 1522;



    public static final int PROGRESS_PERIOD_SECOND = 10_000;

    public static final String GRASP = "GRASP";
    public static final String ALO = "ALO";
    public static final String CONSTRUCT_INITIAL_SOLUTION = "constructInitialSolution";






}
