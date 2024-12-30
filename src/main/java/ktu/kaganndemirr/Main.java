package ktu.kaganndemirr;

import ktu.kaganndemirr.application.Application;
import ktu.kaganndemirr.architecture.GCLEdge;
import ktu.kaganndemirr.architecture.Node;
import ktu.kaganndemirr.evaluator.AVBLatencyMathTSNConfigurationFramework;
import ktu.kaganndemirr.evaluator.AVBLatencyMathTSNRoutingOptimization;
import ktu.kaganndemirr.evaluator.Evaluator;
import ktu.kaganndemirr.parser.ApplicationParser;
import ktu.kaganndemirr.parser.TopologyParser;
import org.apache.commons.cli.*;
import org.jgrapht.Graph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ktu.kaganndemirr.util.Constants;

import java.io.File;
import java.time.Duration;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class.getSimpleName());

    //region <Command line options>
    private static final String APP_ARG = "app";
    private static final String NET_ARG = "net";
    private static final String RATE_ARG = "rate";
    private static final String EVALUATOR_ARG = "evaluator";
    private static final String K_ARG = "k";
    private static final String THREAD_NUMBER_ARG = "thread";
    private static final String TIMEOUT_ARG = "timeout";

    private static final String ROUTING_ARG = "routing";
    private static final String PATH_FINDER_METHOD_ARG = "pathFinderMethod";
    private static final String ALGORITHM_ARG = "algorithm";

    private static final String WEIGHTED_PRODUCT_MODEL_OBJECTIVE_ARG = "weightedProductModelObjective";
    private static final String W_SOFT_REAL_TIME_ARG = "wSoftRealTime";
    private static final String W_TIME_TRIGGERED_ARG = "wTimeTriggered";
    private static final String W_LENGTH_ARG = "wLength";
    private static final String W_UTIL_ARG = "wUtil";
    private static final String LINK_WEIGHT_RANDOMIZATION_ARG = "linkWeightRandomization";
    private static final String CRITERIA_WEIGHT_RANDOMIZATION_ARG = "criteriaWeightRandomization";
    private static final String WEIGHTED_PRODUCT_MODEL_VERSION_ARG = "weightedProductModelVersion";
    private static final String WEIGHTED_PRODUCT_MODEL_VALUE_TYPE_ARG = "weightedProductModelValueType";

    private static final String IDLE_SLOPE_ARG = "idleSlope";

    private static final String LOG_ARG = "log";

    private static final String TSN_SIMULATION_VERSION_ARG = "TSNSimulationVersion";
    //endregion

    public static void main(String[] args) {
        //region <Default Values>

        //Default value of rate (mbps)
        int rate = 1000;
        //Default Evaluator
        Evaluator evaluator = new AVBLatencyMathTSNConfigurationFramework();
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
        String weightedProductModelObjective = "srtTTLength";
        //Default wSoftRealTime
        double wSoftRealTime = 0;
        //Default wTimeTriggered
        double wTimeTriggered = 0;
        //Default wLength
        double wLength = 0;
        //Default wUtil
        double wUtil = 0;
        //Default linkWeightRandomization
        String linkWeightRandomization = "headsOrTails";
        //Default criteriaWeightRandomization
        String criteriaWeightRandomization = "secureRandom";
        //Default weightedProductModelVersion
        String weightedProductModelVersion = "v1";
        //Default weightedProductModelValueType
        String weightedProductModelValueType = "actual";

        //Default IdleSlope
        double idleSlope = 0.75;

        //Default Log
        String log = null;

        //Default TSN Simulation Version
        String TSNSimulationVersion = "TSNConfigurationFramework";

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

        options.addOption(WEIGHTED_PRODUCT_MODEL_OBJECTIVE_ARG, true, "Weighted Product Model Objective (Default: srtTTLength) (Choices: srtTT, srtTTLength, srtTTLengthUtil)");
        options.addOption(W_SOFT_REAL_TIME_ARG, true, "SRT Weight (Default: 0");
        options.addOption(W_TIME_TRIGGERED_ARG, true, "TT Weight (Default: 0");
        options.addOption(W_LENGTH_ARG, true, "Candidate Path Length Weight (Default: 0");
        options.addOption(W_UTIL_ARG, true, "Utilization Weight (Default: 0");

        options.addOption(LINK_WEIGHT_RANDOMIZATION_ARG, true, "Link weight randomization method (Default: headsOrTails)");
        options.addOption(CRITERIA_WEIGHT_RANDOMIZATION_ARG, true, "Criteria weight randomization method (Default: secureRandom)");
        options.addOption(WEIGHTED_PRODUCT_MODEL_VERSION_ARG, true, "Weighted Product Model Version (Default: v1)");
        options.addOption(WEIGHTED_PRODUCT_MODEL_VALUE_TYPE_ARG, true, "Weighted Product Model Value Type for v2 (Default: actual)");

        options.addOption(IDLE_SLOPE_ARG, true, "Idle slope for CLASS_A (Default: 0.75)");

        options.addOption(LOG_ARG, true, "Log Type (Default: No Log) (Choices: info, debug)");

        options.addOption(TSN_SIMULATION_VERSION_ARG, true, "TSN Simulation Version (Default: TSNConfigurationFramework) (Choices: TSNConfigurationFramework, TSNRoutingOptimization)");

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
                if (Objects.equals(line.getOptionValue(LOG_ARG), Constants.AVB_LATENCY_MATH_VERSION_TSN_CONFIGURATION_FRAMEWORK)) {
                    evaluator = new AVBLatencyMathTSNConfigurationFramework();
                } else if (Objects.equals(line.getOptionValue(EVALUATOR_ARG), Constants.AVB_LATENCY_MATH_VERSION_TSN_ROUTING_OPTIMIZATION)) {
                    evaluator = new AVBLatencyMathTSNRoutingOptimization();
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

            if (line.hasOption(PATH_FINDER_METHOD_ARG)) {
                pathFindingMethod = line.getOptionValue(PATH_FINDER_METHOD_ARG);
            }

            if (line.hasOption(ALGORITHM_ARG)) {
                algorithm = line.getOptionValue(ALGORITHM_ARG);
            }

            if (line.hasOption(WEIGHTED_PRODUCT_MODEL_OBJECTIVE_ARG)) {
                weightedProductModelObjective = line.getOptionValue(WEIGHTED_PRODUCT_MODEL_OBJECTIVE_ARG);
            }

            if (line.hasOption(W_SOFT_REAL_TIME_ARG)) {
                wSoftRealTime = Double.parseDouble(line.getOptionValue(W_SOFT_REAL_TIME_ARG));
            }

            if (line.hasOption(W_TIME_TRIGGERED_ARG)) {
                wTimeTriggered = Double.parseDouble(line.getOptionValue(W_TIME_TRIGGERED_ARG));
            }

            if (line.hasOption(W_LENGTH_ARG)) {
                wLength = Double.parseDouble(line.getOptionValue(W_LENGTH_ARG));
            }

            if (line.hasOption(W_UTIL_ARG)) {
                wUtil = Double.parseDouble(line.getOptionValue(W_UTIL_ARG));
            }

            if (line.hasOption(LINK_WEIGHT_RANDOMIZATION_ARG)) {
                linkWeightRandomization = line.getOptionValue(LINK_WEIGHT_RANDOMIZATION_ARG);
            }

            if (line.hasOption(CRITERIA_WEIGHT_RANDOMIZATION_ARG)) {
                criteriaWeightRandomization = line.getOptionValue(CRITERIA_WEIGHT_RANDOMIZATION_ARG);
            }

            if (line.hasOption(WEIGHTED_PRODUCT_MODEL_VERSION_ARG)) {
                weightedProductModelVersion = line.getOptionValue(WEIGHTED_PRODUCT_MODEL_VERSION_ARG);
            }

            if (line.hasOption(WEIGHTED_PRODUCT_MODEL_VALUE_TYPE_ARG)) {
                weightedProductModelValueType = line.getOptionValue(WEIGHTED_PRODUCT_MODEL_VALUE_TYPE_ARG);
            }

            if (line.hasOption(IDLE_SLOPE_ARG)) {
                idleSlope = Double.parseDouble(line.getOptionValue(IDLE_SLOPE_ARG));
            }

            if (line.hasOption(LOG_ARG)) {
                if (Objects.equals(line.getOptionValue(LOG_ARG), Constants.INFO)) {
                    System.setProperty(org.slf4j.simple.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "INFO");
                } else if (Objects.equals(line.getOptionValue(EVALUATOR_ARG), Constants.DEBUG)) {
                    System.setProperty(org.slf4j.simple.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "DEBUG");
                }
            }

            if (line.hasOption(TSN_SIMULATION_VERSION_ARG)) {
                TSNSimulationVersion = line.getOptionValue(TSN_SIMULATION_VERSION_ARG);
            }

            //endregion

            //Parse Topology
            logger.info("Parsing Topology from {}", net.getName());
            Graph<Node, GCLEdge> graph = TopologyParser.parse(net, rate, idleSlope);
            logger.info("Topology parsed!");
            //endregion

            //Parse Applications
            logger.info("Parsing application set from {}", app.getName());
            List<Application> apps = ApplicationParser.parse(app, TSNSimulationVersion, rate, graph);
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
                                case "WPMD" -> {
                                    s = new WPMD(k);

                                    if(Objects.equals(wpmVersion, Constants.WPMVERSIONV1)){
                                        logger.info("Solving problem using {}, {}, {}, K: {}, mcdmObjective: {}, wAVB: {}, wTT: {}, wLength: {}, wUtil: {}, wpmVersion: {}", solver, method, algorithm, k, mcdmObjective, wAVB, wTT, wLength, wUtil, wpmVersion);
                                    } else if (Objects.equals(wpmVersion, Constants.WPMVERSIONV2)) {
                                        logger.info("Solving problem using {}, {}, {}, K: {}, mcdmObjective: {}, wAVB: {}, wTT: {}, wLength: {}, wUtil: {}, wpmVersion: {}, wpmValueType: {}", solver, method, algorithm, k, mcdmObjective, wAVB, wTT, wLength, wUtil, wpmVersion, wpmValueType);
                                    }

                                    Solution sol = s.solveHWPM(graph, apps, evaluator, Double.parseDouble(wAVB), Double.parseDouble(wTT), Double.parseDouble(wLength), Double.parseDouble(wUtil), rate, mcdmObjective, wpmVersion, wpmValueType);

                                    PHYWPMv1Holder phyWPMv1Holder = new PHYWPMv1Holder();
                                    PHYWPMv2Holder phyWPMv2Holder = new PHYWPMv2Holder();
                                    if(Objects.equals(wpmVersion, Constants.WPMVERSIONV1)){
                                        phyWPMv1Holder.setTopologyName(topologyName);
                                        phyWPMv1Holder.setApplicationName(applicationName);
                                        phyWPMv1Holder.setSolver(solver);
                                        phyWPMv1Holder.setMethod(method);
                                        phyWPMv1Holder.setAlgorithm(algorithm);
                                        phyWPMv1Holder.setK(k);
                                        phyWPMv1Holder.setWAVB(Double.parseDouble(wAVB));
                                        phyWPMv1Holder.setWTT(Double.parseDouble(wTT));
                                        phyWPMv1Holder.setWLength(Double.parseDouble(wLength));
                                        phyWPMv1Holder.setWUtil(Double.parseDouble(wUtil));
                                        phyWPMv1Holder.setMCDMObjective(mcdmObjective);
                                        phyWPMv1Holder.setWPMVersion(wpmVersion);

                                        sol.getCost().writePHYWPMv1ResultToFile(phyWPMv1Holder);
                                    } else if (Objects.equals(wpmVersion, Constants.WPMVERSIONV2)) {
                                        phyWPMv2Holder.setTopologyName(topologyName);
                                        phyWPMv2Holder.setApplicationName(applicationName);
                                        phyWPMv2Holder.setSolver(solver);
                                        phyWPMv2Holder.setMethod(method);
                                        phyWPMv2Holder.setAlgorithm(algorithm);
                                        phyWPMv2Holder.setK(k);
                                        phyWPMv2Holder.setWAVB(Double.parseDouble(wAVB));
                                        phyWPMv2Holder.setWTT(Double.parseDouble(wTT));
                                        phyWPMv2Holder.setWLength(Double.parseDouble(wLength));
                                        phyWPMv2Holder.setWUtil(Double.parseDouble(wUtil));
                                        phyWPMv2Holder.setMCDMObjective(mcdmObjective);
                                        phyWPMv2Holder.setWPMVersion(wpmVersion);
                                        phyWPMv2Holder.setWPMValueType(wpmValueType);

                                        sol.getCost().writePHYWPMv2ResultToFile(phyWPMv2Holder);
                                    }


                                    if (sol.getRouting() == null || sol.getRouting().isEmpty()) {
                                        logger.info("No solution could be found!");
                                    } else {
                                        if (sol.getCost().getTotalCost() == Double.MAX_VALUE) {
                                            logger.info("Found No solution : {}", sol.getCost().toDetailedString());
                                        } else {
                                            logger.info("Found solution : {}", sol.getCost().toDetailedString());

                                            if(Objects.equals(wpmVersion, Constants.WPMVERSIONV1)){
                                                PHYWPMv1OutputShaper oS = new PHYWPMv1OutputShaper(phyWPMv1Holder);

                                                oS.writeSolutionToFile(s.getSolution());

                                                oS.writeWCDsToFile(sol.getCost().getaWCDMap());

                                                oS.writeLinkUtilizationsToFile(s.getSolution(), graph, rate);

                                                oS.writeDurationMap(s.getDurationMap());

                                                oS.writeAVBCandidatesTTIntersectionToFile(s.getAVBUnicastCandidates(), s.getTTUnicasts());

                                                oS.writeAVBTTIntersectionToFile(s.getSolution());

                                                oS.writeLinkAVBTTIntersectionToFile(graph, s.getSolution());

                                                oS.writeAVBCandidateRoutesToFile(s.getAVBUnicastCandidates());
                                            } else if (Objects.equals(wpmVersion, Constants.WPMVERSIONV2)) {
                                                PHYWPMv2OutputShaper oS = new PHYWPMv2OutputShaper(phyWPMv2Holder);

                                                oS.writeSolutionToFile(s.getSolution());

                                                oS.writeWCDsToFile(sol.getCost().getaWCDMap());

                                                oS.writeLinkUtilizationsToFile(s.getSolution(), graph, rate);

                                                oS.writeDurationMap(s.getDurationMap());

                                                oS.writeAVBCandidatesTTIntersectionToFile(s.getAVBUnicastCandidates(), s.getTTUnicasts());

                                                oS.writeAVBTTIntersectionToFile(s.getSolution());

                                                oS.writeLinkAVBTTIntersectionToFile(graph, s.getSolution());

                                                oS.writeAVBCandidateRoutesToFile(s.getAVBUnicastCandidates());
                                            }


                                        }
                                    }
                                }
                                case "GRASPWPMLWRD" -> {
                                    s = new GRASPWPMLWRD(k);

                                    if(Objects.equals(wpmVersion, Constants.WPMVERSIONV1)){
                                        logger.info("Solving problem using {}, {}, {}, K: {}, Dur: {}s, MCDMObjective: {}, wAVB: {}, wTT: {}, wLength: {}, wUtil: {}, RandomizationLWR: {}, wpmVersion: {}", solver, method, algorithm, k, Duration.ofSeconds(duration).toSeconds(), mcdmObjective, wAVB, wTT, wLength, wUtil, randomizationLWR, wpmVersion);
                                    }else {
                                        logger.info("Solving problem using {}, {}, {}, K: {}, Dur: {}s, MCDMObjective: {}, wAVB: {}, wTT: {}, wLength: {}, wUtil: {}, RandomizationLWR: {}, wpmVersion: {}, wpmValueType: {}", solver, method, algorithm, k, Duration.ofSeconds(duration).toSeconds(), mcdmObjective, wAVB, wTT, wLength, wUtil, randomizationLWR, wpmVersion, wpmValueType);
                                    }

                                    Solution sol = s.solveMWPMLWR(graph, apps, evaluator, Duration.ofSeconds(duration), threadNumber, Double.parseDouble(wAVB), Double.parseDouble(wTT), Double.parseDouble(wLength), Double.parseDouble(wUtil), rate, mcdmObjective, randomizationLWR, mhtype, maxIter, wpmVersion, wpmValueType);

                                    PHYWPMLWRv1Holder phyWPMLWRv1Holder = new PHYWPMLWRv1Holder();
                                    PHYWPMLWRv2Holder phyWPMLWRv2Holder = new PHYWPMLWRv2Holder();

                                    if(Objects.equals(wpmVersion, Constants.WPMVERSIONV1)){
                                        phyWPMLWRv1Holder.setTopologyName(topologyName);
                                        phyWPMLWRv1Holder.setApplicationName(applicationName);
                                        phyWPMLWRv1Holder.setSolver(solver);
                                        phyWPMLWRv1Holder.setMethod(method);
                                        phyWPMLWRv1Holder.setAlgorithm(algorithm);
                                        phyWPMLWRv1Holder.setK(k);
                                        phyWPMLWRv1Holder.setWAVB(Double.parseDouble(wAVB));
                                        phyWPMLWRv1Holder.setWTT(Double.parseDouble(wTT));
                                        phyWPMLWRv1Holder.setWLength(Double.parseDouble(wLength));
                                        phyWPMLWRv1Holder.setWUtil(Double.parseDouble(wUtil));
                                        phyWPMLWRv1Holder.setMCDMObjective(mcdmObjective);
                                        phyWPMLWRv1Holder.setRandomizationLWR(randomizationLWR);
                                        phyWPMLWRv1Holder.setWPMVersion(wpmVersion);

                                        sol.getCost().writePHYWPMLWRv1ResultToFile(phyWPMLWRv1Holder);

                                    }else {
                                        phyWPMLWRv2Holder.setTopologyName(topologyName);
                                        phyWPMLWRv2Holder.setApplicationName(applicationName);
                                        phyWPMLWRv2Holder.setSolver(solver);
                                        phyWPMLWRv2Holder.setMethod(method);
                                        phyWPMLWRv2Holder.setAlgorithm(algorithm);
                                        phyWPMLWRv2Holder.setK(k);
                                        phyWPMLWRv2Holder.setWAVB(Double.parseDouble(wAVB));
                                        phyWPMLWRv2Holder.setWTT(Double.parseDouble(wTT));
                                        phyWPMLWRv2Holder.setWLength(Double.parseDouble(wLength));
                                        phyWPMLWRv2Holder.setWUtil(Double.parseDouble(wUtil));
                                        phyWPMLWRv2Holder.setMCDMObjective(mcdmObjective);
                                        phyWPMLWRv2Holder.setRandomizationLWR(randomizationLWR);
                                        phyWPMLWRv2Holder.setWPMVersion(wpmVersion);
                                        phyWPMLWRv2Holder.setWPMValueType(wpmValueType);

                                        sol.getCost().writePHYWPMLWRv2ResultToFile(phyWPMLWRv2Holder);
                                    }

                                    if (sol.getRouting() == null || sol.getRouting().isEmpty()) {
                                        logger.info("No solution could be found!");
                                    } else {
                                        if (sol.getCost().getTotalCost() == Double.MAX_VALUE) {
                                            logger.info("Found No solution : {}", sol.getCost().toDetailedString());
                                        } else {
                                            logger.info("Found solution : {}", sol.getCost().toDetailedString());

                                            if(Objects.equals(wpmVersion, Constants.WPMVERSIONV1)){
                                                PHYWPMLWRv1OutputShaper oS = new PHYWPMLWRv1OutputShaper(phyWPMLWRv1Holder);

                                                oS.writeSolutionToFile(s.getSolution());

                                                oS.writeWCDsToFile(sol.getCost().getaWCDMap());

                                                oS.writeLinkUtilizationsToFile(s.getSolution(), graph, rate);

                                                oS.writeDurationMap(s.getDurationMap());

                                                oS.writeAVBCandidatesTTIntersectionToFile(s.getAVBUnicastCandidates(), s.getTTUnicasts());

                                                oS.writeAVBTTIntersectionToFile(s.getSolution());

                                                oS.writeLinkAVBTTIntersectionToFile(graph, s.getSolution());

                                                oS.writeAVBCandidateRoutesToFile(s.getAVBUnicastCandidates());
                                            }
                                            else {
                                                PHYWPMLWRv2OutputShaper oS = new PHYWPMLWRv2OutputShaper(phyWPMLWRv2Holder);

                                                oS.writeSolutionToFile(s.getSolution());

                                                oS.writeWCDsToFile(sol.getCost().getaWCDMap());

                                                oS.writeLinkUtilizationsToFile(s.getSolution(), graph, rate);

                                                oS.writeDurationMap(s.getDurationMap());

                                                oS.writeAVBCandidatesTTIntersectionToFile(s.getAVBUnicastCandidates(), s.getTTUnicasts());

                                                oS.writeAVBTTIntersectionToFile(s.getSolution());

                                                oS.writeLinkAVBTTIntersectionToFile(graph, s.getSolution());

                                                oS.writeAVBCandidateRoutesToFile(s.getAVBUnicastCandidates());
                                            }


                                        }
                                    }
                                }
                                case "GRASPWPMCWRD" -> {
                                    s = new GRASPWPMCWRD(k);

                                    if(Objects.equals(wpmVersion, Constants.WPMVERSIONV1)){
                                        logger.info("Solving problem using {}, {}, {}, K: {}, Dur: {}s, MCDMObjective: {}, RandomizationCWR: {}, wpmVersion: {}", solver, method, algorithm, k, Duration.ofSeconds(duration).toSeconds(), mcdmObjective, randomizationCWR, wpmVersion);
                                    }else {
                                        logger.info("Solving problem using {}, {}, {}, K: {}, Dur: {}s, MCDMObjective: {}, RandomizationCWR: {}, wpmVersion: {}, wpmValueType: {}", solver, method, algorithm, k, Duration.ofSeconds(duration).toSeconds(), mcdmObjective, randomizationCWR, wpmVersion, wpmValueType);
                                    }

                                    Solution sol = s.solveMWPMCWR(graph, apps, evaluator, Duration.ofSeconds(duration), threadNumber, mcdmObjective, randomizationCWR, rate, mhtype, maxIter, wpmVersion, wpmValueType);

                                    PHYWPMCWRv1Holder phyWPMCWRv1Holder = new PHYWPMCWRv1Holder();
                                    PHYWPMCWRv2Holder phyWPMCWRv2Holder = new PHYWPMCWRv2Holder();

                                    if(Objects.equals(wpmVersion, Constants.WPMVERSIONV1)){

                                        phyWPMCWRv1Holder.setTopologyName(topologyName);
                                        phyWPMCWRv1Holder.setApplicationName(applicationName);
                                        phyWPMCWRv1Holder.setSolver(solver);
                                        phyWPMCWRv1Holder.setMethod(method);
                                        phyWPMCWRv1Holder.setAlgorithm(algorithm);
                                        phyWPMCWRv1Holder.setK(k);
                                        phyWPMCWRv1Holder.setMCDMObjective(mcdmObjective);
                                        phyWPMCWRv1Holder.setRandomizationCWR(randomizationCWR);
                                        phyWPMCWRv1Holder.setWPMVersion(wpmVersion);

                                        sol.getCost().writePHYWPMCWRv1ResultToFile(phyWPMCWRv1Holder);
                                    }else {
                                        phyWPMCWRv2Holder.setTopologyName(topologyName);
                                        phyWPMCWRv2Holder.setApplicationName(applicationName);
                                        phyWPMCWRv2Holder.setSolver(solver);
                                        phyWPMCWRv2Holder.setMethod(method);
                                        phyWPMCWRv2Holder.setAlgorithm(algorithm);
                                        phyWPMCWRv2Holder.setK(k);
                                        phyWPMCWRv2Holder.setMCDMObjective(mcdmObjective);
                                        phyWPMCWRv2Holder.setRandomizationCWR(randomizationCWR);
                                        phyWPMCWRv2Holder.setWPMVersion(wpmVersion);
                                        phyWPMCWRv2Holder.setWPMValueType(wpmValueType);

                                        sol.getCost().writePHYWPMCWRv2ResultToFile(phyWPMCWRv2Holder);
                                    }



                                    if (sol.getRouting() == null || sol.getRouting().isEmpty()) {
                                        logger.info("No solution could be found!");
                                    } else {
                                        if (sol.getCost().getTotalCost() == Double.MAX_VALUE) {
                                            logger.info("Found No solution : {}", sol.getCost().toDetailedString());
                                        } else {
                                            logger.info("Found solution : {}", sol.getCost().toDetailedString());

                                            if(Objects.equals(wpmVersion, Constants.WPMVERSIONV1)){
                                                PHYWPMCWRv1OutputShaper oS = new PHYWPMCWRv1OutputShaper(phyWPMCWRv1Holder);

                                                oS.writeSolutionToFile(s.getSolution());

                                                oS.writeWCDsToFile(sol.getCost().getaWCDMap());

                                                oS.writeLinkUtilizationsToFile(s.getSolution(), graph, rate);

                                                oS.writeDurationMap(s.getDurationMap());

                                                oS.writeAVBCandidatesTTIntersectionToFile(s.getAVBUnicastCandidates(), s.getTTUnicasts());

                                                oS.writeAVBTTIntersectionToFile(s.getSolution());

                                                oS.writeLinkAVBTTIntersectionToFile(graph, s.getSolution());

                                                oS.writeAVBCandidateRoutesToFile(s.getAVBUnicastCandidates());
                                            }else {
                                                PHYWPMCWRv2OutputShaper oS = new PHYWPMCWRv2OutputShaper(phyWPMCWRv2Holder);

                                                oS.writeSolutionToFile(s.getSolution());

                                                oS.writeWCDsToFile(sol.getCost().getaWCDMap());

                                                oS.writeLinkUtilizationsToFile(s.getSolution(), graph, rate);

                                                oS.writeDurationMap(s.getDurationMap());

                                                oS.writeAVBCandidatesTTIntersectionToFile(s.getAVBUnicastCandidates(), s.getTTUnicasts());

                                                oS.writeAVBTTIntersectionToFile(s.getSolution());

                                                oS.writeLinkAVBTTIntersectionToFile(graph, s.getSolution());

                                                oS.writeAVBCandidateRoutesToFile(s.getAVBUnicastCandidates());
                                            }


                                        }
                                    }
                                }
                                default -> throw new InputMismatchException("Aborting: " + routing + ", " + pathFindingMethod + ", " + algorithm + " unrecognized!");
                            }
                        }
                        case "pp" -> {
                            switch (algorithm) {
                                case "WSMD" -> {
                                    s = new ktu.kaganndemirr.phy.pp.heuristic.WSMD(k);

                                    logger.info("Solving problem using {}, {}, {}, K: {}, mcdmObjective: {}, wAVB: {}, wTT: {}, wLength: {}, wUtil: {}", solver, method, algorithm, k, mcdmObjective, wAVB, wTT, wLength, wUtil);
                                    Solution sol = s.solveHWSM(graph, apps, evaluator, wsmNormalization, Double.parseDouble(wAVB), Double.parseDouble(wTT), Double.parseDouble(wLength), Double.parseDouble(wUtil), rate, mcdmObjective);

                                    PHYWSMHolder phyWSMHolder = new PHYWSMHolder();
                                    phyWSMHolder.setTopologyName(topologyName);
                                    phyWSMHolder.setApplicationName(applicationName);
                                    phyWSMHolder.setSolver(solver);
                                    phyWSMHolder.setMethod(method);
                                    phyWSMHolder.setWSMNormalization(wsmNormalization);
                                    phyWSMHolder.setAlgorithm(algorithm);
                                    phyWSMHolder.setK(k);
                                    phyWSMHolder.setWAVB(Double.parseDouble(wAVB));
                                    phyWSMHolder.setWTT(Double.parseDouble(wTT));
                                    phyWSMHolder.setWLength(Double.parseDouble(wLength));
                                    phyWSMHolder.setWUtil(Double.parseDouble(wUtil));
                                    phyWSMHolder.setMCDMObjective(mcdmObjective);

                                    sol.getCost().writePHYWSMResultToFile(phyWSMHolder);

                                    if (sol.getRouting() == null || sol.getRouting().isEmpty()) {
                                        logger.info("No solution could be found!");
                                    } else {
                                        if (sol.getCost().getTotalCost() == Double.MAX_VALUE) {
                                            logger.info("Found No solution : {}", sol.getCost().toDetailedString());
                                        } else {
                                            logger.info("Found solution : {}", sol.getCost().toDetailedString());

                                            PHYWSMOutputShaper oS = new PHYWSMOutputShaper(phyWSMHolder);

                                            oS.writeSolutionToFile(s.getSolution());

                                            oS.writeWCDsToFile(sol.getCost().getaWCDMap());

                                            oS.writeLinkUtilizationsToFile(s.getSolution(), graph, rate);

                                            oS.writeDurationMap(s.getDurationMap());

                                            oS.writeAVBCandidatesTTIntersectionToFile(s.getAVBUnicastCandidates(), s.getTTUnicasts());

                                            oS.writeAVBTTIntersectionToFile(s.getSolution());

                                            oS.writeLinkAVBTTIntersectionToFile(graph, s.getSolution());

                                            oS.writeAVBCandidateRoutesToFile(s.getAVBUnicastCandidates());
                                        }
                                    }
                                }
                                case "WPMD" -> {
                                    s = new ktu.kaganndemirr.phy.pp.heuristic.WPMD(k);

                                    if(Objects.equals(wpmVersion, Constants.WPMVERSIONV1)){
                                        logger.info("Solving problem using {}, {}, {}, K: {}, mcdmObjective: {}, wAVB: {}, wTT: {}, wLength: {}, wUtil: {}, wpmVersion: {}", solver, method, algorithm, k, mcdmObjective, wAVB, wTT, wLength, wUtil, wpmVersion);
                                    } else if (Objects.equals(wpmVersion, Constants.WPMVERSIONV2)) {
                                        logger.info("Solving problem using {}, {}, {}, K: {}, mcdmObjective: {}, wAVB: {}, wTT: {}, wLength: {}, wUtil: {}, wpmVersion: {}, wpmValueType: {}", solver, method, algorithm, k, mcdmObjective, wAVB, wTT, wLength, wUtil, wpmVersion, wpmValueType);
                                    }

                                    Solution sol = s.solveHWPM(graph, apps, evaluator, Double.parseDouble(wAVB), Double.parseDouble(wTT), Double.parseDouble(wLength), Double.parseDouble(wUtil), rate, mcdmObjective, wpmVersion, wpmValueType);

                                    PHYWPMv1Holder phyWPMv1Holder = new PHYWPMv1Holder();
                                    PHYWPMv2Holder phyWPMv2Holder = new PHYWPMv2Holder();
                                    if(Objects.equals(wpmVersion, Constants.WPMVERSIONV1)){
                                        phyWPMv1Holder.setTopologyName(topologyName);
                                        phyWPMv1Holder.setApplicationName(applicationName);
                                        phyWPMv1Holder.setSolver(solver);
                                        phyWPMv1Holder.setMethod(method);
                                        phyWPMv1Holder.setAlgorithm(algorithm);
                                        phyWPMv1Holder.setK(k);
                                        phyWPMv1Holder.setWAVB(Double.parseDouble(wAVB));
                                        phyWPMv1Holder.setWTT(Double.parseDouble(wTT));
                                        phyWPMv1Holder.setWLength(Double.parseDouble(wLength));
                                        phyWPMv1Holder.setWUtil(Double.parseDouble(wUtil));
                                        phyWPMv1Holder.setMCDMObjective(mcdmObjective);
                                        phyWPMv1Holder.setWPMVersion(wpmVersion);

                                        sol.getCost().writePHYWPMv1ResultToFile(phyWPMv1Holder);
                                    } else if (Objects.equals(wpmVersion, Constants.WPMVERSIONV2)) {
                                        phyWPMv2Holder.setTopologyName(topologyName);
                                        phyWPMv2Holder.setApplicationName(applicationName);
                                        phyWPMv2Holder.setSolver(solver);
                                        phyWPMv2Holder.setMethod(method);
                                        phyWPMv2Holder.setAlgorithm(algorithm);
                                        phyWPMv2Holder.setK(k);
                                        phyWPMv2Holder.setWAVB(Double.parseDouble(wAVB));
                                        phyWPMv2Holder.setWTT(Double.parseDouble(wTT));
                                        phyWPMv2Holder.setWLength(Double.parseDouble(wLength));
                                        phyWPMv2Holder.setWUtil(Double.parseDouble(wUtil));
                                        phyWPMv2Holder.setMCDMObjective(mcdmObjective);
                                        phyWPMv2Holder.setWPMVersion(wpmVersion);
                                        phyWPMv2Holder.setWPMValueType(wpmValueType);

                                        sol.getCost().writePHYWPMv2ResultToFile(phyWPMv2Holder);
                                    }


                                    if (sol.getRouting() == null || sol.getRouting().isEmpty()) {
                                        logger.info("No solution could be found!");
                                    } else {
                                        if (sol.getCost().getTotalCost() == Double.MAX_VALUE) {
                                            logger.info("Found No solution : {}", sol.getCost().toDetailedString());
                                        } else {
                                            logger.info("Found solution : {}", sol.getCost().toDetailedString());

                                            if(Objects.equals(wpmVersion, Constants.WPMVERSIONV1)){
                                                PHYWPMv1OutputShaper oS = new PHYWPMv1OutputShaper(phyWPMv1Holder);

                                                oS.writeSolutionToFile(s.getSolution());

                                                oS.writeWCDsToFile(sol.getCost().getaWCDMap());

                                                oS.writeLinkUtilizationsToFile(s.getSolution(), graph, rate);

                                                oS.writeDurationMap(s.getDurationMap());

                                                oS.writeAVBCandidatesTTIntersectionToFile(s.getAVBUnicastCandidates(), s.getTTUnicasts());

                                                oS.writeAVBTTIntersectionToFile(s.getSolution());

                                                oS.writeLinkAVBTTIntersectionToFile(graph, s.getSolution());

                                                oS.writeAVBCandidateRoutesToFile(s.getAVBUnicastCandidates());
                                            } else if (Objects.equals(wpmVersion, Constants.WPMVERSIONV2)) {
                                                PHYWPMv2OutputShaper oS = new PHYWPMv2OutputShaper(phyWPMv2Holder);

                                                oS.writeSolutionToFile(s.getSolution());

                                                oS.writeWCDsToFile(sol.getCost().getaWCDMap());

                                                oS.writeLinkUtilizationsToFile(s.getSolution(), graph, rate);

                                                oS.writeDurationMap(s.getDurationMap());

                                                oS.writeAVBCandidatesTTIntersectionToFile(s.getAVBUnicastCandidates(), s.getTTUnicasts());

                                                oS.writeAVBTTIntersectionToFile(s.getSolution());

                                                oS.writeLinkAVBTTIntersectionToFile(graph, s.getSolution());

                                                oS.writeAVBCandidateRoutesToFile(s.getAVBUnicastCandidates());
                                            }


                                        }
                                    }
                                }
                                case "GRASPWSMLWRD" -> {
                                    s = new ktu.kaganndemirr.phy.pp.metaheuristic.GRASPWSMLWRD(k);

                                    logger.info("Solving problem using {}, {}, {}, K: {}, Dur: {}s, MCDMObjective: {}, wAVB: {}, wTT: {}, wLength: {}, wUtil: {}, RandomizationLWR: {}", solver, method, algorithm, k, Duration.ofSeconds(duration).toSeconds(), mcdmObjective, wAVB, wTT, wLength, wUtil, randomizationLWR);
                                    Solution sol = s.solveMWSMLWR(graph, apps, evaluator, Duration.ofSeconds(duration), threadNumber, wsmNormalization, Double.parseDouble(wAVB), Double.parseDouble(wTT), Double.parseDouble(wLength), Double.parseDouble(wUtil), rate, mcdmObjective, randomizationLWR, mhtype, maxIter);

                                    PHYWSMLWRHolder phyWSMLWRHolder = new PHYWSMLWRHolder();
                                    phyWSMLWRHolder.setTopologyName(topologyName);
                                    phyWSMLWRHolder.setApplicationName(applicationName);
                                    phyWSMLWRHolder.setSolver(solver);
                                    phyWSMLWRHolder.setMethod(method);
                                    phyWSMLWRHolder.setWSMNormalization(wsmNormalization);
                                    phyWSMLWRHolder.setAlgorithm(algorithm);
                                    phyWSMLWRHolder.setK(k);
                                    phyWSMLWRHolder.setWAVB(Double.parseDouble(wAVB));
                                    phyWSMLWRHolder.setWTT(Double.parseDouble(wTT));
                                    phyWSMLWRHolder.setWLength(Double.parseDouble(wLength));
                                    phyWSMLWRHolder.setWUtil(Double.parseDouble(wUtil));
                                    phyWSMLWRHolder.setMCDMObjective(mcdmObjective);
                                    phyWSMLWRHolder.setRandomizationLWR(randomizationLWR);

                                    sol.getCost().writePHYWSMLWRResultToFile(phyWSMLWRHolder);

                                    if (sol.getRouting() == null || sol.getRouting().isEmpty()) {
                                        logger.info("No solution could be found!");
                                    } else {
                                        if (sol.getCost().getTotalCost() == Double.MAX_VALUE) {
                                            logger.info("Found No solution : {}", sol.getCost().toDetailedString());
                                        } else {
                                            logger.info("Found solution : {}", sol.getCost().toDetailedString());

                                            PHYWSMLWROutputShaper oS = new PHYWSMLWROutputShaper(phyWSMLWRHolder);

                                            oS.writeSolutionToFile(s.getSolution());

                                            oS.writeWCDsToFile(sol.getCost().getaWCDMap());

                                            oS.writeLinkUtilizationsToFile(s.getSolution(), graph, rate);

                                            oS.writeDurationMap(s.getDurationMap());

                                            oS.writeAVBCandidatesTTIntersectionToFile(s.getAVBUnicastCandidates(), s.getTTUnicasts());

                                            oS.writeAVBTTIntersectionToFile(s.getSolution());

                                            oS.writeLinkAVBTTIntersectionToFile(graph, s.getSolution());

                                            oS.writeAVBCandidateRoutesToFile(s.getAVBUnicastCandidates());
                                        }
                                    }
                                }
                                case "GRASPWPMLWRD" -> {
                                    s = new ktu.kaganndemirr.phy.pp.metaheuristic.GRASPWPMLWRD(k);

                                    if(Objects.equals(wpmVersion, Constants.WPMVERSIONV1)){
                                        logger.info("Solving problem using {}, {}, {}, K: {}, Dur: {}s, MCDMObjective: {}, wAVB: {}, wTT: {}, wLength: {}, wUtil: {}, RandomizationLWR: {}, wpmVersion: {}", solver, method, algorithm, k, Duration.ofSeconds(duration).toSeconds(), mcdmObjective, wAVB, wTT, wLength, wUtil, randomizationLWR, wpmVersion);
                                    }else {
                                        logger.info("Solving problem using {}, {}, {}, K: {}, Dur: {}s, MCDMObjective: {}, wAVB: {}, wTT: {}, wLength: {}, wUtil: {}, RandomizationLWR: {}, wpmVersion: {}, wpmValueType: {}", solver, method, algorithm, k, Duration.ofSeconds(duration).toSeconds(), mcdmObjective, wAVB, wTT, wLength, wUtil, randomizationLWR, wpmVersion, wpmValueType);
                                    }

                                    Solution sol = s.solveMWPMLWR(graph, apps, evaluator, Duration.ofSeconds(duration), threadNumber, Double.parseDouble(wAVB), Double.parseDouble(wTT), Double.parseDouble(wLength), Double.parseDouble(wUtil), rate, mcdmObjective, randomizationLWR, mhtype, maxIter, wpmVersion, wpmValueType);

                                    PHYWPMLWRv1Holder phyWPMLWRv1Holder = new PHYWPMLWRv1Holder();
                                    PHYWPMLWRv2Holder phyWPMLWRv2Holder = new PHYWPMLWRv2Holder();

                                    if(Objects.equals(wpmVersion, Constants.WPMVERSIONV1)){
                                        phyWPMLWRv1Holder.setTopologyName(topologyName);
                                        phyWPMLWRv1Holder.setApplicationName(applicationName);
                                        phyWPMLWRv1Holder.setSolver(solver);
                                        phyWPMLWRv1Holder.setMethod(method);
                                        phyWPMLWRv1Holder.setAlgorithm(algorithm);
                                        phyWPMLWRv1Holder.setK(k);
                                        phyWPMLWRv1Holder.setWAVB(Double.parseDouble(wAVB));
                                        phyWPMLWRv1Holder.setWTT(Double.parseDouble(wTT));
                                        phyWPMLWRv1Holder.setWLength(Double.parseDouble(wLength));
                                        phyWPMLWRv1Holder.setWUtil(Double.parseDouble(wUtil));
                                        phyWPMLWRv1Holder.setMCDMObjective(mcdmObjective);
                                        phyWPMLWRv1Holder.setRandomizationLWR(randomizationLWR);
                                        phyWPMLWRv1Holder.setWPMVersion(wpmVersion);

                                        sol.getCost().writePHYWPMLWRv1ResultToFile(phyWPMLWRv1Holder);

                                    }else {
                                        phyWPMLWRv2Holder.setTopologyName(topologyName);
                                        phyWPMLWRv2Holder.setApplicationName(applicationName);
                                        phyWPMLWRv2Holder.setSolver(solver);
                                        phyWPMLWRv2Holder.setMethod(method);
                                        phyWPMLWRv2Holder.setAlgorithm(algorithm);
                                        phyWPMLWRv2Holder.setK(k);
                                        phyWPMLWRv2Holder.setWAVB(Double.parseDouble(wAVB));
                                        phyWPMLWRv2Holder.setWTT(Double.parseDouble(wTT));
                                        phyWPMLWRv2Holder.setWLength(Double.parseDouble(wLength));
                                        phyWPMLWRv2Holder.setWUtil(Double.parseDouble(wUtil));
                                        phyWPMLWRv2Holder.setMCDMObjective(mcdmObjective);
                                        phyWPMLWRv2Holder.setRandomizationLWR(randomizationLWR);
                                        phyWPMLWRv2Holder.setWPMVersion(wpmVersion);
                                        phyWPMLWRv2Holder.setWPMValueType(wpmValueType);

                                        sol.getCost().writePHYWPMLWRv2ResultToFile(phyWPMLWRv2Holder);
                                    }

                                    if (sol.getRouting() == null || sol.getRouting().isEmpty()) {
                                        logger.info("No solution could be found!");
                                    } else {
                                        if (sol.getCost().getTotalCost() == Double.MAX_VALUE) {
                                            logger.info("Found No solution : {}", sol.getCost().toDetailedString());
                                        } else {
                                            logger.info("Found solution : {}", sol.getCost().toDetailedString());

                                            if(Objects.equals(wpmVersion, Constants.WPMVERSIONV1)){
                                                PHYWPMLWRv1OutputShaper oS = new PHYWPMLWRv1OutputShaper(phyWPMLWRv1Holder);

                                                oS.writeSolutionToFile(s.getSolution());

                                                oS.writeWCDsToFile(sol.getCost().getaWCDMap());

                                                oS.writeLinkUtilizationsToFile(s.getSolution(), graph, rate);

                                                oS.writeDurationMap(s.getDurationMap());

                                                oS.writeAVBCandidatesTTIntersectionToFile(s.getAVBUnicastCandidates(), s.getTTUnicasts());

                                                oS.writeAVBTTIntersectionToFile(s.getSolution());

                                                oS.writeLinkAVBTTIntersectionToFile(graph, s.getSolution());

                                                oS.writeAVBCandidateRoutesToFile(s.getAVBUnicastCandidates());
                                            }
                                            else {
                                                PHYWPMLWRv2OutputShaper oS = new PHYWPMLWRv2OutputShaper(phyWPMLWRv2Holder);

                                                oS.writeSolutionToFile(s.getSolution());

                                                oS.writeWCDsToFile(sol.getCost().getaWCDMap());

                                                oS.writeLinkUtilizationsToFile(s.getSolution(), graph, rate);

                                                oS.writeDurationMap(s.getDurationMap());

                                                oS.writeAVBCandidatesTTIntersectionToFile(s.getAVBUnicastCandidates(), s.getTTUnicasts());

                                                oS.writeAVBTTIntersectionToFile(s.getSolution());

                                                oS.writeLinkAVBTTIntersectionToFile(graph, s.getSolution());

                                                oS.writeAVBCandidateRoutesToFile(s.getAVBUnicastCandidates());
                                            }


                                        }
                                    }
                                }
                                case "GRASPWSMCWRD" -> {
                                    s = new ktu.kaganndemirr.phy.pp.metaheuristic.GRASPWSMCWRD(k);

                                    logger.info("Solving problem using {}, {}, {}, K: {}, Dur: {}s, MCDMObjective: {}, RandomizationCWR: {}", solver, method, algorithm, k, Duration.ofSeconds(duration).toSeconds(), mcdmObjective, randomizationCWR);
                                    Solution sol = s.solveMWSMCWR(graph, apps, evaluator, Duration.ofSeconds(duration), threadNumber, wsmNormalization, mcdmObjective, randomizationCWR, rate, mhtype, maxIter);

                                    PHYWSMCWRHolder phyWSMCWRHolder = new PHYWSMCWRHolder();

                                    phyWSMCWRHolder.setTopologyName(topologyName);
                                    phyWSMCWRHolder.setApplicationName(applicationName);
                                    phyWSMCWRHolder.setSolver(solver);
                                    phyWSMCWRHolder.setMethod(method);
                                    phyWSMCWRHolder.setAlgorithm(algorithm);
                                    phyWSMCWRHolder.setK(k);
                                    phyWSMCWRHolder.setMCDMObjective(mcdmObjective);
                                    phyWSMCWRHolder.setRandomizationCWR(randomizationCWR);
                                    phyWSMCWRHolder.setWSMNormalization(wsmNormalization);

                                    sol.getCost().writePHYWSMCWRResultToFile(phyWSMCWRHolder);

                                    if (sol.getRouting() == null || sol.getRouting().isEmpty()) {
                                        logger.info("No solution could be found!");
                                    } else {
                                        if (sol.getCost().getTotalCost() == Double.MAX_VALUE) {
                                            logger.info("Found No solution : {}", sol.getCost().toDetailedString());
                                        } else {
                                            logger.info("Found solution : {}", sol.getCost().toDetailedString());

                                            PHYWSMCWROutputShaper oS = new PHYWSMCWROutputShaper(phyWSMCWRHolder);

                                            oS.writeSolutionToFile(s.getSolution());

                                            oS.writeWCDsToFile(sol.getCost().getaWCDMap());

                                            oS.writeLinkUtilizationsToFile(s.getSolution(), graph, rate);

                                            oS.writeDurationMap(s.getDurationMap());

                                            oS.writeAVBCandidatesTTIntersectionToFile(s.getAVBUnicastCandidates(), s.getTTUnicasts());

                                            oS.writeAVBTTIntersectionToFile(s.getSolution());

                                            oS.writeLinkAVBTTIntersectionToFile(graph, s.getSolution());

                                            oS.writeAVBCandidateRoutesToFile(s.getAVBUnicastCandidates());
                                        }
                                    }
                                }
                                case "GRASPWPMCWRD" -> {
                                    s = new ktu.kaganndemirr.phy.pp.metaheuristic.GRASPWPMCWRD(k);

                                    if(Objects.equals(wpmVersion, Constants.WPMVERSIONV1)){
                                        logger.info("Solving problem using {}, {}, {}, K: {}, Dur: {}s, MCDMObjective: {}, RandomizationCWR: {}, wpmVersion: {}", solver, method, algorithm, k, Duration.ofSeconds(duration).toSeconds(), mcdmObjective, randomizationCWR, wpmVersion);
                                    }else {
                                        logger.info("Solving problem using {}, {}, {}, K: {}, Dur: {}s, MCDMObjective: {}, RandomizationCWR: {}, wpmVersion: {}, wpmValueType: {}", solver, method, algorithm, k, Duration.ofSeconds(duration).toSeconds(), mcdmObjective, randomizationCWR, wpmVersion, wpmValueType);
                                    }

                                    Solution sol = s.solveMWPMCWR(graph, apps, evaluator, Duration.ofSeconds(duration), threadNumber, mcdmObjective, randomizationCWR, rate, mhtype, maxIter, wpmVersion, wpmValueType);

                                    PHYWPMCWRv1Holder phyWPMCWRv1Holder = new PHYWPMCWRv1Holder();
                                    PHYWPMCWRv2Holder phyWPMCWRv2Holder = new PHYWPMCWRv2Holder();

                                    if(Objects.equals(wpmVersion, Constants.WPMVERSIONV1)){

                                        phyWPMCWRv1Holder.setTopologyName(topologyName);
                                        phyWPMCWRv1Holder.setApplicationName(applicationName);
                                        phyWPMCWRv1Holder.setSolver(solver);
                                        phyWPMCWRv1Holder.setMethod(method);
                                        phyWPMCWRv1Holder.setAlgorithm(algorithm);
                                        phyWPMCWRv1Holder.setK(k);
                                        phyWPMCWRv1Holder.setMCDMObjective(mcdmObjective);
                                        phyWPMCWRv1Holder.setRandomizationCWR(randomizationCWR);
                                        phyWPMCWRv1Holder.setWPMVersion(wpmVersion);

                                        sol.getCost().writePHYWPMCWRv1ResultToFile(phyWPMCWRv1Holder);
                                    }else {
                                        phyWPMCWRv2Holder.setTopologyName(topologyName);
                                        phyWPMCWRv2Holder.setApplicationName(applicationName);
                                        phyWPMCWRv2Holder.setSolver(solver);
                                        phyWPMCWRv2Holder.setMethod(method);
                                        phyWPMCWRv2Holder.setAlgorithm(algorithm);
                                        phyWPMCWRv2Holder.setK(k);
                                        phyWPMCWRv2Holder.setMCDMObjective(mcdmObjective);
                                        phyWPMCWRv2Holder.setRandomizationCWR(randomizationCWR);
                                        phyWPMCWRv2Holder.setWPMVersion(wpmVersion);
                                        phyWPMCWRv2Holder.setWPMValueType(wpmValueType);

                                        sol.getCost().writePHYWPMCWRv2ResultToFile(phyWPMCWRv2Holder);
                                    }



                                    if (sol.getRouting() == null || sol.getRouting().isEmpty()) {
                                        logger.info("No solution could be found!");
                                    } else {
                                        if (sol.getCost().getTotalCost() == Double.MAX_VALUE) {
                                            logger.info("Found No solution : {}", sol.getCost().toDetailedString());
                                        } else {
                                            logger.info("Found solution : {}", sol.getCost().toDetailedString());

                                            if(Objects.equals(wpmVersion, Constants.WPMVERSIONV1)){
                                                PHYWPMCWRv1OutputShaper oS = new PHYWPMCWRv1OutputShaper(phyWPMCWRv1Holder);

                                                oS.writeSolutionToFile(s.getSolution());

                                                oS.writeWCDsToFile(sol.getCost().getaWCDMap());

                                                oS.writeLinkUtilizationsToFile(s.getSolution(), graph, rate);

                                                oS.writeDurationMap(s.getDurationMap());

                                                oS.writeAVBCandidatesTTIntersectionToFile(s.getAVBUnicastCandidates(), s.getTTUnicasts());

                                                oS.writeAVBTTIntersectionToFile(s.getSolution());

                                                oS.writeLinkAVBTTIntersectionToFile(graph, s.getSolution());

                                                oS.writeAVBCandidateRoutesToFile(s.getAVBUnicastCandidates());
                                            }else {
                                                PHYWPMCWRv2OutputShaper oS = new PHYWPMCWRv2OutputShaper(phyWPMCWRv2Holder);

                                                oS.writeSolutionToFile(s.getSolution());

                                                oS.writeWCDsToFile(sol.getCost().getaWCDMap());

                                                oS.writeLinkUtilizationsToFile(s.getSolution(), graph, rate);

                                                oS.writeDurationMap(s.getDurationMap());

                                                oS.writeAVBCandidatesTTIntersectionToFile(s.getAVBUnicastCandidates(), s.getTTUnicasts());

                                                oS.writeAVBTTIntersectionToFile(s.getSolution());

                                                oS.writeLinkAVBTTIntersectionToFile(graph, s.getSolution());

                                                oS.writeAVBCandidateRoutesToFile(s.getAVBUnicastCandidates());
                                            }


                                        }
                                    }
                                }
                                case "GRASPWSMLWRCWRD" -> {
                                    s = new ktu.kaganndemirr.phy.pp.metaheuristic.GRASPWSMLWRCWRD(k);

                                    logger.info("Solving problem using {}, {}, {}, K: {}, Dur: {}s, mcdmObjective: {}, RandomizationLWR: {}, RandomizationCWR: {}", solver, method, algorithm, k, Duration.ofSeconds(duration).toSeconds(), mcdmObjective, randomizationLWR, randomizationCWR);
                                    Solution sol = s.solveMWSMLWRCWR(graph, apps, evaluator, Duration.ofSeconds(duration), threadNumber, wsmNormalization, mcdmObjective, randomizationLWR, rate, mhtype, maxIter, randomizationCWR);

                                    PHYWSMLWRCWRHolder phyWSMLWRCWRHolder = new PHYWSMLWRCWRHolder();
                                    phyWSMLWRCWRHolder.setTopologyName(topologyName);
                                    phyWSMLWRCWRHolder.setApplicationName(applicationName);
                                    phyWSMLWRCWRHolder.setSolver(solver);
                                    phyWSMLWRCWRHolder.setMethod(method);
                                    phyWSMLWRCWRHolder.setWSMNormalization(wsmNormalization);
                                    phyWSMLWRCWRHolder.setAlgorithm(algorithm);
                                    phyWSMLWRCWRHolder.setK(k);
                                    phyWSMLWRCWRHolder.setMCDMObjective(mcdmObjective);
                                    phyWSMLWRCWRHolder.setRandomizationLWR(randomizationLWR);
                                    phyWSMLWRCWRHolder.setRandomizationCWR(randomizationCWR);

                                    sol.getCost().writePHYWSMLWRCWRResultToFile(phyWSMLWRCWRHolder);

                                    if (sol.getRouting() == null || sol.getRouting().isEmpty()) {
                                        logger.info("No solution could be found!");
                                    } else {
                                        if (sol.getCost().getTotalCost() == Double.MAX_VALUE) {
                                            logger.info("Found No solution : {}", sol.getCost().toDetailedString());
                                        } else {
                                            logger.info("Found solution : {}", sol.getCost().toDetailedString());

                                            PHYWSMLWRCWROutputShaper oS = new PHYWSMLWRCWROutputShaper(phyWSMLWRCWRHolder);

                                            oS.writeSolutionToFile(s.getSolution());

                                            oS.writeWCDsToFile(sol.getCost().getaWCDMap());

                                            oS.writeLinkUtilizationsToFile(s.getSolution(), graph, rate);

                                            oS.writeDurationMap(s.getDurationMap());

                                            oS.writeAVBCandidatesTTIntersectionToFile(s.getAVBUnicastCandidates(), s.getTTUnicasts());

                                            oS.writeAVBTTIntersectionToFile(s.getSolution());

                                            oS.writeLinkAVBTTIntersectionToFile(graph, s.getSolution());

                                            oS.writeAVBCandidateRoutesToFile(s.getAVBUnicastCandidates());
                                        }
                                    }
                                }
                                default -> throw new InputMismatchException("Aborting: " + solver + ", " + method + ", " + algorithm + " unrecognized!");
                            }
                        }
                        default -> throw new InputMismatchException("Aborting: Solver " + routing + ", " + pathFindingMethod + " unrecognized!");
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