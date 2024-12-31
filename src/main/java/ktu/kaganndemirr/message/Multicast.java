package ktu.kaganndemirr.message;

import ktu.kaganndemirr.application.Application;

import java.util.*;

public class Multicast {
    private final Application application;

    private final List<Unicast> unicastList;

    public Multicast(Application application, List<Unicast> unicastList) {
        this.application = application;
        this.unicastList = unicastList;
    }

    public List<Unicast> getUnicastList() {
        return unicastList;
    }

    public Application getApplication() {
        return application;
    }


    @Override
    public boolean equals(Object object) {
        if (object instanceof Multicast multicast) {
            return multicast.getApplication().equals(getApplication()) &&
                    multicast.getUnicastList().equals(getUnicastList());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return application.hashCode() + unicastList.hashCode();
    }

    @Override
    public String toString() {
        return application.toString();
    }

    public static List<Multicast> generateMulticastList(List<Unicast> unicastList) {
        Map<Application, ArrayList<Unicast>> multicastMap = new HashMap<>();
        for (Unicast unicast : unicastList) {
            if (!multicastMap.containsKey(unicast.getApplication())) {
                multicastMap.put(unicast.getApplication(), new ArrayList<>());
            }
            multicastMap.get(unicast.getApplication()).add(unicast);
        }

        List<Multicast> multicastList = new ArrayList<>();
        for (Application application : multicastMap.keySet()) {
            multicastList.add(new Multicast(application, multicastMap.get(application)));
        }
        return multicastList;
    }
}
