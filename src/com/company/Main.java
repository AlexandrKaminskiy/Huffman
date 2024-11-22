package com.company;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        String inputString = "beep boop beer!";

//        inputString += '\26';
        System.out.println(inputString);
        List<Character> characters = new ArrayList<>();
        List<Integer> count = new ArrayList<>();
        for (int i = 0; i < inputString.length(); i++) {
            int index = characters.indexOf(inputString.charAt(i));
            if (index != -1) {
                count.set(index, count.get(index) + 1);
            } else {
                characters.add(inputString.charAt(i));
                count.add(1);
            }
        }
        //put in pq
        PriorityQueue pq = new PriorityQueue();
        for (int i = 0; i < count.size(); i++) {
            pq.push(new QueueElement(new Node(count.get(i), characters.get(i))));
        }

        //huffman tree
        while (pq.size() != 1) {
            QueueElement el0 = pq.pop();
            QueueElement el1 = pq.pop();
            pq.push(new QueueElement(new Node(el0.data, el1.data)));
        }
        Node rootNode = pq.pop().data;

        rootNode.abr();

        String prefixString = rootNode.getPrefixString();
        System.out.println(prefixString);

        Map<Character, String> map = new HashMap<>();
        rootNode.getSymbolMap("", map);

        String outputString = "";

        if (map.size() == 1) {
            map.put(rootNode.symbol, "0");
        }

        //compressing
        for (int i = 0; i < inputString.length(); i++) {
            outputString += map.get(inputString.charAt(i));
        }
        System.out.println(outputString);

        //decompressing
        Node currentNode = rootNode;

        String decompressed = "";

        for (int i = 0; i < outputString.length(); i++) {
            if ((currentNode.right == null && currentNode.left == null) ||
                (rootNode.right == null && rootNode.left == null)) {
                decompressed += currentNode.symbol;
                currentNode = rootNode;
            }
            if (currentNode.right != null && currentNode.left != null) {
                if (outputString.charAt(i) == '1') {
                    currentNode = currentNode.right;
                } else {
                    currentNode = currentNode.left;
                }
            }
        }
        if (map.size() > 1) {
            decompressed += currentNode.symbol;
        }

        System.out.println(decompressed);

        //create tree from rpr
        Node node = new Node(prefixString);
        node.abr();
        System.out.println();
    }
}

