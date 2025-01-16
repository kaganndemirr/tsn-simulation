package ktu.kaganndemirr;

import ktu.kaganndemirr.application.Application;
import ktu.kaganndemirr.architecture.GCLEdge;
import ktu.kaganndemirr.architecture.Node;
import ktu.kaganndemirr.evaluator.*;
import ktu.kaganndemirr.output.OutputMethods;
import ktu.kaganndemirr.parser.ApplicationParser;
import ktu.kaganndemirr.parser.TopologyParser;
import ktu.kaganndemirr.routing.phy.yen.heuristic.WPMDeadline;
import ktu.kaganndemirr.routing.phy.yen.heuristic.WSM;
import ktu.kaganndemirr.routing.phy.yen.metaheuristic.*;
import ktu.kaganndemirr.solver.Solution;
import ktu.kaganndemirr.util.Bag;
import ktu.kaganndemirr.util.mcdm.MCDMConstants;
import org.apache.commons.cli.*;
import org.jgrapht.Graph;
import ktu.kaganndemirr.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ktu.kaganndemirr.util.HelperMethods.*;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class.getSimpleName());

    //region <Command line options>
    private static final String APP_ARG = "app";
    private static final String NET_ARG = "net";
    private static final String RATE_ARG = "rate";
    private static final String K_ARG = "k";
    private static final String THREAD_NUMBER_ARG = "threadNumber";
    private static final String TIMEOUT_ARG = "timeout";

    private static final String ROUTING_ARG = "routing";
    private static final String PATH_FINDING_METHOD_ARG = "pathFindingMethod";
    private static final String ALGORITHM_ARG = "algorithm";
    private static final String UNICAST_CANDIDATE_SORTING_METHOD_ARG = "unicastCandidateSortingMethod";

    private static final String WPM_OBJECTIVE_ARG = "mcdmObjective";
    private static final String W_SRT_ARG = "wSRT";
    private static final String W_TT_ARG = "wTT";
    private static final String W_LENGTH_ARG = "wLength";
    private static final String W_UTIL_ARG = "wUtil";
    private static final String LWR_ARG = "lwr";
    private static final String CWR_ARG = "cwr";
    private static final String WPM_VERSION_ARG = "wpmVersion";
    private static final String WPM_VALUE_TYPE_ARG = "wpmValueType";

    private static final String IDLE_SLOPE_ARG = "idleSlope";

    private static final String TSN_SIMULATION_VERSION_ARG = "tsnSimulationVersion";

    private static final String METAHEURISTIC_NAME_ARG = "metaheuristicName";

    private static final String MTR_NAME_ARG = "mtrName";

    private static final String WSM_NORMALIZATION_ARG = "wsmNormalization";
    private static final String MCDM_NAME_ARG = "mcdmName";
    //endregion

    public static void main(String[] args) {
        //region <Default Values>

        //Default value of rate (mbps)
        int rate = 1000;
        //Default Evaluator Name and Evaluator
        String evaluatorName = "avbLatencyMathTSNCF";
        Evaluator evaluator = new AVBLatencyMathTSNCF();
        //Default value of K
        int k = 50;
        //Default thread number
        int threadNumber = Runtime.getRuntime().availableProcessors();
        //Metaheuristic Algorithm Timeout
        int timeout = 60;

        //Default Routing
        String routing = "phy";
        //Default Method
        String pathFindingMethod = "yen";
        //Default Algorithm
        String algorithm = "LaursenRO";

        String unicastCandidateSortingMethod = "deadline";

        //Default MCDM Objective
        String mcdmObjective = "srtTTLength";
        //Default wSoftRealTime
        double wSRT = 1;
        //Default wTimeTriggered
        double wTT = 1;
        //Default wLength
        double wLength = 1;
        //Default wUtil
        double wUtil = 0;
        //Default linkWeightRandomization
        String lwr = "headsOrTailsThreadLocalRandom";
        //Default criteriaWeightRandomization
        String cwr = "threadLocalRandom";
        //Default weightedProductModelVersion
        String wpmVersion = "v1";
        //Default weightedProductModelValueType
        String wpmValueType = "actual";

        //Default IdleSlope
        double idleSlope = 0.75;

        //Default TSN Simulation Version
        String tsnSimulationVersion = "TSNCF";

        //Default Metaheuristic Name
        String metaheuristicName = "GRASP";

        //Default MTR Name
        String mtrName = "v1";

        //Default WSM Normalization
        String wsmNormalization = "max";

        //Default WSM Normalization
        String mcdmName = "wsm";

        //endregion

        //region <Options>
        Option architectureFile = Option.builder(NET_ARG).required().argName("file").hasArg().desc("Use given file as network").build();
        Option applicationFile = Option.builder(APP_ARG).required().argName("file").hasArg().desc("Use given file as application").build();

        Options options = new Options();
        options.addOption(applicationFile);
        options.addOption(architectureFile);
        options.addOption(RATE_ARG, true, "The rate in mbps (Type: mbps) (Default: 1000)");
        options.addOption(K_ARG, true, "Value of K for search-space reduction (Default: 50)");
        options.addOption(THREAD_NUMBER_ARG, true, "Thread number (Default: Number of Processor Thread)");
        options.addOption(TIMEOUT_ARG, true, "Metaheuristic algorithm timeout (Type: Second) (Default: 60");

        options.addOption(ROUTING_ARG, true, "Choose routing (Default: phy) (Choices: phy, mtr)");
        options.addOption(PATH_FINDING_METHOD_ARG, true, "Choose path finding method (Default = yen) (Choices: shortestPath, yen, pathPenalization)");
        options.addOption(ALGORITHM_ARG, true, "Choose algorithm (Default = GRASP) (Choices: GRASP, ALO)");
        options.addOption(UNICAST_CANDIDATE_SORTING_METHOD_ARG, true, "Unicast Candidate Sorting Method (Default = deadline) (Choices: deadline, deadline/size/period)");

        options.addOption(WPM_OBJECTIVE_ARG, true, "Weighted Product Model Objective (Default: srtTTLengthForCandidatePathComputing) (Choices: srtTT, srtTTLengthForCandidatePathComputing, srtTTLengthUtil)");
        options.addOption(W_SRT_ARG, true, "SRT Weight (Default: 0");
        options.addOption(W_TT_ARG, true, "TT Weight (Default: 0");
        options.addOption(W_LENGTH_ARG, true, "Candidate Path Length Weight (Default: 0");
        options.addOption(W_UTIL_ARG, true, "Utilization Weight (Default: 0");

        options.addOption(LWR_ARG, true, "Link weight randomization method (Default: headsOrTails)");
        options.addOption(CWR_ARG, true, "Criteria weight randomization method (Default: secureRandom)");
        options.addOption(WPM_VERSION_ARG, true, "WPM Version (Default: v1) (Choices: v1, v2)");
        options.addOption(WPM_VALUE_TYPE_ARG, true, "WPM Value Type for v2 (Default: actual) (Choices: actual, relative");

        options.addOption(IDLE_SLOPE_ARG, true, "Idle slope for CLASS_A (Default: 0.75)");

        options.addOption(TSN_SIMULATION_VERSION_ARG, true, "TSN Simulation Version (Default: TSNCF) (Choices: TSNCF, TSNCFV2, TSNTSNSCHED, TSNNC)");

        options.addOption(METAHEURISTIC_NAME_ARG, true, "Which metaheuristic runs (Default: GRASP) (Choices: GRASP, ALO)");

        options.addOption(MTR_NAME_ARG, true, "MTR Versions (Default: v1) (Choices: v1, Average)");

        options.addOption(WSM_NORMALIZATION_ARG, true, "WSM Normalization Versions (Default: max) (Choices: max, minMax, vector)");

        options.addOption(MCDM_NAME_ARG, true, "MCDM Versions (Default: wsm) (Choices: wsm, wpm, vector)");


        //endregion

        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine line = parser.parse(options, args);

            //region <Set Values>

            File net = new File(line.getOptionValue(NET_ARG));
            File app = new File(line.getOptionValue(APP_ARG));

            //Set rate
            if (line.hasOption(RATE_ARG)) {
                rate = Integer.parseInt(line.getOptionValue(RATE_ARG));
            }

            if (Objects.equals(line.getOptionValue(TSN_SIMULATION_VERSION_ARG), Constants.TSNCF)) {
                evaluatorName = Constants.AVB_LATENCY_MATH_TSNCF;
                evaluator = new AVBLatencyMathTSNCF();
            } else if (Objects.equals(line.getOptionValue(TSN_SIMULATION_VERSION_ARG), Constants.TSNCF_V2)) {
                evaluatorName = Constants.AVB_LATENCY_MATH_TSNCF_V2;
                evaluator = new AVBLatencyMathTSNCFV2();
            } else if (Objects.equals(line.getOptionValue(TSN_SIMULATION_VERSION_ARG), Constants.TSN_TSNSCHED)) {
                evaluatorName = Constants.AVB_LATENCY_MATH_TSNCF_V2;
                evaluator = new AVBLatencyMathTSNCFV2();
            } else if (Objects.equals(line.getOptionValue(TSN_SIMULATION_VERSION_ARG), Constants.TSN_NC)) {
                evaluatorName = Constants.EVALUATOR_TSN_NC;
                evaluator = new NetworkCalculus();
            }

            //Set K
            if (line.hasOption(K_ARG)) {
                k = Integer.parseInt(line.getOptionValue(K_ARG));
            }

            //Set Thread Number
            if (line.hasOption(THREAD_NUMBER_ARG)) {
                threadNumber = Integer.parseInt(line.getOptionValue(THREAD_NUMBER_ARG));
            }

            //Set Timeout
            if (line.hasOption(TIMEOUT_ARG)) {
                timeout = Integer.parseInt(line.getOptionValue(TIMEOUT_ARG));
            }

            if (line.hasOption(ROUTING_ARG)) {
                routing = line.getOptionValue(ROUTING_ARG);
            }

            if (line.hasOption(PATH_FINDING_METHOD_ARG)) {
                pathFindingMethod = line.getOptionValue(PATH_FINDING_METHOD_ARG);
            }

            if (line.hasOption(ALGORITHM_ARG)) {
                algorithm = line.getOptionValue(ALGORITHM_ARG);
            }

            if (line.hasOption(UNICAST_CANDIDATE_SORTING_METHOD_ARG)) {
                unicastCandidateSortingMethod = line.getOptionValue(UNICAST_CANDIDATE_SORTING_METHOD_ARG);
            }

            if (line.hasOption(WPM_OBJECTIVE_ARG)) {
                mcdmObjective = line.getOptionValue(WPM_OBJECTIVE_ARG);
            }

            if (line.hasOption(W_SRT_ARG)) {
                wSRT = Double.parseDouble(line.getOptionValue(W_SRT_ARG));
            }

            if (line.hasOption(W_TT_ARG)) {
                wTT = Double.parseDouble(line.getOptionValue(W_TT_ARG));
            }

            if (line.hasOption(W_LENGTH_ARG)) {
                wLength = Double.parseDouble(line.getOptionValue(W_LENGTH_ARG));
            }

            if (line.hasOption(W_UTIL_ARG)) {
                wUtil = Double.parseDouble(line.getOptionValue(W_UTIL_ARG));
            }

            if (line.hasOption(LWR_ARG)) {
                lwr = line.getOptionValue(LWR_ARG);
            }

            if (line.hasOption(CWR_ARG)) {
                cwr = line.getOptionValue(CWR_ARG);
            }

            if (line.hasOption(WPM_VERSION_ARG)) {
                wpmVersion = line.getOptionValue(WPM_VERSION_ARG);
            }

            if (line.hasOption(WPM_VALUE_TYPE_ARG)) {
                wpmValueType = line.getOptionValue(WPM_VALUE_TYPE_ARG);
            }

            if (line.hasOption(IDLE_SLOPE_ARG)) {
                idleSlope = Double.parseDouble(line.getOptionValue(IDLE_SLOPE_ARG));
            }

            if (line.hasOption(TSN_SIMULATION_VERSION_ARG)) {
                tsnSimulationVersion = line.getOptionValue(TSN_SIMULATION_VERSION_ARG);
            }

            if (line.hasOption(METAHEURISTIC_NAME_ARG)) {
                metaheuristicName = line.getOptionValue(METAHEURISTIC_NAME_ARG);
            }

            if (line.hasOption(MTR_NAME_ARG)) {
                mtrName = line.getOptionValue(MTR_NAME_ARG);
            }

            if (line.hasOption(WSM_NORMALIZATION_ARG)) {
                wsmNormalization = line.getOptionValue(WSM_NORMALIZATION_ARG);
            }

            if (line.hasOption(MCDM_NAME_ARG)) {
                mcdmName = line.getOptionValue(MCDM_NAME_ARG);
            }

            //endregion

            //Parse Topology
            logger.info("Parsing Topology from {}", net.getName());
            Graph<Node, GCLEdge> graph = TopologyParser.parse(net, rate, idleSlope);
            logger.info("Topology parsed!");
            //endregion

            //Parse Applications
            logger.info("Parsing application set from {}", app.getName());
            List<Application> applicationList = ApplicationParser.parse(app, tsnSimulationVersion, rate, graph);
            logger.info("Applications parsed! ");
            //endregion

            //region <Parse Topology Name>
            String applicationName = null;
            String topologyName = null;

            //region Parse BRITE Topology Name
            if (countUnderscores(net.getName()) > 0) {
                Pattern patternTopology = Pattern.compile(".*?_(.*?)_[^_]*$");
                Matcher matcherTopology = patternTopology.matcher(net.getName());
                if (matcherTopology.find()) {
                    topologyName = matcherTopology.group(1);
                }

                Pattern patternApplication = Pattern.compile(".*_([^_]*_\\d+)$");
                Matcher matcherApplication = patternApplication.matcher(app.getName());
                if (matcherApplication.find()) {
                    applicationName = matcherApplication.group(1);
                }

            }
            //endregion

            //region Parse Classic Topology Name
            else {
                Pattern patternTopology = Pattern.compile("(.+?)(?=\\.xml)");
                Matcher matcherTopology = patternTopology.matcher(net.getName());
                if (matcherTopology.find()) {
                    topologyName = matcherTopology.group(1);
                }

                Pattern patternApplication = Pattern.compile("(?<=_)(.*?)(?=\\.xml)");
                Matcher matcherApplication = patternApplication.matcher(app.getName());
                if (matcherApplication.find()) {
                    applicationName = matcherApplication.group(1);
                }
            }
            //endregion

            //region <Set Routing>
            switch (routing) {
                case "phy" -> {
                    switch (pathFindingMethod) {
                        case "yen" -> {
                            switch (algorithm) {
                                case "LaursenRO" -> {
                                    Bag bag = new Bag();
                                    bag.setGraph(graph);
                                    bag.setApplicationList(applicationList);
                                    bag.setTopologyName(topologyName);
                                    bag.setApplicationName(applicationName);
                                    bag.setRouting(routing);
                                    bag.setPathFindingMethod(pathFindingMethod);
                                    bag.setAlgorithm(algorithm);
                                    bag.setK(k);
                                    bag.setThreadNumber(threadNumber);
                                    bag.setTimeout(timeout);
                                    bag.setMetaheuristicName(metaheuristicName);
                                    bag.setEvaluator(evaluator);
                                    bag.setEvaluatorName(evaluatorName);

                                    if (evaluatorName.equals(Constants.EVALUATOR_TSN_NC)){
                                        createGCLSynthesisAndNetworkCalculusDirectories(bag);
                                    }

                                    LaursenRO laursenRO = new LaursenRO(k);

                                    logger.info(createInfo(bag));

                                    Solution solution = laursenRO.solve(bag);

                                    solution.getCost().writeResultToFile(bag);

                                    if (solution.getMulticastList() == null || solution.getMulticastList().isEmpty()) {
                                        logger.info(Constants.NO_SOLUTION_COULD_BE_FOUND);
                                    } else {
                                        if (solution.getCost().getTotalCost() == Double.MAX_VALUE) {
                                            logger.info(createFoundNoSolutionString(solution));
                                        } else {
                                            logger.info(createFoundSolutionString(solution));

                                            new OutputMethods(bag, laursenRO.getSolution(), solution.getCost().getWCDMap(), graph, rate, laursenRO.getDurationMap(), laursenRO.getSRTUnicastCandidateList());
                                        }
                                    }
                                }
                                case "WSM" -> {
                                    WSM wsm = new WSM();

                                    Bag bag = new Bag();
                                    bag.setGraph(graph);
                                    bag.setApplicationList(applicationList);
                                    bag.setTopologyName(topologyName);
                                    bag.setApplicationName(applicationName);
                                    bag.setRouting(routing);
                                    bag.setPathFindingMethod(pathFindingMethod);
                                    bag.setAlgorithm(algorithm);
                                    bag.setUnicastCandidateSortingMethod(unicastCandidateSortingMethod);
                                    bag.setK(k);
                                    bag.setMCDMName(mcdmName);
                                    bag.setMCDMObjective(mcdmObjective);
                                    bag.setWSMNormalization(wsmNormalization);
                                    bag.setWSRT(wSRT);
                                    bag.setWTT(wTT);
                                    bag.setWLength(wLength);
                                    bag.setWUtil(wUtil);
                                    bag.setEvaluator(evaluator);
                                    bag.setEvaluatorName(evaluatorName);

                                    logger.info(createInfo(bag));

                                    Solution solution = wsm.solve(bag);

                                    solution.getCost().writeResultToFile(bag);

                                    if (solution.getMulticastList() == null || solution.getMulticastList().isEmpty()) {
                                        logger.info(Constants.NO_SOLUTION_COULD_BE_FOUND);
                                    } else {
                                        if (solution.getCost().getTotalCost() == Double.MAX_VALUE) {
                                            logger.info(createFoundNoSolutionString(solution));
                                        } else {
                                            logger.info(createFoundSolutionString(solution));

                                            new OutputMethods(bag, wsm.getSolution(), solution.getCost().getWCDMap(), graph, rate, wsm.getDurationMap(), wsm.getSRTUnicastCandidateList());


                                        }
                                    }
                                }
                                case "WSMLWR" -> {
                                    WSMLWR wsmLWR = new WSMLWR();

                                    Bag bag = new Bag();
                                    bag.setGraph(graph);
                                    bag.setApplicationList(applicationList);
                                    bag.setTopologyName(topologyName);
                                    bag.setApplicationName(applicationName);
                                    bag.setRouting(routing);
                                    bag.setPathFindingMethod(pathFindingMethod);
                                    bag.setAlgorithm(algorithm);
                                    bag.setUnicastCandidateSortingMethod(unicastCandidateSortingMethod);
                                    bag.setLWR(lwr);
                                    bag.setK(k);
                                    bag.setMCDMName(mcdmName);
                                    bag.setMCDMObjective(mcdmObjective);
                                    bag.setWSMNormalization(wsmNormalization);
                                    bag.setWSRT(wSRT);
                                    bag.setWTT(wTT);
                                    bag.setWLength(wLength);
                                    bag.setWUtil(wUtil);
                                    bag.setThreadNumber(threadNumber);
                                    bag.setTimeout(timeout);
                                    bag.setMetaheuristicName(metaheuristicName);
                                    bag.setEvaluator(evaluator);
                                    bag.setEvaluatorName(evaluatorName);

                                    logger.info(createInfo(bag));

                                    Solution solution = wsmLWR.solve(bag);

                                    solution.getCost().writeResultToFile(bag);

                                    if (solution.getMulticastList() == null || solution.getMulticastList().isEmpty()) {
                                        logger.info(Constants.NO_SOLUTION_COULD_BE_FOUND);
                                    } else {
                                        if (solution.getCost().getTotalCost() == Double.MAX_VALUE) {
                                            logger.info(createFoundNoSolutionString(solution));
                                        } else {
                                            logger.info(createFoundSolutionString(solution));

                                            new OutputMethods(bag, wsmLWR.getSolution(), solution.getCost().getWCDMap(), graph, rate, wsmLWR.getDurationMap(), wsmLWR.getSRTUnicastCandidateList());

                                        }
                                    }
                                }
                                case "WSMCWR" -> {
                                    WSMCWR wsmCWR = new WSMCWR();

                                    Bag bag = new Bag();
                                    bag.setGraph(graph);
                                    bag.setApplicationList(applicationList);
                                    bag.setTopologyName(topologyName);
                                    bag.setApplicationName(applicationName);
                                    bag.setRouting(routing);
                                    bag.setPathFindingMethod(pathFindingMethod);
                                    bag.setAlgorithm(algorithm);
                                    bag.setUnicastCandidateSortingMethod(unicastCandidateSortingMethod);
                                    bag.setK(k);
                                    bag.setMCDMName(mcdmName);
                                    bag.setMCDMObjective(mcdmObjective);
                                    bag.setWSMNormalization(wsmNormalization);
                                    bag.setCWR(cwr);
                                    bag.setThreadNumber(threadNumber);
                                    bag.setTimeout(timeout);
                                    bag.setMetaheuristicName(metaheuristicName);
                                    bag.setEvaluator(evaluator);
                                    bag.setEvaluatorName(evaluatorName);

                                    logger.info(createInfo(bag));

                                    Solution solution = wsmCWR.solve(bag);

                                    solution.getCost().writeResultToFile(bag);

                                    if (solution.getMulticastList() == null || solution.getMulticastList().isEmpty()) {
                                        logger.info(Constants.NO_SOLUTION_COULD_BE_FOUND);
                                    } else {
                                        if (solution.getCost().getTotalCost() == Double.MAX_VALUE) {
                                            logger.info(createFoundNoSolutionString(solution));
                                        } else {
                                            logger.info(createFoundSolutionString(solution));

                                            new OutputMethods(bag, wsmCWR.getSolution(), solution.getCost().getWCDMap(), graph, rate, wsmCWR.getDurationMap(), wsmCWR.getSRTUnicastCandidateList());

                                        }
                                    }
                                }
                                default -> throw new InputMismatchException("Aborting: " + routing + ", " + pathFindingMethod + ", " + algorithm + " unrecognized!");
                            }
                        }
                        case "pathPenalization" -> {
                            switch (algorithm) {
                                case "LaursenRO" -> {
                                    Bag bag = new Bag();
                                    bag.setGraph(graph);
                                    bag.setApplicationList(applicationList);
                                    bag.setTopologyName(topologyName);
                                    bag.setApplicationName(applicationName);
                                    bag.setRouting(routing);
                                    bag.setPathFindingMethod(pathFindingMethod);
                                    bag.setAlgorithm(algorithm);
                                    bag.setK(k);
                                    bag.setThreadNumber(threadNumber);
                                    bag.setTimeout(timeout);
                                    bag.setMetaheuristicName(metaheuristicName);
                                    bag.setEvaluator(evaluator);
                                    bag.setEvaluatorName(evaluatorName);

                                    if (evaluatorName.equals(Constants.EVALUATOR_TSN_NC)){
                                        createGCLSynthesisAndNetworkCalculusDirectories(bag);
                                    }

                                    LaursenRO laursenRO = new LaursenRO(k);

                                    logger.info(createInfo(bag));

                                    Solution solution = laursenRO.solve(bag);

                                    solution.getCost().writeResultToFile(bag);

                                    if (solution.getMulticastList() == null || solution.getMulticastList().isEmpty()) {
                                        logger.info(Constants.NO_SOLUTION_COULD_BE_FOUND);
                                    } else {
                                        if (solution.getCost().getTotalCost() == Double.MAX_VALUE) {
                                            logger.info(createFoundNoSolutionString(solution));
                                        } else {
                                            logger.info(createFoundSolutionString(solution));

                                            new OutputMethods(bag, laursenRO.getSolution(), solution.getCost().getWCDMap(), graph, rate, laursenRO.getDurationMap(), laursenRO.getSRTUnicastCandidateList());
                                        }
                                    }
                                }
                                case "WSM" -> {
                                    ktu.kaganndemirr.routing.phy.pathpenalization.heuristic.WSM wsm = new ktu.kaganndemirr.routing.phy.pathpenalization.heuristic.WSM();

                                    Bag bag = new Bag();
                                    bag.setGraph(graph);
                                    bag.setApplicationList(applicationList);
                                    bag.setTopologyName(topologyName);
                                    bag.setApplicationName(applicationName);
                                    bag.setRouting(routing);
                                    bag.setPathFindingMethod(pathFindingMethod);
                                    bag.setAlgorithm(algorithm);
                                    bag.setUnicastCandidateSortingMethod(unicastCandidateSortingMethod);
                                    bag.setK(k);
                                    bag.setMCDMName(mcdmName);
                                    bag.setMCDMObjective(mcdmObjective);
                                    bag.setWSMNormalization(wsmNormalization);
                                    bag.setWSRT(wSRT);
                                    bag.setWTT(wTT);
                                    bag.setWLength(wLength);
                                    bag.setWUtil(wUtil);
                                    bag.setEvaluator(evaluator);
                                    bag.setEvaluatorName(evaluatorName);

                                    logger.info(createInfo(bag));

                                    Solution solution = wsm.solve(bag);

                                    solution.getCost().writeResultToFile(bag);

                                    if (solution.getMulticastList() == null || solution.getMulticastList().isEmpty()) {
                                        logger.info(Constants.NO_SOLUTION_COULD_BE_FOUND);
                                    } else {
                                        if (solution.getCost().getTotalCost() == Double.MAX_VALUE) {
                                            logger.info(createFoundNoSolutionString(solution));
                                        } else {
                                            logger.info(createFoundSolutionString(solution));

                                            new OutputMethods(bag, wsm.getSolution(), solution.getCost().getWCDMap(), graph, rate, wsm.getDurationMap(), wsm.getSRTUnicastCandidateList());


                                        }
                                    }
                                }
                                case "WSMLWR" -> {
                                    ktu.kaganndemirr.routing.phy.pathpenalization.metaheuristic.WSMLWR wsmLWR = new ktu.kaganndemirr.routing.phy.pathpenalization.metaheuristic.WSMLWR();

                                    Bag bag = new Bag();
                                    bag.setGraph(graph);
                                    bag.setApplicationList(applicationList);
                                    bag.setTopologyName(topologyName);
                                    bag.setApplicationName(applicationName);
                                    bag.setRouting(routing);
                                    bag.setPathFindingMethod(pathFindingMethod);
                                    bag.setAlgorithm(algorithm);
                                    bag.setUnicastCandidateSortingMethod(unicastCandidateSortingMethod);
                                    bag.setLWR(lwr);
                                    bag.setK(k);
                                    bag.setMCDMName(mcdmName);
                                    bag.setMCDMObjective(mcdmObjective);
                                    bag.setWSMNormalization(wsmNormalization);
                                    bag.setWSRT(wSRT);
                                    bag.setWTT(wTT);
                                    bag.setWLength(wLength);
                                    bag.setWUtil(wUtil);
                                    bag.setThreadNumber(threadNumber);
                                    bag.setTimeout(timeout);
                                    bag.setMetaheuristicName(metaheuristicName);
                                    bag.setEvaluator(evaluator);
                                    bag.setEvaluatorName(evaluatorName);

                                    logger.info(createInfo(bag));

                                    Solution solution = wsmLWR.solve(bag);

                                    solution.getCost().writeResultToFile(bag);

                                    if (solution.getMulticastList() == null || solution.getMulticastList().isEmpty()) {
                                        logger.info(Constants.NO_SOLUTION_COULD_BE_FOUND);
                                    } else {
                                        if (solution.getCost().getTotalCost() == Double.MAX_VALUE) {
                                            logger.info(createFoundNoSolutionString(solution));
                                        } else {
                                            logger.info(createFoundSolutionString(solution));

                                            new OutputMethods(bag, wsmLWR.getSolution(), solution.getCost().getWCDMap(), graph, rate, wsmLWR.getDurationMap(), wsmLWR.getSRTUnicastCandidateList());

                                        }
                                    }
                                }
                                case "WSMCWR" -> {
                                    ktu.kaganndemirr.routing.phy.pathpenalization.metaheuristic.WSMCWR wsmCWR = new ktu.kaganndemirr.routing.phy.pathpenalization.metaheuristic.WSMCWR();

                                    Bag bag = new Bag();
                                    bag.setGraph(graph);
                                    bag.setApplicationList(applicationList);
                                    bag.setTopologyName(topologyName);
                                    bag.setApplicationName(applicationName);
                                    bag.setRouting(routing);
                                    bag.setPathFindingMethod(pathFindingMethod);
                                    bag.setAlgorithm(algorithm);
                                    bag.setUnicastCandidateSortingMethod(unicastCandidateSortingMethod);
                                    bag.setK(k);
                                    bag.setMCDMName(mcdmName);
                                    bag.setMCDMObjective(mcdmObjective);
                                    bag.setWSMNormalization(wsmNormalization);
                                    bag.setCWR(cwr);
                                    bag.setThreadNumber(threadNumber);
                                    bag.setTimeout(timeout);
                                    bag.setMetaheuristicName(metaheuristicName);
                                    bag.setEvaluator(evaluator);
                                    bag.setEvaluatorName(evaluatorName);

                                    logger.info(createInfo(bag));

                                    Solution solution = wsmCWR.solve(bag);

                                    solution.getCost().writeResultToFile(bag);

                                    if (solution.getMulticastList() == null || solution.getMulticastList().isEmpty()) {
                                        logger.info(Constants.NO_SOLUTION_COULD_BE_FOUND);
                                    } else {
                                        if (solution.getCost().getTotalCost() == Double.MAX_VALUE) {
                                            logger.info(createFoundNoSolutionString(solution));
                                        } else {
                                            logger.info(createFoundSolutionString(solution));

                                            new OutputMethods(bag, wsmCWR.getSolution(), solution.getCost().getWCDMap(), graph, rate, wsmCWR.getDurationMap(), wsmCWR.getSRTUnicastCandidateList());

                                        }
                                    }
                                }
                                case "WPM" -> {
                                    ktu.kaganndemirr.routing.phy.pathpenalization.heuristic.WPM wpm = new ktu.kaganndemirr.routing.phy.pathpenalization.heuristic.WPM();

                                    Bag bag = new Bag();
                                    bag.setGraph(graph);
                                    bag.setApplicationList(applicationList);
                                    bag.setTopologyName(topologyName);
                                    bag.setApplicationName(applicationName);
                                    bag.setRouting(routing);
                                    bag.setPathFindingMethod(pathFindingMethod);
                                    bag.setAlgorithm(algorithm);
                                    bag.setUnicastCandidateSortingMethod(unicastCandidateSortingMethod);
                                    bag.setK(k);
                                    bag.setMCDMName(mcdmName);
                                    bag.setMCDMObjective(mcdmObjective);
                                    bag.setWSRT(wSRT);
                                    bag.setWTT(wTT);
                                    bag.setWLength(wLength);
                                    bag.setWUtil(wUtil);
                                    bag.setWPMVersion(wpmVersion);
                                    bag.setEvaluator(evaluator);
                                    bag.setEvaluatorName(evaluatorName);

                                    if(Objects.equals(bag.getWPMVersion(), MCDMConstants.WPM_VERSION_V2)){
                                        bag.setWPMValueType(wpmValueType);
                                    }

                                    logger.info(createInfo(bag));

                                    Solution solution = wpm.solve(bag);

                                    solution.getCost().writeResultToFile(bag);

                                    if (solution.getMulticastList() == null || solution.getMulticastList().isEmpty()) {
                                        logger.info(Constants.NO_SOLUTION_COULD_BE_FOUND);
                                    } else {
                                        if (solution.getCost().getTotalCost() == Double.MAX_VALUE) {
                                            logger.info(createFoundNoSolutionString(solution));
                                        } else {
                                            logger.info(createFoundSolutionString(solution));

                                            new OutputMethods(bag, wpm.getSolution(), solution.getCost().getWCDMap(), graph, rate, wpm.getDurationMap(), wpm.getSRTUnicastCandidateList());


                                        }
                                    }
                                }
                                case "WPMLWR" -> {
                                    ktu.kaganndemirr.routing.phy.pathpenalization.metaheuristic.WPMLWR wpmLWR = new ktu.kaganndemirr.routing.phy.pathpenalization.metaheuristic.WPMLWR();

                                    Bag bag = new Bag();
                                    bag.setGraph(graph);
                                    bag.setApplicationList(applicationList);
                                    bag.setTopologyName(topologyName);
                                    bag.setApplicationName(applicationName);
                                    bag.setRouting(routing);
                                    bag.setPathFindingMethod(pathFindingMethod);
                                    bag.setAlgorithm(algorithm);
                                    bag.setUnicastCandidateSortingMethod(unicastCandidateSortingMethod);
                                    bag.setLWR(lwr);
                                    bag.setK(k);
                                    bag.setMCDMName(mcdmName);
                                    bag.setMCDMObjective(mcdmObjective);
                                    bag.setWSRT(wSRT);
                                    bag.setWTT(wTT);
                                    bag.setWLength(wLength);
                                    bag.setWUtil(wUtil);
                                    bag.setThreadNumber(threadNumber);
                                    bag.setTimeout(timeout);
                                    bag.setMetaheuristicName(metaheuristicName);
                                    bag.setEvaluator(evaluator);
                                    bag.setEvaluatorName(evaluatorName);
                                    bag.setEvaluatorName(evaluatorName);

                                    if(Objects.equals(bag.getWPMVersion(), MCDMConstants.WPM_VERSION_V2)){
                                        bag.setWPMValueType(wpmValueType);
                                    }

                                    logger.info(createInfo(bag));

                                    Solution solution = wpmLWR.solve(bag);

                                    solution.getCost().writeResultToFile(bag);

                                    if (solution.getMulticastList() == null || solution.getMulticastList().isEmpty()) {
                                        logger.info(Constants.NO_SOLUTION_COULD_BE_FOUND);
                                    } else {
                                        if (solution.getCost().getTotalCost() == Double.MAX_VALUE) {
                                            logger.info(createFoundNoSolutionString(solution));
                                        } else {
                                            logger.info(createFoundSolutionString(solution));

                                            new OutputMethods(bag, wpmLWR.getSolution(), solution.getCost().getWCDMap(), graph, rate, wpmLWR.getDurationMap(), wpmLWR.getSRTUnicastCandidateList());

                                        }
                                    }
                                }
                                default -> throw new InputMismatchException("Aborting: " + routing + ", " + pathFindingMethod + ", " + algorithm + " unrecognized!");
                            }
                        }
                        default -> throw new InputMismatchException("Aborting: Solver " + routing + ", " + pathFindingMethod + " unrecognized!");
                    }
                }
                default -> throw new InputMismatchException("Aborting: " + routing + " unrecognized!");
            }
            //endregion

        } catch (ParseException | IOException pe) {
            throw new RuntimeException(pe);
        }
    }

    public static int countUnderscores(String str) {
        int count = 0;
        for (char c : str.toCharArray()) {
            if (c == '_') {
                count++;
            }
        }
        return count;
    }
}