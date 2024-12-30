package ktu.kaganndemirr.message;

import ktu.kaganndemirr.application.Application;
import ktu.kaganndemirr.architecture.Node;

public class Route {

    protected Application application;

    protected Node target;

    public Application getApplication() {
        return application;
    }

    public Node getTarget() {
        return target;
    }

    @Override
    public String toString() {
        return application.toString() + "->" + target;
    }

    @Override
    public boolean equals(Object object) {

        if (object instanceof Route route) {
            return application.equals(route.application) && target.equals(route.target);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return application.hashCode() + target.hashCode();
    }

}
