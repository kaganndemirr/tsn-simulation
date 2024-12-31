package ktu.kaganndemirr.evaluator;

import ktu.kaganndemirr.message.Unicast;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class AVBLatencyMathTSNRO implements Evaluator{
    private static final Logger logger = LoggerFactory.getLogger(AVBLatencyMathTSNRO.class.getSimpleName());

    @Override
    public Cost evaluate(List<Unicast> unicastList) {
        return null;
    }
}
