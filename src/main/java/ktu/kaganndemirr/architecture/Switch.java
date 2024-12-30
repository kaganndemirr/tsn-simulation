package ktu.kaganndemirr.architecture;

public class Switch extends Node {

    public Switch(String id) {
        this.id = id;
    }

    public Switch(Node node) {
        this.id = node.id;
    }
}
