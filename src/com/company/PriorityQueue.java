package com.company;

import java.util.ArrayList;
import java.util.List;

public class PriorityQueue {

    List<QueueElement> queue = new ArrayList<>();

    public void push(QueueElement element) {
        if (queue.isEmpty()) {
            queue.add(element);
            return;
        }
        boolean inserted = false;
        for (int i = 0; i < queue.size(); i++) {
            if (queue.get(i).priority == element.priority || queue.get(i).priority > element.priority) {
                queue.add(i, element);
                inserted = true;
                break;
            }
        }
        if (!inserted) {
            queue.add(element);
        }
    }

    public QueueElement pop() {
        if (queue.isEmpty()) {
            return null;
        }
        return queue.remove(0);
    }

    public QueueElement poll() {
        if (queue.isEmpty()) {
            return null;
        }
        return queue.get(0);
    }

    public int size() {
        return queue.size();
    }
}
