package com.company;

public class QueueElement {
    int priority;
    Node data;

    public QueueElement(Node data) {
        this.priority = data.getPriority();
        this.data = data;
    }
}
