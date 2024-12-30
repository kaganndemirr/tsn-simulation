package ktu.kaganndemirr.routing.phy.yen;

import ktu.kaganndemirr.architecture.GCLEdge;
import ktu.kaganndemirr.architecture.Node;
import org.jgrapht.GraphPath;

import java.util.List;

public class HelperMethods {
    public static List<GraphPath<Node, GCLEdge>> fillYenKShortestPathGraphPathList(List<GraphPath<Node, GCLEdge>> yenKShortestPathList, int k){
        if (yenKShortestPathList.size() != k) {
            int appPathsSize = yenKShortestPathList.size();
            for (int j = 0; j < k - appPathsSize; j++) {
                yenKShortestPathList.add(yenKShortestPathList.getLast());
            }
        }
        return yenKShortestPathList;
    }
}
