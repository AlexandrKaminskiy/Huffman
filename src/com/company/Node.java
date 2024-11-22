package com.company;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class Node implements Priorityable {
    Node left;
    Node right;
    private int priority;
    char symbol;

    public Node(String prefixString) {
        Stack<Node> prefixRecord = new Stack<>();
        for (int i = 0; i < prefixString.length(); i++) {
            if (prefixString.charAt(i) == '*') {
                Node root = new Node(null, null);
                if (prefixRecord.size() == 1) {
                    break;
                }
                if (prefixRecord.size() > 1) {
                    Node leftNode = prefixRecord.pop();
                    Node rightNode = prefixRecord.pop();
                    root.right = leftNode;
                    root.left = rightNode;
                    prefixRecord.push(root);
                }


            } else {
                prefixRecord.push(new Node(0, prefixString.charAt(i)));
            }
        }
        Node root = prefixRecord.pop();
        left = root.left;
        right = root.right;
        symbol = root.symbol;
    }
    public Node(Node left, Node right) {
        this.left = left;
        this.right = right;
    }

    public Node(int priority, char symbol) {
        this.priority = priority;
        this.symbol = symbol;
    }

    @Override
    public int getPriority() {
        if (priority != 0) {
            return priority;
        }
        if (left != null) {
            priority += left.getPriority();
        }
        if (right != null) {
            priority += right.getPriority();
        }
        this.priority = priority;
        return priority;
    }

    public void abr() {
        abr("");
    }

    public void abr(String code) {
        if (left != null) {
            left.abr(code + '0');
        }

        if (right != null) {
            right.abr(code + '1');
        }

        if (right == null && left == null) {
            System.out.println(Character.valueOf(symbol).toString() + ' ' + code);
        }
    }

    public Map<Character, String> getSymbolMap(String code, Map<Character, String> map) {
        if (left != null) {
            map = left.getSymbolMap(code + '0', map);
        }

        if (right != null) {
            map = right.getSymbolMap(code + '1', map);
        }

        if (right == null && left == null) {
            map.put(symbol, code);
        }
        return map;
    }

    public String getPrefixString() {
        String result = getPrefix();
        if (result.length() == 1) {
            result += "*";
        }
        return result;
    }

    private String getPrefix() {
        String result = "";
        if (left != null) {
            result += left.getPrefix();
        }

        if (right != null) {
            result += right.getPrefix();
        }

        if (right == null && left == null) {
            result += Character.toString(symbol);
        } else {
            result += '*';
        }
        return result;
    }
}
