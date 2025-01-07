package ktu.kaganndemirr.util;

import ktu.kaganndemirr.application.SRTApplication;
import ktu.kaganndemirr.application.TTApplication;
import ktu.kaganndemirr.message.Unicast;
import ktu.kaganndemirr.message.UnicastCandidate;

import java.io.BufferedWriter;
import java.nio.file.Paths;
import java.util.List;

import static ktu.kaganndemirr.util.Constants.findAveragePathLengthIncludingES;
import static ktu.kaganndemirr.util.Constants.findAveragePathLengthWithoutES;

public class HelperMethods {
    public static void writeSolutionsToFile(List<Unicast> initialSolution, List<Unicast> solution){
        BufferedWriter initialSolutionWriter = new BufferedWriter(new java.io.FileWriter(Paths.get(topologyOutputLocation, "Routes.txt").toString()));

        for (Unicast unicast : solution) {
            if (unicast.getApplication() instanceof SRTApplication) {
                writer.write(unicast.getApplication().getName() + ": ");
                writer.write(unicast.getPath().getEdgeList() + ", ");
                writer.write("Length(weight non-aware): " + unicast.getPath().getEdgeList().size());
                writer.newLine();
            }
        }

        writer.write("Average Length (ESs included): " + findAveragePathLengthIncludingES(solution) + ", ");
        writer.write("Average Length (between switches): " + findAveragePathLengthWithoutES(solution));

        writer.write("\n");
        writer.write("\n");

        for (Unicast unicast : solution) {
            if (unicast.getApplication() instanceof TTApplication) {
                writer.write(unicast.getApplication().getName() + ": ");
                writer.write(String.valueOf(unicast.getPath().getEdgeList()));
                writer.newLine();
            }
        }
        writer.close();
    }
}
