package ktu.kaganndemirr.architecture;

public class EndSystem extends Node {

    public EndSystem(String id) {
        this.id = id;
    }

    public EndSystem(Node endSystem) {
        this.id = endSystem.id;
    }
}
