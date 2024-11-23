package com.company;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Compressor {

    private static final byte ZERO_BYTE = 0;
    private static final int BYTE_SIZE_IN_BITS = 8;

    /**
     * Offset value size for last byte
     */
    private static final int OFFSET_SIZE_IN_BYTES = 1;
    private static final int PREFIX_STRING_SIZE_IN_BYTES = 4;

    public void compress(String path) throws IOException {

        byte[] bytes = Files.readAllBytes(Path.of(path));
        if (bytes.length == 0) {
            System.err.println("Error! File is empty!");
            return;
        }

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

        while (pq.size() != 1) {
            QueueElement el0 = pq.pop();
            QueueElement el1 = pq.pop();
            pq.push(new QueueElement(new Node(el0.data, el1.data)));
        }
        Node rootNode = pq.pop().data;
//        rootNode.abr();
        List<Byte> prefixString = rootNode.getPrefixString();

        Map<Byte, String> map = new HashMap<>();
        rootNode.getSymbolMap("", map);

        if (map.size() == 1) {
            map.put(rootNode.symbol, String.valueOf(ZERO_BYTE));
        }

        StringBuilder outputStringBuilder = new StringBuilder();

        for (byte aByte : bytes) {
            outputStringBuilder.append(map.get(aByte));
        }
        String outputString = outputStringBuilder.toString();

        ByteBuffer outputBuffer = ByteBuffer.allocate(PREFIX_STRING_SIZE_IN_BYTES + OFFSET_SIZE_IN_BYTES + prefixString.size() + outputString.length() / BYTE_SIZE_IN_BITS + 1);
//        System.out.println("Output string size: " + outputString.length());
//        System.out.println("Prefix string size: " + prefixString.size());
        outputBuffer.putInt(prefixString.size());
        outputBuffer.put((byte) ((BYTE_SIZE_IN_BITS - outputString.length() % BYTE_SIZE_IN_BITS) % BYTE_SIZE_IN_BITS));

        for (Byte aByte : prefixString) {
            outputBuffer.put(aByte);
        }

        for (int i = 0; i < outputString.length(); i += BYTE_SIZE_IN_BITS) {
            String currentString = outputString.length() - i - 1 >= 8
                ? outputString.substring(i, i + BYTE_SIZE_IN_BITS)
                : outputString.substring(i);
            if (!currentString.isEmpty()) {
                byte current = (byte) (int) Integer.valueOf(currentString, 2);
                outputBuffer.put(current);
            }
        }
        outputBuffer.flip();

        Path filePath = Path.of("compressed_" + path);

        FileChannel fileChannel = FileChannel.open(filePath,
            StandardOpenOption.CREATE,  // Create the file if it doesn't exist
            StandardOpenOption.WRITE
        );
        fileChannel.write(outputBuffer);
        fileChannel.close();
    }

    public void decompress(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Path.of(path));
        ByteBuffer inputBuffer = ByteBuffer.wrap(bytes);
        int prefixStringSize = inputBuffer.getInt();
        byte offset = inputBuffer.get();
        Node rootNode = new Node(inputBuffer, prefixStringSize);
        Node currentNode = rootNode;
        List<Byte> output = new ArrayList<>();
        while (inputBuffer.hasRemaining()) {
            byte b = inputBuffer.get();
            int lastByteOffset = inputBuffer.hasRemaining() ? 0 : offset;
            int mask = (0b10000000 >>> lastByteOffset);
            for (int i = lastByteOffset; i < BYTE_SIZE_IN_BITS; i++) {
                byte current = (byte) ((b & mask) >>> (BYTE_SIZE_IN_BITS - i - 1));
                assert current == 1 || current == 0;

                if ((currentNode.right == null && currentNode.left == null) ||
                    (rootNode.right == null && rootNode.left == null)) {
                    output.add(currentNode.symbol);
                    currentNode = rootNode;
                }
                if (currentNode.right != null && currentNode.left != null) {
                    if (current == 1) {
                        currentNode = currentNode.right;
                    } else {
                        currentNode = currentNode.left;
                    }
                }
                mask >>>= 1;
            }
        }
        if (rootNode.left != null && rootNode.right != null) {
            output.add(currentNode.symbol);
        }


        byte[] byteArray = new byte[output.size()];
        for (int i = 0; i < output.size(); i++) {
            byteArray[i] = output.get(i);
        }
        ByteBuffer buffer = ByteBuffer.wrap(byteArray);

        Path filePath = Path.of("decompressed_" + path);

        FileChannel fileChannel = FileChannel.open(filePath,
            StandardOpenOption.CREATE,
            StandardOpenOption.WRITE
        );
        fileChannel.write(buffer);
        fileChannel.close();
    }
}
