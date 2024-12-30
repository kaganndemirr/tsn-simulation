package ktu.kaganndemirr.evaluator;

import ktu.kaganndemirr.message.Unicast;

import java.util.List;

public interface Evaluator {

    Cost evaluate(List<Unicast> unicastList);
}
