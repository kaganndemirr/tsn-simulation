package ktu.kaganndemirr.util;

import ktu.kaganndemirr.application.SRTApplication;
import ktu.kaganndemirr.architecture.GCLEdge;
import ktu.kaganndemirr.message.Unicast;
import ktu.kaganndemirr.solver.Solution;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Constants {
    public static final String AVB_LATENCY_MATH_VERSION_TSNCF = "avbLatencyMathCF";
    public static final String AVB_LATENCY_MATH_VERSION_TSNRO = "avbLatencyMathTSNRO";

    public static final String INFO = "info";
    public static final String DEBUG = "debug";

    public static final String TSNCF = "TSNCF";
    public static final String TSNRO = "TSNRO";

    public static final int CLASS_A_PCP = 6;
    public static final String CLASS_A = "CLASS_A";

    public static final int ONE_BYTE_TO_BIT = 8;

    public static final int TT_PCP = 7;
    public static final String TT = "TT";

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

    public static final String SRT_UNICAST_CANDIDATE_SORTING_METHOD = "deadline";


}
