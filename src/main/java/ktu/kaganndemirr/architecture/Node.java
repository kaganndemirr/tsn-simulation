package ktu.kaganndemirr.architecture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class Node {
    private static final Logger logger = LoggerFactory.getLogger(Node.class.getSimpleName());

    protected String id;

    @Override
    public boolean equals(Object object) {
        boolean result;
        if (object == null || getClass() != object.getClass()) {
            result = false;
        } else {
            Node otherNode = (Node) object;
            result = id.equals(otherNode.id);
        }
        return result;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return id;
    }
}
