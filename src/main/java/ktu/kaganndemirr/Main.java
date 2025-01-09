package ktu.kaganndemirr;

import ktu.kaganndemirr.application.Application;
import ktu.kaganndemirr.architecture.GCLEdge;
import ktu.kaganndemirr.architecture.Node;
import ktu.kaganndemirr.evaluator.*;
import ktu.kaganndemirr.output.OutputMethods;
import ktu.kaganndemirr.parser.ApplicationParser;
import ktu.kaganndemirr.parser.TopologyParser;
import ktu.kaganndemirr.routing.phy.yen.heuristic.WPMDeadline;
import ktu.kaganndemirr.routing.phy.yen.metaheuristic.*;
import ktu.kaganndemirr.solver.Solution;
import ktu.kaganndemirr.util.Bag;
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

    //region <Command line options>
    private static final String APP_ARG = "app";
    private static final String NET_ARG = "net";
    private static final String RATE_ARG = "rate";
    private static final String EVALUATOR_ARG = "evaluator";
    private static final String K_ARG = "k";
    private static final String THREAD_NUMBER_ARG = "threadNumber";
    private static final String TIMEOUT_ARG = "timeout";

    private static final String ROUTING_ARG = "routing";
    private static final String PATH_FINDING_METHOD_ARG = "pathFindingMethod";
    private static final String ALGORITHM_ARG = "algorithm";

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

    private static final String LOG_ARG = "log";

    private static final String TSN_SIMULATION_VERSION_ARG = "tsnSimulationVersion";

    private static final String METAHEURISTIC_NAME_ARG = "metaheuristicName";

    private static final String MTR_NAME_ARG = "mtrName";

    private static final String WSM_NORMALIZATION_ARG = "wsmNormalization";
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
        String algorithm = "GRASP";

        //Default weightedProductModelObjective
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

        //Default Log
        String log = null;

        //Default TSN Simulation Version
        String tsnSimulationVersion = "TSNCF";

        //Default Metaheuristic Name
        String metaheuristicName = "GRASP";

        //Default MTR Name
        String mtrName = "v1";

        //Default MTR Name
        String wsmNormalization = "max";

        //endregion

        //region <Options>
        Option architectureFile = Option.builder(NET_ARG).required().argName("file").hasArg().desc("Use given file as network").build();
        Option applicationFile = Option.builder(APP_ARG).required().argName("file").hasArg().desc("Use given file as application").build();

        Options options = new Options();
        options.addOption(applicationFile);
        options.addOption(architectureFile);
        options.addOption(RATE_ARG, true, "The rate in mbps (Type: mbps) (Default: 1000)");
        options.addOption(EVALUATOR_ARG, true, "WCD Analysis Method (Default: avbLatencyMathTSNCF) (Choices: avbLatencyMathTSNCF, avbLatencyMathTSNRO, networkCalculus)");
        options.addOption(K_ARG, true, "Value of K for search-space reduction (Default: 50)");
        options.addOption(THREAD_NUMBER_ARG, true, "Thread number (Default: Number of Processor Thread)");
        options.addOption(TIMEOUT_ARG, true, "Metaheuristic algorithm timeout (Type: Second) (Default: 60");

        options.addOption(ROUTING_ARG, true, "Choose routing (Default: phy) (Choices: phy, mtr)");
        options.addOption(PATH_FINDING_METHOD_ARG, true, "Choose path finding method (Default = yen) (Choices: shortestPath, yen, pathPenalization)");
        options.addOption(ALGORITHM_ARG, true, "Choose algorithm (Default = GRASP) (Choices: GRASP, ALO)");

        options.addOption(WPM_OBJECTIVE_ARG, true, "Weighted Product Model Objective (Default: srtTTLength) (Choices: srtTT, srtTTLength, srtTTLengthUtil)");
        options.addOption(W_SRT_ARG, true, "SRT Weight (Default: 0");
        options.addOption(W_TT_ARG, true, "TT Weight (Default: 0");
        options.addOption(W_LENGTH_ARG, true, "Candidate Path Length Weight (Default: 0");
        options.addOption(W_UTIL_ARG, true, "Utilization Weight (Default: 0");

        options.addOption(LWR_ARG, true, "Link weight randomization method (Default: headsOrTails)");
        options.addOption(CWR_ARG, true, "Criteria weight randomization method (Default: secureRandom)");
        options.addOption(WPM_VERSION_ARG, true, "Weighted Product Model Version (Default: v1)");
        options.addOption(WPM_VALUE_TYPE_ARG, true, "Weighted Product Model Value Type for v2 (Default: actual)");

        options.addOption(IDLE_SLOPE_ARG, true, "Idle slope for CLASS_A (Default: 0.75)");

        options.addOption(LOG_ARG, true, "Log Type (Default: info) (Choices: debug)");

        options.addOption(TSN_SIMULATION_VERSION_ARG, true, "TSN Simulation Version (Default: TSNConfigurationFramework) (Choices: TSNConfigurationFramework, TSNRoutingOptimization)");

        options.addOption(METAHEURISTIC_NAME_ARG, true, "Which metaheuristic runs (Default: GRASP) (Choices: GRASP, ALO)");

        options.addOption(MTR_NAME_ARG, true, "MTR Versions (Default: v1) (Choices: v1, Average)");

        options.addOption(WSM_NORMALIZATION_ARG, true, "WSM Normalization Versions (Default: max) (Choices: max, minMax, vector)");


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

            if (line.hasOption(EVALUATOR_ARG)) {
                if (Objects.equals(line.getOptionValue(EVALUATOR_ARG), Constants.AVB_LATENCY_MATH_VERSION_TSNCF)) {
                    evaluatorName = Constants.AVB_LATENCY_MATH_VERSION_TSNCF;
                    evaluator = new AVBLatencyMathTSNCF();
                } else if (Objects.equals(line.getOptionValue(EVALUATOR_ARG), Constants.AVB_LATENCY_MATH_VERSION_TSNRO)) {
                    evaluatorName = Constants.AVB_LATENCY_MATH_VERSION_TSNRO;
                    evaluator = new AVBLatencyMathTSNRO();
                } else if (Objects.equals(line.getOptionValue(EVALUATOR_ARG), Constants.NETWORK_CALCULUS)) {
                    evaluatorName = Constants.NETWORK_CALCULUS;
                    evaluator = new NetworkCalculus();
                }
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

            if (Objects.equals(line.getOptionValue(LOG_ARG), Constants.DEBUG)) {
                System.setProperty("org.slf4j.simple.SimpleLogger.DEFAULT_LOG_LEVEL_KEY", "DEBUG");
            }

            final Logger logger = LoggerFactory.getLogger(Main.class.getSimpleName());

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
                                    bag.setTopologyName(topologyName);
                                    bag.setApplicationName(applicationName);
                                    bag.setRouting(routing);
                                    bag.setPathFindingMethod(pathFindingMethod);
                                    bag.setAlgorithm(algorithm);
                                    bag.setK(k);
                                    bag.setThreadNumber(threadNumber);
                                    bag.setTimeout(timeout);
                                    bag.setMetaheuristicName(metaheuristicName);
                                    bag.setEvaluatorName(evaluatorName);

                                    if (evaluatorName.equals(Constants.NETWORK_CALCULUS)){
                                        createGCLSynthesisAndNetworkCalculusDirectories(bag);
                                    }

                                    LaursenRO laursenRO = new LaursenRO(k);

                                    logger.info(createInfo(bag));

                                    Solution solution = laursenRO.solve(graph, applicationList, bag, threadNumber, Duration.ofSeconds(timeout), evaluator);

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
                                case "WSMv2LWR" -> {
                                    WSMv2LWR wsmV2LWR = new WSMv2LWR(k);

                                    Bag bag = new Bag();
                                    bag.setTopologyName(topologyName);
                                    bag.setApplicationName(applicationName);
                                    bag.setRouting(routing);
                                    bag.setPathFindingMethod(pathFindingMethod);
                                    bag.setAlgorithm(algorithm);
                                    bag.setLWR(lwr);
                                    bag.setK(k);
                                    bag.setMCDMObjective(mcdmObjective);
                                    bag.setWSMNormalization(wsmNormalization);
                                    bag.setWSRT(wSRT);
                                    bag.setWTT(wTT);
                                    bag.setWLength(wLength);
                                    bag.setWUtil(wUtil);
                                    bag.setThreadNumber(threadNumber);
                                    bag.setTimeout(timeout);
                                    bag.setMetaheuristicName(metaheuristicName);
                                    bag.setEvaluatorName(evaluatorName);

                                    logger.info(createInfo(bag));

                                    Solution solution = wsmV2LWR.solve(graph, applicationList, bag, threadNumber, evaluator, Duration.ofSeconds(timeout));

                                    solution.getCost().writeResultToFile(bag);

                                    if (solution.getMulticastList() == null || solution.getMulticastList().isEmpty()) {
                                        logger.info(Constants.NO_SOLUTION_COULD_BE_FOUND);
                                    } else {
                                        if (solution.getCost().getTotalCost() == Double.MAX_VALUE) {
                                            logger.info(createFoundNoSolutionString(solution));
                                        } else {
                                            logger.info(createFoundSolutionString(solution));

                                            new OutputMethods(bag, wsmV2LWR.getSolution(), solution.getCost().getWCDMap(), graph, rate, wsmV2LWR.getDurationMap(), wsmV2LWR.getSRTUnicastCandidateList());

                                        }
                                    }
                                }
                                case "WPMDeadline" -> {
                                    WPMDeadline wpmDeadline = new WPMDeadline(k);

                                    Bag bag = new Bag();
                                    bag.setTopologyName(topologyName);
                                    bag.setApplicationName(applicationName);
                                    bag.setRouting(routing);
                                    bag.setPathFindingMethod(pathFindingMethod);
                                    bag.setAlgorithm(algorithm);
                                    bag.setK(k);
                                    bag.setMCDMObjective(mcdmObjective);
                                    bag.setWSRT(wSRT);
                                    bag.setWTT(wTT);
                                    bag.setWLength(wLength);
                                    bag.setWUtil(wUtil);
                                    bag.setMetaheuristicName(metaheuristicName);
                                    bag.setEvaluatorName(evaluatorName);
                                    bag.setWPMVersion(wpmVersion);

                                    if(Objects.equals(wpmVersion, Constants.WPM_VERSION_V1)){
                                        logger.info(createInfo(bag));
                                    } else if (Objects.equals(wpmVersion, Constants.WPM_VERSION_V2)) {
                                        bag.setWPMValueType(wpmValueType);
                                        logger.info(createInfo(bag));
                                    }

                                    Solution solution = wpmDeadline.solve(graph, applicationList, bag, evaluator);

                                    solution.getCost().writeResultToFile(bag);

                                    if (solution.getMulticastList() == null || solution.getMulticastList().isEmpty()) {
                                        logger.info(Constants.NO_SOLUTION_COULD_BE_FOUND);
                                    } else {
                                        if (solution.getCost().getTotalCost() == Double.MAX_VALUE) {
                                            logger.info(createFoundNoSolutionString(solution));
                                        } else {
                                            logger.info(createFoundSolutionString(solution));

                                            new OutputMethods(bag, wpmDeadline.getSolution(), solution.getCost().getWCDMap(), graph, rate, wpmDeadline.getDurationMap(), wpmDeadline.getSRTUnicastCandidateList());


                                        }
                                    }
                                }
                                case "WPMLWRDeadline" -> {
                                    WPMLWRDeadline graspWPMLWRDeadline = new WPMLWRDeadline(k);

                                    Bag bag = new Bag();
                                    bag.setTopologyName(topologyName);
                                    bag.setApplicationName(applicationName);
                                    bag.setRouting(routing);
                                    bag.setPathFindingMethod(pathFindingMethod);
                                    bag.setAlgorithm(algorithm);
                                    bag.setLWR(lwr);
                                    bag.setK(k);
                                    bag.setMCDMObjective(mcdmObjective);
                                    bag.setWSRT(wSRT);
                                    bag.setWTT(wTT);
                                    bag.setWLength(wLength);
                                    bag.setWUtil(wUtil);
                                    bag.setMetaheuristicName(metaheuristicName);
                                    bag.setEvaluatorName(evaluatorName);
                                    bag.setWPMVersion(wpmVersion);

                                    if(Objects.equals(wpmVersion, Constants.WPM_VERSION_V1)){
                                        logger.info(createInfo(bag));
                                    } else if (Objects.equals(wpmVersion, Constants.WPM_VERSION_V2)) {
                                        bag.setWPMValueType(wpmValueType);
                                        logger.info(createInfo(bag));
                                    }

                                    Solution solution = graspWPMLWRDeadline.solve(graph, applicationList, bag, threadNumber, evaluator, Duration.ofSeconds(timeout));

                                    solution.getCost().writeResultToFile(bag);


                                    if (solution.getMulticastList() == null || solution.getMulticastList().isEmpty()) {
                                        logger.info(Constants.NO_SOLUTION_COULD_BE_FOUND);
                                    } else {
                                        if (solution.getCost().getTotalCost() == Double.MAX_VALUE) {
                                            logger.info(createFoundNoSolutionString(solution));
                                        } else {
                                            logger.info(createFoundSolutionString(solution));

                                            new OutputMethods(bag, graspWPMLWRDeadline.getSolution(), solution.getCost().getWCDMap(), graph, rate, graspWPMLWRDeadline.getDurationMap(), graspWPMLWRDeadline.getSRTUnicastCandidateList());


                                        }
                                    }
                                }
                                case "WPMCWRDeadline" -> {
                                    WPMCWRDeadline graspWPMCWRDeadline = new WPMCWRDeadline(k);

                                    Bag bag = new Bag();
                                    bag.setTopologyName(topologyName);
                                    bag.setApplicationName(applicationName);
                                    bag.setRouting(routing);
                                    bag.setPathFindingMethod(pathFindingMethod);
                                    bag.setAlgorithm(algorithm);
                                    bag.setK(k);
                                    bag.setMCDMObjective(mcdmObjective);
                                    bag.setCWR(cwr);
                                    bag.setWSRT(wSRT);
                                    bag.setWTT(wTT);
                                    bag.setWLength(wLength);
                                    bag.setWUtil(wUtil);
                                    bag.setMetaheuristicName(metaheuristicName);
                                    bag.setEvaluatorName(evaluatorName);
                                    bag.setWPMVersion(wpmVersion);

                                    if(Objects.equals(wpmVersion, Constants.WPM_VERSION_V1)){
                                        logger.info(createInfo(bag));
                                    } else if (Objects.equals(wpmVersion, Constants.WPM_VERSION_V2)) {
                                        bag.setWPMValueType(wpmValueType);
                                        logger.info(createInfo(bag));
                                    }

                                    Solution solution = graspWPMCWRDeadline.solve(graph, applicationList, bag, threadNumber, evaluator, Duration.ofSeconds(timeout));

                                    solution.getCost().writeResultToFile(bag);


                                    if (solution.getMulticastList() == null || solution.getMulticastList().isEmpty()) {
                                        logger.info(Constants.NO_SOLUTION_COULD_BE_FOUND);
                                    } else {
                                        if (solution.getCost().getTotalCost() == Double.MAX_VALUE) {
                                            logger.info(createFoundNoSolutionString(solution));
                                        } else {
                                            logger.info(createFoundSolutionString(solution));

                                            new OutputMethods(bag, graspWPMCWRDeadline.getSolution(), solution.getCost().getWCDMap(), graph, rate, graspWPMCWRDeadline.getDurationMap(), graspWPMCWRDeadline.getSRTUnicastCandidateList());


                                        }
                                    }
                                }
                                case "WPMLWRCWRDeadline" -> {
                                    WPMLWRCWRDeadline graspWPMLWRCWRDeadline = new WPMLWRCWRDeadline(k);

                                    Bag bag = new Bag();
                                    bag.setTopologyName(topologyName);
                                    bag.setApplicationName(applicationName);
                                    bag.setRouting(routing);
                                    bag.setPathFindingMethod(pathFindingMethod);
                                    bag.setAlgorithm(algorithm);
                                    bag.setLWR(lwr);
                                    bag.setK(k);
                                    bag.setMCDMObjective(mcdmObjective);
                                    bag.setCWR(cwr);
                                    bag.setWSRT(wSRT);
                                    bag.setWTT(wTT);
                                    bag.setWLength(wLength);
                                    bag.setWUtil(wUtil);
                                    bag.setMetaheuristicName(metaheuristicName);
                                    bag.setEvaluatorName(evaluatorName);
                                    bag.setWPMVersion(wpmVersion);

                                    if(Objects.equals(wpmVersion, Constants.WPM_VERSION_V1)){
                                        logger.info(createInfo(bag));
                                    } else if (Objects.equals(wpmVersion, Constants.WPM_VERSION_V2)) {
                                        bag.setWPMValueType(wpmValueType);
                                        logger.info(createInfo(bag));
                                    }

                                    Solution solution = graspWPMLWRCWRDeadline.solve(graph, applicationList, bag, threadNumber, evaluator, Duration.ofSeconds(timeout));

                                    solution.getCost().writeResultToFile(bag);


                                    if (solution.getMulticastList() == null || solution.getMulticastList().isEmpty()) {
                                        logger.info(Constants.NO_SOLUTION_COULD_BE_FOUND);
                                    } else {
                                        if (solution.getCost().getTotalCost() == Double.MAX_VALUE) {
                                            logger.info(createFoundNoSolutionString(solution));
                                        } else {
                                            logger.info(createFoundSolutionString(solution));

                                            new OutputMethods(bag, graspWPMLWRCWRDeadline.getSolution(), solution.getCost().getWCDMap(), graph, rate, graspWPMLWRCWRDeadline.getDurationMap(), graspWPMLWRCWRDeadline.getSRTUnicastCandidateList());


                                        }
                                    }
                                }

                                default -> throw new InputMismatchException("Aborting: " + routing + ", " + pathFindingMethod + ", " + algorithm + " unrecognized!");
                            }
                        }
//                        case "pathPenalization" -> {
//                            switch (algorithm) {
//                                case "WSMv2LWR" -> {
//                                    ktu.kaganndemirr.routing.phy.pathpenalization.metaheuristic.WSMv2LWR wsmV2LWR = new ktu.kaganndemirr.routing.phy.pathpenalization.metaheuristic.WSMv2LWR(k);
//
//                                    Bag bag = new Bag();
//                                    bag.setTopologyName(topologyName);
//                                    bag.setApplicationName(applicationName);
//                                    bag.setRouting(routing);
//                                    bag.setPathFindingMethod(pathFindingMethod);
//                                    bag.setAlgorithm(algorithm);
//                                    bag.setLWR(lwr);
//                                    bag.setK(k);
//                                    bag.setMCDMObjective(mcdmObjective);
//                                    bag.setWSMNormalization(wsmNormalization);
//                                    bag.setWSRT(wSRT);
//                                    bag.setWTT(wTT);
//                                    bag.setWLength(wLength);
//                                    bag.setWUtil(wUtil);
//                                    bag.setMetaheuristicName(metaheuristicName);
//                                    bag.setEvaluatorName(evaluatorName);
//
//                                    logger.info("Solving problem using {}, {}, {}, K: {}, LWR: {}, wsmNormalization: {}, wSRT: {}, wTT: {}, wLength: {}, wUtil: {}, rate: {}, mcdmObjective: {}, Metaheuristic Name: {}, Evaluator: {}, Timeout: {}", routing, pathFindingMethod, algorithm, k, lwr, wsmNormalization, wSRT, wTT, wLength, wUtil, rate, mcdmObjective, metaheuristicName, evaluatorName, timeout);
//
//                                    Solution solution = wsmV2LWR.solve(graph, applicationList, bag, threadNumber, evaluator, Duration.ofSeconds(timeout));
//
//                                    PHYWSMv2LWRHolder phyWSMv2LWRHolder = new PHYWSMv2LWRHolder();
//                                    phyWSMv2LWRHolder.setTopologyName(topologyName);
//                                    phyWSMv2LWRHolder.setApplicationName(applicationName);
//                                    phyWSMv2LWRHolder.setRouting(routing);
//                                    phyWSMv2LWRHolder.setPathFindingMethod(pathFindingMethod);
//                                    phyWSMv2LWRHolder.setAlgorithm(algorithm);
//                                    phyWSMv2LWRHolder.setLWR(lwr);
//                                    phyWSMv2LWRHolder.setK(k);
//                                    phyWSMv2LWRHolder.setMCDMObjective(mcdmObjective);
//                                    phyWSMv2LWRHolder.setWSMNormalization(wsmNormalization);
//                                    phyWSMv2LWRHolder.setWSRT(wSRT);
//                                    phyWSMv2LWRHolder.setWTT(wTT);
//                                    phyWSMv2LWRHolder.setWLength(wLength);
//                                    phyWSMv2LWRHolder.setWUtil(wUtil);
//
//                                    solution.getCost().writePHYWSMv2LWRResultToFile(phyWSMv2LWRHolder);
//
//                                    if (solution.getMulticastList() == null || solution.getMulticastList().isEmpty()) {
//                                        logger.info(Constants.NO_SOLUTION_COULD_BE_FOUND);
//                                    } else {
//                                        if (solution.getCost().getTotalCost() == Double.MAX_VALUE) {
//                                            logger.info(createFoundNoSolutionString(solution));
//                                        } else {
//                                            logger.info(createFoundSolutionString(solution));
//
//                                            PHYWSMv2LWROutputShaper oS = new PHYWSMv2LWROutputShaper(phyWSMv2LWRHolder);
//
//                                            oS.writeSolutionToFile(wsmV2LWR.getSolution());
//
//                                            oS.writeWCDsToFile(solution.getCost().getWCDMap());
//
//                                            oS.writeLinkUtilizationsToFile(wsmV2LWR.getSolution(), graph, rate);
//
//                                            oS.writeDurationMap(wsmV2LWR.getDurationMap());
//
//                                            oS.writeSRTCandidateRoutesToFile(wsmV2LWR.getSRTUnicastCandidateList());
//
//                                        }
//                                    }
//                                }
//                                case "WPMDeadline" -> {
//                                    ktu.kaganndemirr.routing.phy.pathpenalization.heuristic.WPMDeadline wpmDeadline = new ktu.kaganndemirr.routing.phy.pathpenalization.heuristic.WPMDeadline(k);
//
//                                    if(Objects.equals(wpmVersion, Constants.WPM_VERSION_V1)){
//                                        logger.info("Solving problem using {}, {}, {}, K: {}, mcdmObjective: {}, wSRT: {}, wTT: {}, wLength: {}, wUtil: {}, wpmVersion: {}", routing, pathFindingMethod, algorithm, k, mcdmObjective, wSRT, wTT, wLength, wUtil, wpmVersion);
//                                    } else if (Objects.equals(wpmVersion, Constants.WPM_VERSION_V2)) {
//                                        logger.info("Solving problem using {}, {}, {}, K: {}, mcdmObjective: {}, wSRT: {}, wTT: {}, wLength: {}, wUtil: {}, wpmVersion: {}, wpmValueType: {}", routing, pathFindingMethod, algorithm, k, mcdmObjective, wSRT, wTT, wLength, wUtil, wpmVersion, wpmValueType);
//                                    }
//
//                                    Solution solution = wpmDeadline.solve(graph, applicationList, mcdmObjective, wSRT, wTT, wLength, wUtil, rate, wpmVersion, wpmValueType, evaluator);
//
//                                    PHYWPMv1Holder phyWPMv1Holder = new PHYWPMv1Holder();
//                                    PHYWPMv2Holder phyWPMv2Holder = new PHYWPMv2Holder();
//                                    if(Objects.equals(wpmVersion, Constants.WPM_VERSION_V1)){
//                                        phyWPMv1Holder.setTopologyName(topologyName);
//                                        phyWPMv1Holder.setApplicationName(applicationName);
//                                        phyWPMv1Holder.setRouting(routing);
//                                        phyWPMv1Holder.setPathFindingMethod(pathFindingMethod);
//                                        phyWPMv1Holder.setAlgorithm(algorithm);
//                                        phyWPMv1Holder.setK(k);
//                                        phyWPMv1Holder.setMCDMObjective(mcdmObjective);
//                                        phyWPMv1Holder.setWSRT(wSRT);
//                                        phyWPMv1Holder.setWTT(wTT);
//                                        phyWPMv1Holder.setWLength(wLength);
//                                        phyWPMv1Holder.setWUtil(wUtil);
//                                        phyWPMv1Holder.setMCDMObjective(mcdmObjective);
//                                        phyWPMv1Holder.setWPMVersion(wpmVersion);
//
//                                        solution.getCost().writePHYWPMv1ResultToFile(phyWPMv1Holder);
//
//                                    } else if (Objects.equals(wpmVersion, Constants.WPM_VERSION_V2)) {
//                                        phyWPMv2Holder.setTopologyName(topologyName);
//                                        phyWPMv2Holder.setApplicationName(applicationName);
//                                        phyWPMv2Holder.setRouting(routing);
//                                        phyWPMv2Holder.setPathFindingMethod(pathFindingMethod);
//                                        phyWPMv2Holder.setAlgorithm(algorithm);
//                                        phyWPMv2Holder.setK(k);
//                                        phyWPMv2Holder.setMCDMObjective(mcdmObjective);
//                                        phyWPMv2Holder.setWSRT(wSRT);
//                                        phyWPMv2Holder.setWTT(wTT);
//                                        phyWPMv2Holder.setWLength(wLength);
//                                        phyWPMv2Holder.setWUtil(wUtil);
//                                        phyWPMv2Holder.setMCDMObjective(mcdmObjective);
//                                        phyWPMv2Holder.setWPMVersion(wpmVersion);
//                                        phyWPMv2Holder.setWPMValueType(wpmValueType);
//
//                                        solution.getCost().writePHYWPMv2ResultToFile(phyWPMv2Holder);
//                                    }
//
//
//                                    if (solution.getMulticastList() == null || solution.getMulticastList().isEmpty()) {
//                                        logger.info(Constants.NO_SOLUTION_COULD_BE_FOUND);
//                                    } else {
//                                        if (solution.getCost().getTotalCost() == Double.MAX_VALUE) {
//                                            logger.info(createFoundNoSolutionString(solution));
//                                        } else {
//                                            logger.info(createFoundSolutionString(solution));
//
//                                            if(Objects.equals(wpmVersion, Constants.WPM_VERSION_V1)){
//
//                                                PHYWPMv1OutputShaper oS = new PHYWPMv1OutputShaper(phyWPMv1Holder);
//
//                                                oS.writeSolutionToFile(wpmDeadline.getSolution());
//
//                                                oS.writeWCDsToFile(solution.getCost().getWCDMap());
//
//                                                oS.writeLinkUtilizationsToFile(wpmDeadline.getSolution(), graph, rate);
//
//                                                oS.writeDurationMap(wpmDeadline.getDurationMap());
//
//                                                oS.writeSRTCandidateRoutesToFile(wpmDeadline.getSRTUnicastCandidateList());
//
//
//                                            } else if (Objects.equals(wpmVersion, Constants.WPM_VERSION_V2)) {
//                                                PHYWPMv2OutputShaper oS = new PHYWPMv2OutputShaper(phyWPMv2Holder);
//
//                                                oS.writeSolutionToFile(wpmDeadline.getSolution());
//
//                                                oS.writeWCDsToFile(solution.getCost().getWCDMap());
//
//                                                oS.writeLinkUtilizationsToFile(wpmDeadline.getSolution(), graph, rate);
//
//                                                oS.writeDurationMap(wpmDeadline.getDurationMap());
//
//                                                oS.writeSRTCandidateRoutesToFile(wpmDeadline.getSRTUnicastCandidateList());
//
//                                            }
//
//
//                                        }
//                                    }
//                                }
//                                case "WPMLWRDeadline" -> {
//                                    ktu.kaganndemirr.routing.phy.pathpenalization.metaheuristic.WPMLWRDeadline graspWPMLWRDeadline = new ktu.kaganndemirr.routing.phy.pathpenalization.metaheuristic.WPMLWRDeadline(k);
//
//                                    if(Objects.equals(wpmVersion, Constants.WPM_VERSION_V1)){
//                                        logger.info("Solving problem using {}, {}, {}, LWR: {}, K:{}, mcdmObjective: {}, wSRT: {}, wTT: {}, wLength: {}, wUtil: {}, wpmVersion: {}, Metaheuristic Name: {}, Evaluator: {} Timeout: {}(sec)", routing, pathFindingMethod, algorithm, lwr, k, mcdmObjective, wSRT, wTT, wLength, wUtil, wpmVersion, metaheuristicName, evaluatorName, timeout);
//                                    } else if (Objects.equals(wpmVersion, Constants.WPM_VERSION_V2)) {
//                                        logger.info("Solving problem using {}, {}, {}, LWR: {}, K:{}, mcdmObjective: {}, wSRT: {}, wTT: {}, wLength: {}, wUtil: {}, wpmVersion: {}, wpmValueType: {}, Metaheuristic Name: {}, Evaluator: {}, Timeout: {}(sec)", routing, pathFindingMethod, algorithm, lwr, k, mcdmObjective, wSRT, wTT, wLength, wUtil, wpmVersion, wpmValueType, metaheuristicName, evaluatorName, timeout);
//                                    }
//
//                                    Solution solution = graspWPMLWRDeadline.solve(graph, applicationList, threadNumber, lwr, mcdmObjective, wSRT, wTT, wLength, wUtil, rate, wpmVersion, wpmValueType, metaheuristicName, evaluator, Duration.ofSeconds(timeout));
//
//                                    PHYWPMLWRv1Holder phyWPMLWRv1Holder = new PHYWPMLWRv1Holder();
//                                    PHYWPMLWRv2Holder phyWPMLWRv2Holder = new PHYWPMLWRv2Holder();
//                                    if(Objects.equals(wpmVersion, Constants.WPM_VERSION_V1)){
//                                        phyWPMLWRv1Holder.setTopologyName(topologyName);
//                                        phyWPMLWRv1Holder.setApplicationName(applicationName);
//                                        phyWPMLWRv1Holder.setRouting(routing);
//                                        phyWPMLWRv1Holder.setPathFindingMethod(pathFindingMethod);
//                                        phyWPMLWRv1Holder.setAlgorithm(algorithm);
//                                        phyWPMLWRv1Holder.setLWR(lwr);
//                                        phyWPMLWRv1Holder.setK(k);
//                                        phyWPMLWRv2Holder.setMCDMObjective(mcdmObjective);
//                                        phyWPMLWRv1Holder.setWSRT(wSRT);
//                                        phyWPMLWRv1Holder.setWTT(wTT);
//                                        phyWPMLWRv1Holder.setWLength(wLength);
//                                        phyWPMLWRv1Holder.setWUtil(wUtil);
//                                        phyWPMLWRv1Holder.setWPMVersion(wpmVersion);
//                                        phyWPMLWRv1Holder.setMCDMObjective(mcdmObjective);
//
//                                        solution.getCost().writePHYWPMLWRv1ResultToFile(phyWPMLWRv1Holder);
//
//                                    } else if (Objects.equals(wpmVersion, Constants.WPM_VERSION_V2)) {
//                                        phyWPMLWRv2Holder.setTopologyName(topologyName);
//                                        phyWPMLWRv2Holder.setApplicationName(applicationName);
//                                        phyWPMLWRv2Holder.setRouting(routing);
//                                        phyWPMLWRv2Holder.setPathFindingMethod(pathFindingMethod);
//                                        phyWPMLWRv2Holder.setAlgorithm(algorithm);
//                                        phyWPMLWRv2Holder.setLWR(lwr);
//                                        phyWPMLWRv2Holder.setK(k);
//                                        phyWPMLWRv2Holder.setMCDMObjective(mcdmObjective);
//                                        phyWPMLWRv2Holder.setWSRT(wSRT);
//                                        phyWPMLWRv2Holder.setWTT(wTT);
//                                        phyWPMLWRv2Holder.setWLength(wLength);
//                                        phyWPMLWRv2Holder.setWUtil(wUtil);
//                                        phyWPMLWRv2Holder.setMCDMObjective(mcdmObjective);
//                                        phyWPMLWRv2Holder.setWPMVersion(wpmVersion);
//                                        phyWPMLWRv2Holder.setWPMValueType(wpmValueType);
//
//                                        solution.getCost().writePHYWPMLWRv2ResultToFile(phyWPMLWRv2Holder);
//                                    }
//
//
//                                    if (solution.getMulticastList() == null || solution.getMulticastList().isEmpty()) {
//                                        logger.info(Constants.NO_SOLUTION_COULD_BE_FOUND);
//                                    } else {
//                                        if (solution.getCost().getTotalCost() == Double.MAX_VALUE) {
//                                            logger.info(createFoundNoSolutionString(solution));
//                                        } else {
//                                            logger.info(createFoundSolutionString(solution));
//
//                                            if(Objects.equals(wpmVersion, Constants.WPM_VERSION_V1)){
//
//                                                PHYWPMLWRv1OutputShaper oS = new PHYWPMLWRv1OutputShaper(phyWPMLWRv1Holder);
//
//                                                oS.writeSolutionToFile(graspWPMLWRDeadline.getSolution());
//
//                                                oS.writeWCDsToFile(solution.getCost().getWCDMap());
//
//                                                oS.writeLinkUtilizationsToFile(graspWPMLWRDeadline.getSolution(), graph, rate);
//
//                                                oS.writeDurationMap(graspWPMLWRDeadline.getDurationMap());
//
//                                                oS.writeSRTCandidateRoutesToFile(graspWPMLWRDeadline.getSRTUnicastCandidateList());
//
//
//                                            } else if (Objects.equals(wpmVersion, Constants.WPM_VERSION_V2)) {
//                                                PHYWPMLWRv2OutputShaper oS = new PHYWPMLWRv2OutputShaper(phyWPMLWRv2Holder);
//
//                                                oS.writeSolutionToFile(graspWPMLWRDeadline.getSolution());
//
//                                                oS.writeWCDsToFile(solution.getCost().getWCDMap());
//
//                                                oS.writeLinkUtilizationsToFile(graspWPMLWRDeadline.getSolution(), graph, rate);
//
//                                                oS.writeDurationMap(graspWPMLWRDeadline.getDurationMap());
//
//                                                oS.writeSRTCandidateRoutesToFile(graspWPMLWRDeadline.getSRTUnicastCandidateList());
//
//                                            }
//
//
//                                        }
//                                    }
//                                }
//                                case "WPMCWRDeadline" -> {
//                                    ktu.kaganndemirr.routing.phy.pathpenalization.metaheuristic.WPMCWRDeadline graspWPMCWRDeadline = new ktu.kaganndemirr.routing.phy.pathpenalization.metaheuristic.WPMCWRDeadline(k);
//
//                                    if(Objects.equals(wpmVersion, Constants.WPM_VERSION_V1)){
//                                        logger.info("Solving problem using {}, {}, {}, K:{}, mcdmObjective: {}, CWR: {}, wpmVersion: {}, Metaheuristic Name:{}, Evaluator: {}, Timeout: {}(sec)", routing, pathFindingMethod, algorithm, k, mcdmObjective, cwr, wpmVersion, metaheuristicName, evaluatorName, timeout);
//                                    } else if (Objects.equals(wpmVersion, Constants.WPM_VERSION_V2)) {
//                                        logger.info("Solving problem using {}, {}, {}, K:{}, mcdmObjective: {}, CWR: {}, wpmVersion: {}, wpmValueType: {}, Metaheuristic Name: {}, Evaluator: {}, Timeout: {}(sec)", routing, pathFindingMethod, algorithm, k, mcdmObjective, cwr, wpmVersion, wpmValueType, metaheuristicName, evaluatorName, timeout);
//                                    }
//
//                                    Solution solution = graspWPMCWRDeadline.solve(graph, applicationList, threadNumber, mcdmObjective, cwr, rate, wpmVersion, wpmValueType, metaheuristicName, evaluator, Duration.ofSeconds(timeout));
//
//                                    PHYWPMCWRv1Holder phyWPMCWRv1Holder = new PHYWPMCWRv1Holder();
//                                    PHYWPMCWRv2Holder phyWPMCWRv2Holder = new PHYWPMCWRv2Holder();
//                                    if(Objects.equals(wpmVersion, Constants.WPM_VERSION_V1)){
//                                        phyWPMCWRv1Holder.setTopologyName(topologyName);
//                                        phyWPMCWRv1Holder.setApplicationName(applicationName);
//                                        phyWPMCWRv1Holder.setRouting(routing);
//                                        phyWPMCWRv1Holder.setPathFindingMethod(pathFindingMethod);
//                                        phyWPMCWRv1Holder.setAlgorithm(algorithm);
//                                        phyWPMCWRv1Holder.setK(k);
//                                        phyWPMCWRv1Holder.setMCDMObjective(mcdmObjective);
//                                        phyWPMCWRv1Holder.setCWR(cwr);
//                                        phyWPMCWRv1Holder.setWPMVersion(wpmVersion);
//
//                                        solution.getCost().writePHYWPMCWRv1ResultToFile(phyWPMCWRv1Holder);
//
//                                    } else if (Objects.equals(wpmVersion, Constants.WPM_VERSION_V2)) {
//                                        phyWPMCWRv2Holder.setTopologyName(topologyName);
//                                        phyWPMCWRv2Holder.setApplicationName(applicationName);
//                                        phyWPMCWRv2Holder.setRouting(routing);
//                                        phyWPMCWRv2Holder.setPathFindingMethod(pathFindingMethod);
//                                        phyWPMCWRv2Holder.setAlgorithm(algorithm);
//                                        phyWPMCWRv2Holder.setK(k);
//                                        phyWPMCWRv2Holder.setMCDMObjective(mcdmObjective);
//                                        phyWPMCWRv2Holder.setCWR(cwr);
//                                        phyWPMCWRv2Holder.setWPMVersion(wpmVersion);
//                                        phyWPMCWRv2Holder.setWPMValueType(wpmValueType);
//
//                                        solution.getCost().writePHYWPMCWRv2ResultToFile(phyWPMCWRv2Holder);
//                                    }
//
//
//                                    if (solution.getMulticastList() == null || solution.getMulticastList().isEmpty()) {
//                                        logger.info(Constants.NO_SOLUTION_COULD_BE_FOUND);
//                                    } else {
//                                        if (solution.getCost().getTotalCost() == Double.MAX_VALUE) {
//                                            logger.info(createFoundNoSolutionString(solution));
//                                        } else {
//                                            logger.info(createFoundSolutionString(solution));
//
//                                            if(Objects.equals(wpmVersion, Constants.WPM_VERSION_V1)){
//
//                                                PHYWPMCWRv1OutputShaper oS = new PHYWPMCWRv1OutputShaper(phyWPMCWRv1Holder);
//
//                                                oS.writeSolutionToFile(graspWPMCWRDeadline.getSolution());
//
//                                                oS.writeWCDsToFile(solution.getCost().getWCDMap());
//
//                                                oS.writeLinkUtilizationsToFile(graspWPMCWRDeadline.getSolution(), graph, rate);
//
//                                                oS.writeDurationMap(graspWPMCWRDeadline.getDurationMap());
//
//                                                oS.writeSRTCandidateRoutesToFile(graspWPMCWRDeadline.getSRTUnicastCandidateList());
//
//
//                                            } else if (Objects.equals(wpmVersion, Constants.WPM_VERSION_V2)) {
//                                                PHYWPMCWRv2OutputShaper oS = new PHYWPMCWRv2OutputShaper(phyWPMCWRv2Holder);
//
//                                                oS.writeSolutionToFile(graspWPMCWRDeadline.getSolution());
//
//                                                oS.writeWCDsToFile(solution.getCost().getWCDMap());
//
//                                                oS.writeLinkUtilizationsToFile(graspWPMCWRDeadline.getSolution(), graph, rate);
//
//                                                oS.writeDurationMap(graspWPMCWRDeadline.getDurationMap());
//
//                                                oS.writeSRTCandidateRoutesToFile(graspWPMCWRDeadline.getSRTUnicastCandidateList());
//
//                                            }
//
//
//                                        }
//                                    }
//                                }
//                                case "WPMLWRCWRDeadline" -> {
//                                    ktu.kaganndemirr.routing.phy.pathpenalization.metaheuristic.WPMLWRCWRDeadline graspWPMLWRCWRDeadline = new ktu.kaganndemirr.routing.phy.pathpenalization.metaheuristic.WPMLWRCWRDeadline(k);
//
//                                    if(Objects.equals(wpmVersion, Constants.WPM_VERSION_V1)){
//                                        logger.info("Solving problem using {}, {}, {}, LWR: {}, K: {}, mcdmObjective: {}, CWR: {}, wSRT: {}, wTT: {}, wLength: {}, wUtil: {}, wpmVersion: {}, Thread Number: {}, Timeout: {}(sec), Metaheuristic Name: {}, Evaluator: {}", routing, pathFindingMethod, algorithm, lwr, k, mcdmObjective, cwr, wSRT, wTT, wLength, wUtil, wpmVersion, threadNumber, timeout, metaheuristicName, evaluatorName);
//                                    } else if (Objects.equals(wpmVersion, Constants.WPM_VERSION_V2)) {
//                                        logger.info("Solving problem using {}, {}, {}, LWR: {}, K: {}, mcdmObjective: {}, CWR: {}, wSRT: {}, wTT: {}, wLength: {}, wUtil: {}, wpmVersion: {}, wpmValueType: {}, Thread Number: {}, Timeout: {}(sec), Metaheuristic Name: {}, Evaluator: {}", routing, pathFindingMethod, algorithm, lwr, k, mcdmObjective, cwr, wSRT, wTT, wLength, wUtil, wpmVersion, wpmValueType, threadNumber, timeout, metaheuristicName, evaluatorName);
//                                    }
//
//                                    Solution solution = graspWPMLWRCWRDeadline.solve(graph, applicationList, threadNumber, lwr, mcdmObjective, cwr, wSRT, wTT, wLength, wUtil, rate, wpmVersion, wpmValueType, metaheuristicName, evaluator, Duration.ofSeconds(timeout));
//
//                                    PHYWPMLWRCWRv1Holder phyWPMLWRCWRv1Holder = new PHYWPMLWRCWRv1Holder();
//                                    PHYWPMLWRCWRv2Holder phyWPMLWRCWRv2Holder = new PHYWPMLWRCWRv2Holder();
//                                    if(Objects.equals(wpmVersion, Constants.WPM_VERSION_V1)){
//                                        phyWPMLWRCWRv1Holder.setTopologyName(topologyName);
//                                        phyWPMLWRCWRv1Holder.setApplicationName(applicationName);
//                                        phyWPMLWRCWRv1Holder.setRouting(routing);
//                                        phyWPMLWRCWRv1Holder.setPathFindingMethod(pathFindingMethod);
//                                        phyWPMLWRCWRv1Holder.setAlgorithm(algorithm);
//                                        phyWPMLWRCWRv1Holder.setLWR(lwr);
//                                        phyWPMLWRCWRv1Holder.setK(k);
//                                        phyWPMLWRCWRv1Holder.setMCDMObjective(mcdmObjective);
//                                        phyWPMLWRCWRv1Holder.setCWR(cwr);
//                                        phyWPMLWRCWRv1Holder.setWSRT(wSRT);
//                                        phyWPMLWRCWRv1Holder.setWTT(wTT);
//                                        phyWPMLWRCWRv1Holder.setWLength(wLength);
//                                        phyWPMLWRCWRv1Holder.setWUtil(wUtil);
//                                        phyWPMLWRCWRv1Holder.setWPMVersion(wpmVersion);
//                                        phyWPMLWRCWRv1Holder.setMCDMObjective(mcdmObjective);
//
//                                        solution.getCost().writePHYWPMLWRCWRv1ResultToFile(phyWPMLWRCWRv1Holder);
//
//                                    } else if (Objects.equals(wpmVersion, Constants.WPM_VERSION_V2)) {
//                                        phyWPMLWRCWRv2Holder.setTopologyName(topologyName);
//                                        phyWPMLWRCWRv2Holder.setApplicationName(applicationName);
//                                        phyWPMLWRCWRv2Holder.setRouting(routing);
//                                        phyWPMLWRCWRv2Holder.setPathFindingMethod(pathFindingMethod);
//                                        phyWPMLWRCWRv2Holder.setAlgorithm(algorithm);
//                                        phyWPMLWRCWRv2Holder.setLWR(lwr);
//                                        phyWPMLWRCWRv2Holder.setK(k);
//                                        phyWPMLWRCWRv2Holder.setMCDMObjective(mcdmObjective);
//                                        phyWPMLWRCWRv2Holder.setCWR(cwr);
//                                        phyWPMLWRCWRv2Holder.setWSRT(wSRT);
//                                        phyWPMLWRCWRv2Holder.setWTT(wTT);
//                                        phyWPMLWRCWRv2Holder.setWLength(wLength);
//                                        phyWPMLWRCWRv2Holder.setWUtil(wUtil);
//                                        phyWPMLWRCWRv2Holder.setMCDMObjective(mcdmObjective);
//                                        phyWPMLWRCWRv2Holder.setWPMVersion(wpmVersion);
//                                        phyWPMLWRCWRv2Holder.setWPMValueType(wpmValueType);
//
//                                        solution.getCost().writePHYWPMLWRCWRv2ResultToFile(phyWPMLWRCWRv2Holder);
//                                    }
//
//
//                                    if (solution.getMulticastList() == null || solution.getMulticastList().isEmpty()) {
//                                        logger.info(Constants.NO_SOLUTION_COULD_BE_FOUND);
//                                    } else {
//                                        if (solution.getCost().getTotalCost() == Double.MAX_VALUE) {
//                                            logger.info(createFoundNoSolutionString(solution));
//                                        } else {
//                                            logger.info(createFoundSolutionString(solution));
//
//                                            if(Objects.equals(wpmVersion, Constants.WPM_VERSION_V1)){
//
//                                                PHYWPMLWRCWRv1OutputShaper oS = new PHYWPMLWRCWRv1OutputShaper(phyWPMLWRCWRv1Holder);
//
//                                                oS.writeSolutionToFile(graspWPMLWRCWRDeadline.getSolution());
//
//                                                oS.writeWCDsToFile(solution.getCost().getWCDMap());
//
//                                                oS.writeLinkUtilizationsToFile(graspWPMLWRCWRDeadline.getSolution(), graph, rate);
//
//                                                oS.writeDurationMap(graspWPMLWRCWRDeadline.getDurationMap());
//
//                                                oS.writeSRTCandidateRoutesToFile(graspWPMLWRCWRDeadline.getSRTUnicastCandidateList());
//
//
//                                            } else if (Objects.equals(wpmVersion, Constants.WPM_VERSION_V2)) {
//                                                PHYWPMLWRCWRv2OutputShaper oS = new PHYWPMLWRCWRv2OutputShaper(phyWPMLWRCWRv2Holder);
//
//                                                oS.writeSolutionToFile(graspWPMLWRCWRDeadline.getSolution());
//
//                                                oS.writeWCDsToFile(solution.getCost().getWCDMap());
//
//                                                oS.writeLinkUtilizationsToFile(graspWPMLWRCWRDeadline.getSolution(), graph, rate);
//
//                                                oS.writeDurationMap(graspWPMLWRCWRDeadline.getDurationMap());
//
//                                                oS.writeSRTCandidateRoutesToFile(graspWPMLWRCWRDeadline.getSRTUnicastCandidateList());
//
//                                            }
//
//
//                                        }
//                                    }
//                                }
//                                default -> throw new InputMismatchException("Aborting: " + routing + ", " + pathFindingMethod + ", " + algorithm + " unrecognized!");
//                            }
//                        }
                        default -> throw new InputMismatchException("Aborting: Solver " + routing + ", " + pathFindingMethod + " unrecognized!");
                    }
                }
//                case "mtr" -> {
//                    switch (pathFindingMethod) {
//                        case "yen" -> {
//                            switch (algorithm) {
//                                case "LaursenRO" -> {
//                                    ktu.kaganndemirr.routing.mtr.yen.metaheuristic.LaursenRO laursenRO = new ktu.kaganndemirr.routing.mtr.yen.metaheuristic.LaursenRO(k);
//
//                                    logger.info("Solving problem using {}, {}, {}, {}, K: {}, Thread Number: {}, Timeout: {}(sec), Metaheuristic Name: {}, Evaluator: {}", routing, mtrName, pathFindingMethod, algorithm, k, threadNumber, timeout, metaheuristicName, evaluatorName);
//
//                                    Solution solution = laursenRO.solve(graph, applicationList, mtrName, threadNumber, Duration.ofSeconds(timeout), metaheuristicName, evaluator);
//
//                                    PHYWPMLWRCWRv1Holder phyWPMLWRCWRv1Holder = new PHYWPMLWRCWRv1Holder();
//                                    PHYWPMLWRCWRv2Holder phyWPMLWRCWRv2Holder = new PHYWPMLWRCWRv2Holder();
//                                    if(Objects.equals(wpmVersion, Constants.WPM_VERSION_V1)){
//                                        phyWPMLWRCWRv1Holder.setTopologyName(topologyName);
//                                        phyWPMLWRCWRv1Holder.setApplicationName(applicationName);
//                                        phyWPMLWRCWRv1Holder.setRouting(routing);
//                                        phyWPMLWRCWRv1Holder.setPathFindingMethod(pathFindingMethod);
//                                        phyWPMLWRCWRv1Holder.setAlgorithm(algorithm);
//                                        phyWPMLWRCWRv1Holder.setLWR(lwr);
//                                        phyWPMLWRCWRv1Holder.setK(k);
//                                        phyWPMLWRCWRv1Holder.setMCDMObjective(mcdmObjective);
//                                        phyWPMLWRCWRv1Holder.setCWR(cwr);
//                                        phyWPMLWRCWRv1Holder.setWSRT(wSRT);
//                                        phyWPMLWRCWRv1Holder.setWTT(wTT);
//                                        phyWPMLWRCWRv1Holder.setWLength(wLength);
//                                        phyWPMLWRCWRv1Holder.setWUtil(wUtil);
//                                        phyWPMLWRCWRv1Holder.setWPMVersion(wpmVersion);
//                                        phyWPMLWRCWRv1Holder.setMCDMObjective(mcdmObjective);
//
//                                        solution.getCost().writePHYWPMLWRCWRv1ResultToFile(phyWPMLWRCWRv1Holder);
//
//                                    } else if (Objects.equals(wpmVersion, Constants.WPM_VERSION_V2)) {
//                                        phyWPMLWRCWRv2Holder.setTopologyName(topologyName);
//                                        phyWPMLWRCWRv2Holder.setApplicationName(applicationName);
//                                        phyWPMLWRCWRv2Holder.setRouting(routing);
//                                        phyWPMLWRCWRv2Holder.setPathFindingMethod(pathFindingMethod);
//                                        phyWPMLWRCWRv2Holder.setAlgorithm(algorithm);
//                                        phyWPMLWRCWRv2Holder.setLWR(lwr);
//                                        phyWPMLWRCWRv2Holder.setK(k);
//                                        phyWPMLWRCWRv2Holder.setMCDMObjective(mcdmObjective);
//                                        phyWPMLWRCWRv2Holder.setCWR(cwr);
//                                        phyWPMLWRCWRv2Holder.setWSRT(wSRT);
//                                        phyWPMLWRCWRv2Holder.setWTT(wTT);
//                                        phyWPMLWRCWRv2Holder.setWLength(wLength);
//                                        phyWPMLWRCWRv2Holder.setWUtil(wUtil);
//                                        phyWPMLWRCWRv2Holder.setMCDMObjective(mcdmObjective);
//                                        phyWPMLWRCWRv2Holder.setWPMVersion(wpmVersion);
//                                        phyWPMLWRCWRv2Holder.setWPMValueType(wpmValueType);
//
//                                        solution.getCost().writePHYWPMLWRCWRv2ResultToFile(phyWPMLWRCWRv2Holder);
//                                    }
//
//
//                                    if (solution.getMulticastList() == null || solution.getMulticastList().isEmpty()) {
//                                        logger.info(Constants.NO_SOLUTION_COULD_BE_FOUND);
//                                    } else {
//                                        if (solution.getCost().getTotalCost() == Double.MAX_VALUE) {
//                                            logger.info(createFoundNoSolutionString(solution));
//                                        } else {
//                                            logger.info(createFoundSolutionString(solution));
//
//
//
//
//                                        }
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
                default -> throw new InputMismatchException("Aborting: " + routing + " unrecognized!");
            }
            //endregion

        } catch (ParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new RuntimeException(e);
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