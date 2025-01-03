package ktu.kaganndemirr;

import ktu.kaganndemirr.application.Application;
import ktu.kaganndemirr.architecture.GCLEdge;
import ktu.kaganndemirr.architecture.Node;
import ktu.kaganndemirr.evaluator.AVBLatencyMathTSNCF;
import ktu.kaganndemirr.evaluator.AVBLatencyMathTSNRO;
import ktu.kaganndemirr.evaluator.Evaluator;
import ktu.kaganndemirr.output.shapers.phy.*;
import ktu.kaganndemirr.parser.ApplicationParser;
import ktu.kaganndemirr.parser.TopologyParser;
import ktu.kaganndemirr.routing.phy.yen.heuristic.WPMDeadline;
import ktu.kaganndemirr.routing.phy.yen.metaheuristic.WPMCWRDeadline;
import ktu.kaganndemirr.routing.phy.yen.metaheuristic.WPMLWRCWRDeadline;
import ktu.kaganndemirr.routing.phy.yen.metaheuristic.WPMLWRDeadline;
import ktu.kaganndemirr.solver.Solution;
import ktu.kaganndemirr.util.holders.*;
import org.apache.commons.cli.*;
import org.jgrapht.Graph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ktu.kaganndemirr.util.constants.Constants;

import java.io.File;
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
    private static final String EVALUATOR_ARG = "evaluator";
    private static final String K_ARG = "k";
    private static final String THREAD_NUMBER_ARG = "threadNumber";
    private static final String TIMEOUT_ARG = "timeout";

    private static final String ROUTING_ARG = "routing";
    private static final String PATH_FINDER_METHOD_ARG = "pathFinderMethod";
    private static final String ALGORITHM_ARG = "algorithm";

    private static final String WPM_OBJECTIVE_ARG = "wpmObjective";
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
        String pathFinderMethod = "yen";
        //Default Algorithm
        String algorithm = "GRASP";

        //Default weightedProductModelObjective
        String wpmObjective = "srtTTLength";
        //Default wSoftRealTime
        double wSRT = 0;
        //Default wTimeTriggered
        double wTT = 0;
        //Default wLength
        double wLength = 0;
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

        //endregion

        //region <Options>
        Option architectureFile = Option.builder(NET_ARG).required().argName("file").hasArg().desc("Use given file as network").build();
        Option applicationFile = Option.builder(APP_ARG).required().argName("file").hasArg().desc("Use given file as application").build();

        Options options = new Options();
        options.addOption(applicationFile);
        options.addOption(architectureFile);
        options.addOption(RATE_ARG, true, "The rate in mbps (Type: mbps) (Default: 1000)");
        options.addOption(EVALUATOR_ARG, true, "WCD Analysis Method (Default: avbLatencyMathTSNCF)");
        options.addOption(K_ARG, true, "Value of K for search-space reduction (Default: 50)");
        options.addOption(THREAD_NUMBER_ARG, true, "Thread number (Default: Number of Processor Thread)");
        options.addOption(TIMEOUT_ARG, true, "Metaheuristic algorithm timeout (Type: Second) (Default: 60");

        options.addOption(ROUTING_ARG, true, "Choose routing (Default: phy) (Choices: phy, mtr)");
        options.addOption(PATH_FINDER_METHOD_ARG, true, "Choose path finder method (Default = yen) (Choices: shortestPath, yen, pathPenalization)");
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

        options.addOption(LOG_ARG, true, "Log Type (Default: No Log) (Choices: info, debug)");

        options.addOption(TSN_SIMULATION_VERSION_ARG, true, "TSN Simulation Version (Default: TSNConfigurationFramework) (Choices: TSNConfigurationFramework, TSNRoutingOptimization)");

        options.addOption(METAHEURISTIC_NAME_ARG, true, "Which metaheuristic runs (Default: GRASP) (Choices: GRASP, ALO)");

        //endregion

        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine line = parser.parse(options, args);

            //region <Set Values>

            File net = new File(line.getOptionValue(NET_ARG));
            File app = new File(line.getOptionValue(APP_ARG));

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
                    switch (pathFinderMethod) {
                        case "yen" -> {
                            switch (algorithm) {
                                case "WPMDeadline" -> {
                                    WPMDeadline wpmDeadline = new WPMDeadline(k);

                                    Bag bag = new Bag();
                                    bag.setTopologyName(topologyName);
                                    bag.setApplicationName(applicationName);
                                    bag.setRouting(routing);
                                    bag.setPathFinderMethod(pathFinderMethod);
                                    bag.setAlgorithm(algorithm);
                                    bag.setK(k);
                                    bag.setWPMObjective(wpmObjective);
                                    bag.setWSRT(wSRT);
                                    bag.setWTT(wTT);
                                    bag.setWLength(wLength);
                                    bag.setWUtil(wUtil);
                                    bag.setWLength(wLength);
                                    bag.setWPMVersion(wpmVersion);
                                    bag.setWPMValueType(wpmValueType);
                                    bag.setMetaheuristicName(metaheuristicName);
                                    bag.setEvaluator(evaluator);
                                    bag.setEvaluatorName(evaluatorName);

                                    logger.info(createInfoString(bag));

                                    Solution solution = wpmDeadline.solve(bag);

                                    if(Objects.equals(wpmVersion, Constants.WPM_VERSION_V1)){
                                        solution.getCost().writePHYWPMv1ResultToFile(phyWPMv1Holder);
                                    } else if (Objects.equals(wpmVersion, Constants.WPM_VERSION_V2)) {
                                        solution.getCost().writePHYWPMv2ResultToFile(phyWPMv2Holder);
                                    }

                                    if (solution.getMulticastList() == null || solution.getMulticastList().isEmpty()) {
                                        logger.info(Constants.NO_SOLUTION_COULD_BE_FOUND);
                                    } else {
                                        if (solution.getCost().getTotalCost() == Double.MAX_VALUE) {
                                            logger.info(createFoundNoSolutionString(solution));
                                        } else {
                                            logger.info(createFoundSolutionString(solution));

                                            if(Objects.equals(wpmVersion, Constants.WPM_VERSION_V1)){

                                                PHYWPMv1OutputShaper oS = new PHYWPMv1OutputShaper(phyWPMv1Holder);

                                                oS.writeSolutionToFile(wpmDeadline.getSolution());

                                                oS.writeWCDsToFile(solution.getCost().getWCDMap());

                                                oS.writeLinkUtilizationsToFile(wpmDeadline.getSolution(), graph, rate);

                                                oS.writeDurationMap(wpmDeadline.getDurationMap());

                                                oS.writeSRTCandidateRoutesToFile(wpmDeadline.getSRTUnicastCandidateList());


                                            } else if (Objects.equals(wpmVersion, Constants.WPM_VERSION_V2)) {
                                                PHYWPMv2OutputShaper oS = new PHYWPMv2OutputShaper(phyWPMv2Holder);

                                                oS.writeSolutionToFile(wpmDeadline.getSolution());

                                                oS.writeWCDsToFile(solution.getCost().getWCDMap());

                                                oS.writeLinkUtilizationsToFile(wpmDeadline.getSolution(), graph, rate);

                                                oS.writeDurationMap(wpmDeadline.getDurationMap());

                                                oS.writeSRTCandidateRoutesToFile(wpmDeadline.getSRTUnicastCandidateList());

                                            }


                                        }
                                    }
                                }
                                case "WPMLWRDeadline" -> {
                                    WPMLWRDeadline graspWPMLWRDeadline = new WPMLWRDeadline(k);

                                    if(Objects.equals(wpmVersion, Constants.WPM_VERSION_V1)){
                                        logger.info("Solving problem using {}, {}, {}, LWR: {}, K:{}, wpmObjective: {}, wSRT: {}, wTT: {}, wLength: {}, wUtil: {}, wpmVersion: {}, Metaheuristic Name: {}, Evaluator: {} Timeout: {}(sec)", routing, pathFinderMethod, algorithm, lwr, k, wpmObjective, wSRT, wTT, wLength, wUtil, wpmVersion, metaheuristicName, evaluatorName, timeout);
                                    } else if (Objects.equals(wpmVersion, Constants.WPM_VERSION_V2)) {
                                        logger.info("Solving problem using {}, {}, {}, LWR: {}, K:{}, wpmObjective: {}, wSRT: {}, wTT: {}, wLength: {}, wUtil: {}, wpmVersion: {}, wpmValueType: {}, Metaheuristic Name: {}, Evaluator: {}, Timeout: {}(sec)", routing, pathFinderMethod, algorithm, lwr, k, wpmObjective, wSRT, wTT, wLength, wUtil, wpmVersion, wpmValueType, metaheuristicName, evaluatorName, timeout);
                                    }

                                    Solution solution = graspWPMLWRDeadline.solve(graph, applicationList, threadNumber, lwr, wpmObjective, wSRT, wTT, wLength, wUtil, rate, wpmVersion, wpmValueType, metaheuristicName, evaluator, Duration.ofSeconds(timeout));

                                    PHYWPMLWRv1Holder phyWPMLWRv1Holder = new PHYWPMLWRv1Holder();
                                    PHYWPMLWRv2Holder phyWPMLWRv2Holder = new PHYWPMLWRv2Holder();
                                    if(Objects.equals(wpmVersion, Constants.WPM_VERSION_V1)){
                                        phyWPMLWRv1Holder.setTopologyName(topologyName);
                                        phyWPMLWRv1Holder.setApplicationName(applicationName);
                                        phyWPMLWRv1Holder.setRouting(routing);
                                        phyWPMLWRv1Holder.setPathFindingMethod(pathFinderMethod);
                                        phyWPMLWRv1Holder.setAlgorithm(algorithm);
                                        phyWPMLWRv1Holder.setLWR(lwr);
                                        phyWPMLWRv1Holder.setK(k);
                                        phyWPMLWRv1Holder.setWPMObjective(wpmObjective);
                                        phyWPMLWRv1Holder.setWSRT(wSRT);
                                        phyWPMLWRv1Holder.setWTT(wTT);
                                        phyWPMLWRv1Holder.setWLength(wLength);
                                        phyWPMLWRv1Holder.setWUtil(wUtil);
                                        phyWPMLWRv1Holder.setWPMVersion(wpmVersion);
                                        phyWPMLWRv1Holder.setWPMObjective(wpmObjective);

                                        solution.getCost().writePHYWPMLWRv1ResultToFile(phyWPMLWRv1Holder);

                                    } else if (Objects.equals(wpmVersion, Constants.WPM_VERSION_V2)) {
                                        phyWPMLWRv2Holder.setTopologyName(topologyName);
                                        phyWPMLWRv2Holder.setApplicationName(applicationName);
                                        phyWPMLWRv2Holder.setRouting(routing);
                                        phyWPMLWRv2Holder.setPathFindingMethod(pathFinderMethod);
                                        phyWPMLWRv2Holder.setAlgorithm(algorithm);
                                        phyWPMLWRv2Holder.setLWR(lwr);
                                        phyWPMLWRv2Holder.setK(k);
                                        phyWPMLWRv2Holder.setWPMObjective(wpmObjective);
                                        phyWPMLWRv2Holder.setWSRT(wSRT);
                                        phyWPMLWRv2Holder.setWTT(wTT);
                                        phyWPMLWRv2Holder.setWLength(wLength);
                                        phyWPMLWRv2Holder.setWUtil(wUtil);
                                        phyWPMLWRv2Holder.setWPMObjective(wpmObjective);
                                        phyWPMLWRv2Holder.setWPMVersion(wpmVersion);
                                        phyWPMLWRv2Holder.setWPMValueType(wpmValueType);

                                        solution.getCost().writePHYWPMLWRv2ResultToFile(phyWPMLWRv2Holder);
                                    }


                                    if (solution.getMulticastList() == null || solution.getMulticastList().isEmpty()) {
                                        logger.info(Constants.NO_SOLUTION_COULD_BE_FOUND);
                                    } else {
                                        if (solution.getCost().getTotalCost() == Double.MAX_VALUE) {
                                            logger.info(createFoundNoSolutionString(solution));
                                        } else {
                                            logger.info(createFoundSolutionString(solution));

                                            if(Objects.equals(wpmVersion, Constants.WPM_VERSION_V1)){

                                                PHYWPMLWRv1OutputShaper oS = new PHYWPMLWRv1OutputShaper(phyWPMLWRv1Holder);

                                                oS.writeSolutionToFile(graspWPMLWRDeadline.getSolution());

                                                oS.writeWCDsToFile(solution.getCost().getWCDMap());

                                                oS.writeLinkUtilizationsToFile(graspWPMLWRDeadline.getSolution(), graph, rate);

                                                oS.writeDurationMap(graspWPMLWRDeadline.getDurationMap());

                                                oS.writeSRTCandidateRoutesToFile(graspWPMLWRDeadline.getSRTUnicastCandidateList());


                                            } else if (Objects.equals(wpmVersion, Constants.WPM_VERSION_V2)) {
                                                PHYWPMLWRv2OutputShaper oS = new PHYWPMLWRv2OutputShaper(phyWPMLWRv2Holder);

                                                oS.writeSolutionToFile(graspWPMLWRDeadline.getSolution());

                                                oS.writeWCDsToFile(solution.getCost().getWCDMap());

                                                oS.writeLinkUtilizationsToFile(graspWPMLWRDeadline.getSolution(), graph, rate);

                                                oS.writeDurationMap(graspWPMLWRDeadline.getDurationMap());

                                                oS.writeSRTCandidateRoutesToFile(graspWPMLWRDeadline.getSRTUnicastCandidateList());

                                            }


                                        }
                                    }
                                }
                                case "WPMCWRDeadline" -> {
                                    WPMCWRDeadline graspWPMCWRDeadline = new WPMCWRDeadline(k);

                                    if(Objects.equals(wpmVersion, Constants.WPM_VERSION_V1)){
                                        logger.info("Solving problem using {}, {}, {}, K:{}, wpmObjective: {}, CWR: {}, wpmVersion: {}, Metaheuristic Name:{}, Evaluator: {}, Timeout: {}", routing, pathFinderMethod, algorithm, k, wpmObjective, cwr, wpmVersion, metaheuristicName, evaluatorName, timeout);
                                    } else if (Objects.equals(wpmVersion, Constants.WPM_VERSION_V2)) {
                                        logger.info("Solving problem using {}, {}, {}, K:{}, wpmObjective: {}, CWR: {}, wpmVersion: {}, wpmValueType: {}, Metaheuristic Name: {}, Evaluator: {}, Timeout: {}", routing, pathFinderMethod, algorithm, k, wpmObjective, cwr, wpmVersion, wpmValueType, metaheuristicName, evaluatorName, timeout);
                                    }

                                    Solution solution = graspWPMCWRDeadline.solve(graph, applicationList, threadNumber, wpmObjective, cwr, rate, wpmVersion, wpmValueType, metaheuristicName, evaluator, Duration.ofSeconds(timeout));

                                    PHYWPMCWRv1Holder phyWPMCWRv1Holder = new PHYWPMCWRv1Holder();
                                    PHYWPMCWRv2Holder phyWPMCWRv2Holder = new PHYWPMCWRv2Holder();
                                    if(Objects.equals(wpmVersion, Constants.WPM_VERSION_V1)){
                                        phyWPMCWRv1Holder.setTopologyName(topologyName);
                                        phyWPMCWRv1Holder.setApplicationName(applicationName);
                                        phyWPMCWRv1Holder.setRouting(routing);
                                        phyWPMCWRv1Holder.setPathFindingMethod(pathFinderMethod);
                                        phyWPMCWRv1Holder.setAlgorithm(algorithm);
                                        phyWPMCWRv1Holder.setK(k);
                                        phyWPMCWRv1Holder.setWPMObjective(wpmObjective);
                                        phyWPMCWRv1Holder.setCWR(cwr);
                                        phyWPMCWRv1Holder.setWPMVersion(wpmVersion);

                                        solution.getCost().writePHYWPMCWRv1ResultToFile(phyWPMCWRv1Holder);

                                    } else if (Objects.equals(wpmVersion, Constants.WPM_VERSION_V2)) {
                                        phyWPMCWRv2Holder.setTopologyName(topologyName);
                                        phyWPMCWRv2Holder.setApplicationName(applicationName);
                                        phyWPMCWRv2Holder.setRouting(routing);
                                        phyWPMCWRv2Holder.setPathFindingMethod(pathFinderMethod);
                                        phyWPMCWRv2Holder.setAlgorithm(algorithm);
                                        phyWPMCWRv2Holder.setK(k);
                                        phyWPMCWRv2Holder.setWPMObjective(wpmObjective);
                                        phyWPMCWRv2Holder.setCWR(cwr);
                                        phyWPMCWRv2Holder.setWPMVersion(wpmVersion);
                                        phyWPMCWRv2Holder.setWPMValueType(wpmValueType);

                                        solution.getCost().writePHYWPMCWRv2ResultToFile(phyWPMCWRv2Holder);
                                    }


                                    if (solution.getMulticastList() == null || solution.getMulticastList().isEmpty()) {
                                        logger.info(Constants.NO_SOLUTION_COULD_BE_FOUND);
                                    } else {
                                        if (solution.getCost().getTotalCost() == Double.MAX_VALUE) {
                                            logger.info(createFoundNoSolutionString(solution));
                                        } else {
                                            logger.info(createFoundSolutionString(solution));

                                            if(Objects.equals(wpmVersion, Constants.WPM_VERSION_V1)){

                                                PHYWPMCWRv1OutputShaper oS = new PHYWPMCWRv1OutputShaper(phyWPMCWRv1Holder);

                                                oS.writeSolutionToFile(graspWPMCWRDeadline.getSolution());

                                                oS.writeWCDsToFile(solution.getCost().getWCDMap());

                                                oS.writeLinkUtilizationsToFile(graspWPMCWRDeadline.getSolution(), graph, rate);

                                                oS.writeDurationMap(graspWPMCWRDeadline.getDurationMap());

                                                oS.writeSRTCandidateRoutesToFile(graspWPMCWRDeadline.getSRTUnicastCandidateList());


                                            } else if (Objects.equals(wpmVersion, Constants.WPM_VERSION_V2)) {
                                                PHYWPMCWRv2OutputShaper oS = new PHYWPMCWRv2OutputShaper(phyWPMCWRv2Holder);

                                                oS.writeSolutionToFile(graspWPMCWRDeadline.getSolution());

                                                oS.writeWCDsToFile(solution.getCost().getWCDMap());

                                                oS.writeLinkUtilizationsToFile(graspWPMCWRDeadline.getSolution(), graph, rate);

                                                oS.writeDurationMap(graspWPMCWRDeadline.getDurationMap());

                                                oS.writeSRTCandidateRoutesToFile(graspWPMCWRDeadline.getSRTUnicastCandidateList());

                                            }


                                        }
                                    }
                                }
                                case "WPMLWRCWRDeadline" -> {
                                    WPMLWRCWRDeadline graspWPMLWRCWRDeadline = new WPMLWRCWRDeadline(k);

                                    if(Objects.equals(wpmVersion, Constants.WPM_VERSION_V1)){
                                        logger.info("Solving problem using {}, {}, {}, LWR: {}, K: {}, wpmObjective: {}, CWR: {}, wSRT: {}, wTT: {}, wLength: {}, wUtil: {}, wpmVersion: {}, Thread Number: {}, Timeout: {}(sec), Metaheuristic Name: {}, Evaluator: {}", routing, pathFinderMethod, algorithm, lwr, k, wpmObjective, cwr, wSRT, wTT, wLength, wUtil, wpmVersion, threadNumber, timeout, metaheuristicName, evaluatorName);
                                    } else if (Objects.equals(wpmVersion, Constants.WPM_VERSION_V2)) {
                                        logger.info("Solving problem using {}, {}, {}, LWR: {}, K: {}, wpmObjective: {}, CWR: {}, wSRT: {}, wTT: {}, wLength: {}, wUtil: {}, wpmVersion: {}, wpmValueType: {}, Thread Number: {}, Timeout: {}(sec), Metaheuristic Name: {}, Evaluator: {}", routing, pathFinderMethod, algorithm, lwr, k, wpmObjective, cwr, wSRT, wTT, wLength, wUtil, wpmVersion, wpmValueType, threadNumber, timeout, metaheuristicName, evaluatorName);
                                    }

                                    Solution solution = graspWPMLWRCWRDeadline.solve(graph, applicationList, threadNumber, lwr, wpmObjective, cwr, wSRT, wTT, wLength, wUtil, rate, wpmVersion, wpmValueType, metaheuristicName, evaluator, Duration.ofSeconds(timeout));

                                    PHYWPMLWRCWRv1Holder phyWPMLWRCWRv1Holder = new PHYWPMLWRCWRv1Holder();
                                    PHYWPMLWRCWRv2Holder phyWPMLWRCWRv2Holder = new PHYWPMLWRCWRv2Holder();
                                    if(Objects.equals(wpmVersion, Constants.WPM_VERSION_V1)){
                                        phyWPMLWRCWRv1Holder.setTopologyName(topologyName);
                                        phyWPMLWRCWRv1Holder.setApplicationName(applicationName);
                                        phyWPMLWRCWRv1Holder.setRouting(routing);
                                        phyWPMLWRCWRv1Holder.setPathFindingMethod(pathFinderMethod);
                                        phyWPMLWRCWRv1Holder.setAlgorithm(algorithm);
                                        phyWPMLWRCWRv1Holder.setLWR(lwr);
                                        phyWPMLWRCWRv1Holder.setK(k);
                                        phyWPMLWRCWRv1Holder.setWPMObjective(wpmObjective);
                                        phyWPMLWRCWRv1Holder.setCWR(cwr);
                                        phyWPMLWRCWRv1Holder.setWSRT(wSRT);
                                        phyWPMLWRCWRv1Holder.setWTT(wTT);
                                        phyWPMLWRCWRv1Holder.setWLength(wLength);
                                        phyWPMLWRCWRv1Holder.setWUtil(wUtil);
                                        phyWPMLWRCWRv1Holder.setWPMVersion(wpmVersion);
                                        phyWPMLWRCWRv1Holder.setWPMObjective(wpmObjective);

                                        solution.getCost().writePHYWPMLWRCWRv1ResultToFile(phyWPMLWRCWRv1Holder);

                                    } else if (Objects.equals(wpmVersion, Constants.WPM_VERSION_V2)) {
                                        phyWPMLWRCWRv2Holder.setTopologyName(topologyName);
                                        phyWPMLWRCWRv2Holder.setApplicationName(applicationName);
                                        phyWPMLWRCWRv2Holder.setRouting(routing);
                                        phyWPMLWRCWRv2Holder.setPathFindingMethod(pathFinderMethod);
                                        phyWPMLWRCWRv2Holder.setAlgorithm(algorithm);
                                        phyWPMLWRCWRv2Holder.setLWR(lwr);
                                        phyWPMLWRCWRv2Holder.setK(k);
                                        phyWPMLWRCWRv2Holder.setWPMObjective(wpmObjective);
                                        phyWPMLWRCWRv2Holder.setCWR(cwr);
                                        phyWPMLWRCWRv2Holder.setWSRT(wSRT);
                                        phyWPMLWRCWRv2Holder.setWTT(wTT);
                                        phyWPMLWRCWRv2Holder.setWLength(wLength);
                                        phyWPMLWRCWRv2Holder.setWUtil(wUtil);
                                        phyWPMLWRCWRv2Holder.setWPMObjective(wpmObjective);
                                        phyWPMLWRCWRv2Holder.setWPMVersion(wpmVersion);
                                        phyWPMLWRCWRv2Holder.setWPMValueType(wpmValueType);

                                        solution.getCost().writePHYWPMLWRCWRv2ResultToFile(phyWPMLWRCWRv2Holder);
                                    }


                                    if (solution.getMulticastList() == null || solution.getMulticastList().isEmpty()) {
                                        logger.info(Constants.NO_SOLUTION_COULD_BE_FOUND);
                                    } else {
                                        if (solution.getCost().getTotalCost() == Double.MAX_VALUE) {
                                            logger.info(createFoundNoSolutionString(solution));
                                        } else {
                                            logger.info(createFoundSolutionString(solution));

                                            if(Objects.equals(wpmVersion, Constants.WPM_VERSION_V1)){

                                                PHYWPMLWRCWRv1OutputShaper oS = new PHYWPMLWRCWRv1OutputShaper(phyWPMLWRCWRv1Holder);

                                                oS.writeSolutionToFile(graspWPMLWRCWRDeadline.getSolution());

                                                oS.writeWCDsToFile(solution.getCost().getWCDMap());

                                                oS.writeLinkUtilizationsToFile(graspWPMLWRCWRDeadline.getSolution(), graph, rate);

                                                oS.writeDurationMap(graspWPMLWRCWRDeadline.getDurationMap());

                                                oS.writeSRTCandidateRoutesToFile(graspWPMLWRCWRDeadline.getSRTUnicastCandidateList());


                                            } else if (Objects.equals(wpmVersion, Constants.WPM_VERSION_V2)) {
                                                PHYWPMLWRCWRv2OutputShaper oS = new PHYWPMLWRCWRv2OutputShaper(phyWPMLWRCWRv2Holder);

                                                oS.writeSolutionToFile(graspWPMLWRCWRDeadline.getSolution());

                                                oS.writeWCDsToFile(solution.getCost().getWCDMap());

                                                oS.writeLinkUtilizationsToFile(graspWPMLWRCWRDeadline.getSolution(), graph, rate);

                                                oS.writeDurationMap(graspWPMLWRCWRDeadline.getDurationMap());

                                                oS.writeSRTCandidateRoutesToFile(graspWPMLWRCWRDeadline.getSRTUnicastCandidateList());

                                            }


                                        }
                                    }
                                }
                                default -> throw new InputMismatchException("Aborting: " + routing + ", " + pathFinderMethod + ", " + algorithm + " unrecognized!");
                            }
                        }
                        case "pathPenalization" -> {
                            switch (algorithm) {
                                case "WPMDeadline" -> {
                                    ktu.kaganndemirr.routing.phy.pathpenalization.heuristic.WPMDeadline wpmDeadline = new ktu.kaganndemirr.routing.phy.pathpenalization.heuristic.WPMDeadline(k);

                                    if(Objects.equals(wpmVersion, Constants.WPM_VERSION_V1)){
                                        logger.info("Solving problem using {}, {}, {}, K: {}, wpmObjective: {}, wSRT: {}, wTT: {}, wLength: {}, wUtil: {}, wpmVersion: {}", routing, pathFinderMethod, algorithm, k, wpmObjective, wSRT, wTT, wLength, wUtil, wpmVersion);
                                    } else if (Objects.equals(wpmVersion, Constants.WPM_VERSION_V2)) {
                                        logger.info("Solving problem using {}, {}, {}, K: {}, wpmObjective: {}, wSRT: {}, wTT: {}, wLength: {}, wUtil: {}, wpmVersion: {}, wpmValueType: {}", routing, pathFinderMethod, algorithm, k, wpmObjective, wSRT, wTT, wLength, wUtil, wpmVersion, wpmValueType);
                                    }

                                    Solution solution = wpmDeadline.solve(graph, applicationList, wpmObjective, wSRT, wTT, wLength, wUtil, rate, wpmVersion, wpmValueType, evaluator);

                                    PHYWPMv1Holder phyWPMv1Holder = new PHYWPMv1Holder();
                                    PHYWPMv2Holder phyWPMv2Holder = new PHYWPMv2Holder();
                                    if(Objects.equals(wpmVersion, Constants.WPM_VERSION_V1)){
                                        phyWPMv1Holder.setTopologyName(topologyName);
                                        phyWPMv1Holder.setApplicationName(applicationName);
                                        phyWPMv1Holder.setRouting(routing);
                                        phyWPMv1Holder.setPathFindingMethod(pathFinderMethod);
                                        phyWPMv1Holder.setAlgorithm(algorithm);
                                        phyWPMv1Holder.setK(k);
                                        phyWPMv1Holder.setWPMObjective(wpmObjective);
                                        phyWPMv1Holder.setWSRT(wSRT);
                                        phyWPMv1Holder.setWTT(wTT);
                                        phyWPMv1Holder.setWLength(wLength);
                                        phyWPMv1Holder.setWUtil(wUtil);
                                        phyWPMv1Holder.setWPMObjective(wpmObjective);
                                        phyWPMv1Holder.setWPMVersion(wpmVersion);

                                        solution.getCost().writePHYWPMv1ResultToFile(phyWPMv1Holder);

                                    } else if (Objects.equals(wpmVersion, Constants.WPM_VERSION_V2)) {
                                        phyWPMv2Holder.setTopologyName(topologyName);
                                        phyWPMv2Holder.setApplicationName(applicationName);
                                        phyWPMv2Holder.setRouting(routing);
                                        phyWPMv2Holder.setPathFindingMethod(pathFinderMethod);
                                        phyWPMv2Holder.setAlgorithm(algorithm);
                                        phyWPMv2Holder.setK(k);
                                        phyWPMv2Holder.setWPMObjective(wpmObjective);
                                        phyWPMv2Holder.setWSRT(wSRT);
                                        phyWPMv2Holder.setWTT(wTT);
                                        phyWPMv2Holder.setWLength(wLength);
                                        phyWPMv2Holder.setWUtil(wUtil);
                                        phyWPMv2Holder.setWPMObjective(wpmObjective);
                                        phyWPMv2Holder.setWPMVersion(wpmVersion);
                                        phyWPMv2Holder.setWPMValueType(wpmValueType);

                                        solution.getCost().writePHYWPMv2ResultToFile(phyWPMv2Holder);
                                    }


                                    if (solution.getMulticastList() == null || solution.getMulticastList().isEmpty()) {
                                        logger.info(Constants.NO_SOLUTION_COULD_BE_FOUND);
                                    } else {
                                        if (solution.getCost().getTotalCost() == Double.MAX_VALUE) {
                                            logger.info(createFoundNoSolutionString(solution));
                                        } else {
                                            logger.info(createFoundSolutionString(solution));

                                            if(Objects.equals(wpmVersion, Constants.WPM_VERSION_V1)){

                                                PHYWPMv1OutputShaper oS = new PHYWPMv1OutputShaper(phyWPMv1Holder);

                                                oS.writeSolutionToFile(wpmDeadline.getSolution());

                                                oS.writeWCDsToFile(solution.getCost().getWCDMap());

                                                oS.writeLinkUtilizationsToFile(wpmDeadline.getSolution(), graph, rate);

                                                oS.writeDurationMap(wpmDeadline.getDurationMap());

                                                oS.writeSRTCandidateRoutesToFile(wpmDeadline.getSRTUnicastCandidateList());


                                            } else if (Objects.equals(wpmVersion, Constants.WPM_VERSION_V2)) {
                                                PHYWPMv2OutputShaper oS = new PHYWPMv2OutputShaper(phyWPMv2Holder);

                                                oS.writeSolutionToFile(wpmDeadline.getSolution());

                                                oS.writeWCDsToFile(solution.getCost().getWCDMap());

                                                oS.writeLinkUtilizationsToFile(wpmDeadline.getSolution(), graph, rate);

                                                oS.writeDurationMap(wpmDeadline.getDurationMap());

                                                oS.writeSRTCandidateRoutesToFile(wpmDeadline.getSRTUnicastCandidateList());

                                            }


                                        }
                                    }
                                }
                                case "WPMLWRDeadline" -> {
                                    ktu.kaganndemirr.routing.phy.pathpenalization.metaheuristic.WPMLWRDeadline graspWPMLWRDeadline = new ktu.kaganndemirr.routing.phy.pathpenalization.metaheuristic.WPMLWRDeadline(k);

                                    if(Objects.equals(wpmVersion, Constants.WPM_VERSION_V1)){
                                        logger.info("Solving problem using {}, {}, {}, LWR: {}, K:{}, wpmObjective: {}, wSRT: {}, wTT: {}, wLength: {}, wUtil: {}, wpmVersion: {}, Metaheuristic Name: {}, Evaluator: {} Timeout: {}(sec)", routing, pathFinderMethod, algorithm, lwr, k, wpmObjective, wSRT, wTT, wLength, wUtil, wpmVersion, metaheuristicName, evaluatorName, timeout);
                                    } else if (Objects.equals(wpmVersion, Constants.WPM_VERSION_V2)) {
                                        logger.info("Solving problem using {}, {}, {}, LWR: {}, K:{}, wpmObjective: {}, wSRT: {}, wTT: {}, wLength: {}, wUtil: {}, wpmVersion: {}, wpmValueType: {}, Metaheuristic Name: {}, Evaluator: {}, Timeout: {}(sec)", routing, pathFinderMethod, algorithm, lwr, k, wpmObjective, wSRT, wTT, wLength, wUtil, wpmVersion, wpmValueType, metaheuristicName, evaluatorName, timeout);
                                    }

                                    Solution solution = graspWPMLWRDeadline.solve(graph, applicationList, threadNumber, lwr, wpmObjective, wSRT, wTT, wLength, wUtil, rate, wpmVersion, wpmValueType, metaheuristicName, evaluator, Duration.ofSeconds(timeout));

                                    PHYWPMLWRv1Holder phyWPMLWRv1Holder = new PHYWPMLWRv1Holder();
                                    PHYWPMLWRv2Holder phyWPMLWRv2Holder = new PHYWPMLWRv2Holder();
                                    if(Objects.equals(wpmVersion, Constants.WPM_VERSION_V1)){
                                        phyWPMLWRv1Holder.setTopologyName(topologyName);
                                        phyWPMLWRv1Holder.setApplicationName(applicationName);
                                        phyWPMLWRv1Holder.setRouting(routing);
                                        phyWPMLWRv1Holder.setPathFindingMethod(pathFinderMethod);
                                        phyWPMLWRv1Holder.setAlgorithm(algorithm);
                                        phyWPMLWRv1Holder.setLWR(lwr);
                                        phyWPMLWRv1Holder.setK(k);
                                        phyWPMLWRv2Holder.setWPMObjective(wpmObjective);
                                        phyWPMLWRv1Holder.setWSRT(wSRT);
                                        phyWPMLWRv1Holder.setWTT(wTT);
                                        phyWPMLWRv1Holder.setWLength(wLength);
                                        phyWPMLWRv1Holder.setWUtil(wUtil);
                                        phyWPMLWRv1Holder.setWPMVersion(wpmVersion);
                                        phyWPMLWRv1Holder.setWPMObjective(wpmObjective);

                                        solution.getCost().writePHYWPMLWRv1ResultToFile(phyWPMLWRv1Holder);

                                    } else if (Objects.equals(wpmVersion, Constants.WPM_VERSION_V2)) {
                                        phyWPMLWRv2Holder.setTopologyName(topologyName);
                                        phyWPMLWRv2Holder.setApplicationName(applicationName);
                                        phyWPMLWRv2Holder.setRouting(routing);
                                        phyWPMLWRv2Holder.setPathFindingMethod(pathFinderMethod);
                                        phyWPMLWRv2Holder.setAlgorithm(algorithm);
                                        phyWPMLWRv2Holder.setLWR(lwr);
                                        phyWPMLWRv2Holder.setK(k);
                                        phyWPMLWRv2Holder.setWPMObjective(wpmObjective);
                                        phyWPMLWRv2Holder.setWSRT(wSRT);
                                        phyWPMLWRv2Holder.setWTT(wTT);
                                        phyWPMLWRv2Holder.setWLength(wLength);
                                        phyWPMLWRv2Holder.setWUtil(wUtil);
                                        phyWPMLWRv2Holder.setWPMObjective(wpmObjective);
                                        phyWPMLWRv2Holder.setWPMVersion(wpmVersion);
                                        phyWPMLWRv2Holder.setWPMValueType(wpmValueType);

                                        solution.getCost().writePHYWPMLWRv2ResultToFile(phyWPMLWRv2Holder);
                                    }


                                    if (solution.getMulticastList() == null || solution.getMulticastList().isEmpty()) {
                                        logger.info(Constants.NO_SOLUTION_COULD_BE_FOUND);
                                    } else {
                                        if (solution.getCost().getTotalCost() == Double.MAX_VALUE) {
                                            logger.info(createFoundNoSolutionString(solution));
                                        } else {
                                            logger.info(createFoundSolutionString(solution));

                                            if(Objects.equals(wpmVersion, Constants.WPM_VERSION_V1)){

                                                PHYWPMLWRv1OutputShaper oS = new PHYWPMLWRv1OutputShaper(phyWPMLWRv1Holder);

                                                oS.writeSolutionToFile(graspWPMLWRDeadline.getSolution());

                                                oS.writeWCDsToFile(solution.getCost().getWCDMap());

                                                oS.writeLinkUtilizationsToFile(graspWPMLWRDeadline.getSolution(), graph, rate);

                                                oS.writeDurationMap(graspWPMLWRDeadline.getDurationMap());

                                                oS.writeSRTCandidateRoutesToFile(graspWPMLWRDeadline.getSRTUnicastCandidateList());


                                            } else if (Objects.equals(wpmVersion, Constants.WPM_VERSION_V2)) {
                                                PHYWPMLWRv2OutputShaper oS = new PHYWPMLWRv2OutputShaper(phyWPMLWRv2Holder);

                                                oS.writeSolutionToFile(graspWPMLWRDeadline.getSolution());

                                                oS.writeWCDsToFile(solution.getCost().getWCDMap());

                                                oS.writeLinkUtilizationsToFile(graspWPMLWRDeadline.getSolution(), graph, rate);

                                                oS.writeDurationMap(graspWPMLWRDeadline.getDurationMap());

                                                oS.writeSRTCandidateRoutesToFile(graspWPMLWRDeadline.getSRTUnicastCandidateList());

                                            }


                                        }
                                    }
                                }
                                case "WPMCWRDeadline" -> {
                                    ktu.kaganndemirr.routing.phy.pathpenalization.metaheuristic.WPMCWRDeadline graspWPMCWRDeadline = new ktu.kaganndemirr.routing.phy.pathpenalization.metaheuristic.WPMCWRDeadline(k);

                                    if(Objects.equals(wpmVersion, Constants.WPM_VERSION_V1)){
                                        logger.info("Solving problem using {}, {}, {}, K:{}, wpmObjective: {}, CWR: {}, wpmVersion: {}, Metaheuristic Name:{}, Evaluator: {}, Timeout: {}(sec)", routing, pathFinderMethod, algorithm, k, wpmObjective, cwr, wpmVersion, metaheuristicName, evaluatorName, timeout);
                                    } else if (Objects.equals(wpmVersion, Constants.WPM_VERSION_V2)) {
                                        logger.info("Solving problem using {}, {}, {}, K:{}, wpmObjective: {}, CWR: {}, wpmVersion: {}, wpmValueType: {}, Metaheuristic Name: {}, Evaluator: {}, Timeout: {}(sec)", routing, pathFinderMethod, algorithm, k, wpmObjective, cwr, wpmVersion, wpmValueType, metaheuristicName, evaluatorName, timeout);
                                    }

                                    Solution solution = graspWPMCWRDeadline.solve(graph, applicationList, threadNumber, wpmObjective, cwr, rate, wpmVersion, wpmValueType, metaheuristicName, evaluator, Duration.ofSeconds(timeout));

                                    PHYWPMCWRv1Holder phyWPMCWRv1Holder = new PHYWPMCWRv1Holder();
                                    PHYWPMCWRv2Holder phyWPMCWRv2Holder = new PHYWPMCWRv2Holder();
                                    if(Objects.equals(wpmVersion, Constants.WPM_VERSION_V1)){
                                        phyWPMCWRv1Holder.setTopologyName(topologyName);
                                        phyWPMCWRv1Holder.setApplicationName(applicationName);
                                        phyWPMCWRv1Holder.setRouting(routing);
                                        phyWPMCWRv1Holder.setPathFindingMethod(pathFinderMethod);
                                        phyWPMCWRv1Holder.setAlgorithm(algorithm);
                                        phyWPMCWRv1Holder.setK(k);
                                        phyWPMCWRv1Holder.setWPMObjective(wpmObjective);
                                        phyWPMCWRv1Holder.setCWR(cwr);
                                        phyWPMCWRv1Holder.setWPMVersion(wpmVersion);

                                        solution.getCost().writePHYWPMCWRv1ResultToFile(phyWPMCWRv1Holder);

                                    } else if (Objects.equals(wpmVersion, Constants.WPM_VERSION_V2)) {
                                        phyWPMCWRv2Holder.setTopologyName(topologyName);
                                        phyWPMCWRv2Holder.setApplicationName(applicationName);
                                        phyWPMCWRv2Holder.setRouting(routing);
                                        phyWPMCWRv2Holder.setPathFindingMethod(pathFinderMethod);
                                        phyWPMCWRv2Holder.setAlgorithm(algorithm);
                                        phyWPMCWRv2Holder.setK(k);
                                        phyWPMCWRv2Holder.setWPMObjective(wpmObjective);
                                        phyWPMCWRv2Holder.setCWR(cwr);
                                        phyWPMCWRv2Holder.setWPMVersion(wpmVersion);
                                        phyWPMCWRv2Holder.setWPMValueType(wpmValueType);

                                        solution.getCost().writePHYWPMCWRv2ResultToFile(phyWPMCWRv2Holder);
                                    }


                                    if (solution.getMulticastList() == null || solution.getMulticastList().isEmpty()) {
                                        logger.info(Constants.NO_SOLUTION_COULD_BE_FOUND);
                                    } else {
                                        if (solution.getCost().getTotalCost() == Double.MAX_VALUE) {
                                            logger.info(createFoundNoSolutionString(solution));
                                        } else {
                                            logger.info(createFoundSolutionString(solution));

                                            if(Objects.equals(wpmVersion, Constants.WPM_VERSION_V1)){

                                                PHYWPMCWRv1OutputShaper oS = new PHYWPMCWRv1OutputShaper(phyWPMCWRv1Holder);

                                                oS.writeSolutionToFile(graspWPMCWRDeadline.getSolution());

                                                oS.writeWCDsToFile(solution.getCost().getWCDMap());

                                                oS.writeLinkUtilizationsToFile(graspWPMCWRDeadline.getSolution(), graph, rate);

                                                oS.writeDurationMap(graspWPMCWRDeadline.getDurationMap());

                                                oS.writeSRTCandidateRoutesToFile(graspWPMCWRDeadline.getSRTUnicastCandidateList());


                                            } else if (Objects.equals(wpmVersion, Constants.WPM_VERSION_V2)) {
                                                PHYWPMCWRv2OutputShaper oS = new PHYWPMCWRv2OutputShaper(phyWPMCWRv2Holder);

                                                oS.writeSolutionToFile(graspWPMCWRDeadline.getSolution());

                                                oS.writeWCDsToFile(solution.getCost().getWCDMap());

                                                oS.writeLinkUtilizationsToFile(graspWPMCWRDeadline.getSolution(), graph, rate);

                                                oS.writeDurationMap(graspWPMCWRDeadline.getDurationMap());

                                                oS.writeSRTCandidateRoutesToFile(graspWPMCWRDeadline.getSRTUnicastCandidateList());

                                            }


                                        }
                                    }
                                }
                                case "WPMLWRCWRDeadline" -> {
                                    ktu.kaganndemirr.routing.phy.pathpenalization.metaheuristic.WPMLWRCWRDeadline graspWPMLWRCWRDeadline = new ktu.kaganndemirr.routing.phy.pathpenalization.metaheuristic.WPMLWRCWRDeadline(k);

                                    if(Objects.equals(wpmVersion, Constants.WPM_VERSION_V1)){
                                        logger.info("Solving problem using {}, {}, {}, LWR: {}, K: {}, wpmObjective: {}, CWR: {}, wSRT: {}, wTT: {}, wLength: {}, wUtil: {}, wpmVersion: {}, Thread Number: {}, Timeout: {}(sec), Metaheuristic Name: {}, Evaluator: {}", routing, pathFinderMethod, algorithm, lwr, k, wpmObjective, cwr, wSRT, wTT, wLength, wUtil, wpmVersion, threadNumber, timeout, metaheuristicName, evaluatorName);
                                    } else if (Objects.equals(wpmVersion, Constants.WPM_VERSION_V2)) {
                                        logger.info("Solving problem using {}, {}, {}, LWR: {}, K: {}, wpmObjective: {}, CWR: {}, wSRT: {}, wTT: {}, wLength: {}, wUtil: {}, wpmVersion: {}, wpmValueType: {}, Thread Number: {}, Timeout: {}(sec), Metaheuristic Name: {}, Evaluator: {}", routing, pathFinderMethod, algorithm, lwr, k, wpmObjective, cwr, wSRT, wTT, wLength, wUtil, wpmVersion, wpmValueType, threadNumber, timeout, metaheuristicName, evaluatorName);
                                    }

                                    Solution solution = graspWPMLWRCWRDeadline.solve(graph, applicationList, threadNumber, lwr, wpmObjective, cwr, wSRT, wTT, wLength, wUtil, rate, wpmVersion, wpmValueType, metaheuristicName, evaluator, Duration.ofSeconds(timeout));

                                    PHYWPMLWRCWRv1Holder phyWPMLWRCWRv1Holder = new PHYWPMLWRCWRv1Holder();
                                    PHYWPMLWRCWRv2Holder phyWPMLWRCWRv2Holder = new PHYWPMLWRCWRv2Holder();
                                    if(Objects.equals(wpmVersion, Constants.WPM_VERSION_V1)){
                                        phyWPMLWRCWRv1Holder.setTopologyName(topologyName);
                                        phyWPMLWRCWRv1Holder.setApplicationName(applicationName);
                                        phyWPMLWRCWRv1Holder.setRouting(routing);
                                        phyWPMLWRCWRv1Holder.setPathFindingMethod(pathFinderMethod);
                                        phyWPMLWRCWRv1Holder.setAlgorithm(algorithm);
                                        phyWPMLWRCWRv1Holder.setLWR(lwr);
                                        phyWPMLWRCWRv1Holder.setK(k);
                                        phyWPMLWRCWRv1Holder.setWPMObjective(wpmObjective);
                                        phyWPMLWRCWRv1Holder.setCWR(cwr);
                                        phyWPMLWRCWRv1Holder.setWSRT(wSRT);
                                        phyWPMLWRCWRv1Holder.setWTT(wTT);
                                        phyWPMLWRCWRv1Holder.setWLength(wLength);
                                        phyWPMLWRCWRv1Holder.setWUtil(wUtil);
                                        phyWPMLWRCWRv1Holder.setWPMVersion(wpmVersion);
                                        phyWPMLWRCWRv1Holder.setWPMObjective(wpmObjective);

                                        solution.getCost().writePHYWPMLWRCWRv1ResultToFile(phyWPMLWRCWRv1Holder);

                                    } else if (Objects.equals(wpmVersion, Constants.WPM_VERSION_V2)) {
                                        phyWPMLWRCWRv2Holder.setTopologyName(topologyName);
                                        phyWPMLWRCWRv2Holder.setApplicationName(applicationName);
                                        phyWPMLWRCWRv2Holder.setRouting(routing);
                                        phyWPMLWRCWRv2Holder.setPathFindingMethod(pathFinderMethod);
                                        phyWPMLWRCWRv2Holder.setAlgorithm(algorithm);
                                        phyWPMLWRCWRv2Holder.setLWR(lwr);
                                        phyWPMLWRCWRv2Holder.setK(k);
                                        phyWPMLWRCWRv2Holder.setWPMObjective(wpmObjective);
                                        phyWPMLWRCWRv2Holder.setCWR(cwr);
                                        phyWPMLWRCWRv2Holder.setWSRT(wSRT);
                                        phyWPMLWRCWRv2Holder.setWTT(wTT);
                                        phyWPMLWRCWRv2Holder.setWLength(wLength);
                                        phyWPMLWRCWRv2Holder.setWUtil(wUtil);
                                        phyWPMLWRCWRv2Holder.setWPMObjective(wpmObjective);
                                        phyWPMLWRCWRv2Holder.setWPMVersion(wpmVersion);
                                        phyWPMLWRCWRv2Holder.setWPMValueType(wpmValueType);

                                        solution.getCost().writePHYWPMLWRCWRv2ResultToFile(phyWPMLWRCWRv2Holder);
                                    }


                                    if (solution.getMulticastList() == null || solution.getMulticastList().isEmpty()) {
                                        logger.info(Constants.NO_SOLUTION_COULD_BE_FOUND);
                                    } else {
                                        if (solution.getCost().getTotalCost() == Double.MAX_VALUE) {
                                            logger.info(createFoundNoSolutionString(solution));
                                        } else {
                                            logger.info(createFoundSolutionString(solution));

                                            if(Objects.equals(wpmVersion, Constants.WPM_VERSION_V1)){

                                                PHYWPMLWRCWRv1OutputShaper oS = new PHYWPMLWRCWRv1OutputShaper(phyWPMLWRCWRv1Holder);

                                                oS.writeSolutionToFile(graspWPMLWRCWRDeadline.getSolution());

                                                oS.writeWCDsToFile(solution.getCost().getWCDMap());

                                                oS.writeLinkUtilizationsToFile(graspWPMLWRCWRDeadline.getSolution(), graph, rate);

                                                oS.writeDurationMap(graspWPMLWRCWRDeadline.getDurationMap());

                                                oS.writeSRTCandidateRoutesToFile(graspWPMLWRCWRDeadline.getSRTUnicastCandidateList());


                                            } else if (Objects.equals(wpmVersion, Constants.WPM_VERSION_V2)) {
                                                PHYWPMLWRCWRv2OutputShaper oS = new PHYWPMLWRCWRv2OutputShaper(phyWPMLWRCWRv2Holder);

                                                oS.writeSolutionToFile(graspWPMLWRCWRDeadline.getSolution());

                                                oS.writeWCDsToFile(solution.getCost().getWCDMap());

                                                oS.writeLinkUtilizationsToFile(graspWPMLWRCWRDeadline.getSolution(), graph, rate);

                                                oS.writeDurationMap(graspWPMLWRCWRDeadline.getDurationMap());

                                                oS.writeSRTCandidateRoutesToFile(graspWPMLWRCWRDeadline.getSRTUnicastCandidateList());

                                            }


                                        }
                                    }
                                }
                                default -> throw new InputMismatchException("Aborting: " + routing + ", " + pathFinderMethod + ", " + algorithm + " unrecognized!");
                            }
                        }

                        default -> throw new InputMismatchException("Aborting: Solver " + routing + ", " + pathFinderMethod + " unrecognized!");
                    }
                }
                default -> throw new InputMismatchException("Aborting: " + routing + " unrecognized!");
            }
            //endregion

        } catch (ParseException e) {
            e.printStackTrace();
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