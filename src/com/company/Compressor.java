package com.company;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Compressor {

    private static final byte[] ROOT = { Byte.MAX_VALUE, Byte.MAX_VALUE, Byte.MAX_VALUE };

    public byte[] compress(String path) throws IOException {

        byte[] bytes = Files.readAllBytes(Path.of(path));

        List<Byte> containedBytes = new ArrayList<>();
        List<Integer> containedBytesCount = new ArrayList<>();

        for (byte currentByte : bytes) {
            int index = containedBytes.indexOf(currentByte);
            if (index != -1) {
                containedBytesCount.set(index, containedBytesCount.get(index) + 1);
            } else {
                containedBytes.add(currentByte);
                containedBytesCount.add(1);
            }
        }

        PriorityQueue pq = new PriorityQueue();
        for (int i = 0; i < containedBytesCount.size(); i++) {
            pq.push(new QueueElement(new Node(containedBytesCount.get(i), containedBytes.get(i))));
        }

    }
}
