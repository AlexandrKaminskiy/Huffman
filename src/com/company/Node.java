package com.company;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class Node {
    Node left;
    Node right;
    private int priority;
    Byte symbol;

    private static final List<Byte> ROOT = List.of(
        Byte.MIN_VALUE,
        (byte) (Byte.MIN_VALUE + 1),
        (byte) (Byte.MIN_VALUE + 2)
    );

    public Node(ByteBuffer buffer, int prefixStringSize) {
        Stack<Node> prefixRecord = new Stack<>();

        for (int i = 0; i < prefixStringSize; i++) {
            int position = buffer.position();
            if (buffer.get(position) == ROOT.getFirst() && checkForRoot(buffer, position)) {
                buffer.position(buffer.position() + ROOT.size());
                i += (ROOT.size() - 1);
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
                prefixRecord.push(new Node(0, buffer.get()));
            }
        }

        Node root = prefixRecord.pop();
        left = root.left;
        right = root.right;
        symbol = root.symbol;
    }

    private boolean checkForRoot(ByteBuffer buffer, int position) {
        ByteBuffer fromPosition = buffer.slice(position, buffer.capacity() - position);
        if (fromPosition.capacity() < ROOT.size()) {
            return false;
        }

        return fromPosition.get() == ROOT.getFirst()
            && fromPosition.get() == ROOT.get(1)
            && fromPosition.get() == ROOT.get(2);
    }

    public Node(Node left, Node right) {
        this.left = left;
        this.right = right;
    }

    public Node(int priority, Byte symbol) {
        this.priority = priority;
        this.symbol = symbol;
    }

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
            System.out.println(symbol + "    " + code);
        }
    }

    public Map<Byte, String> getSymbolMap(String code, Map<Byte, String> map) {
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

    public List<Byte> getPrefixString() {
        List<Byte> result = getPrefix();
        if (result.size() == 1) {
            result.addAll(ROOT);
        }
        return result;
    }

    public int getRootCount(int count) {

        if (left != null) {
            getRootCount(count);
        }

        if (right != null) {
            getRootCount(count);
        }

        if (right != null || left != null) {
            return ++count;
        }
        return count;
    }

    private List<Byte> getPrefix() {
        List<Byte> result = new ArrayList<>();
        if (left != null) {
            result.addAll(left.getPrefix());
        }

        if (right != null) {
            result.addAll(right.getPrefix());
        }

        if (right == null && left == null) {
            result.add(symbol);
        } else {
            result.addAll(ROOT);
        }
        return result;
    }
}
