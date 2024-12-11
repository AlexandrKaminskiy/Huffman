package com.company;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Compressor {

    private static final byte ZERO_BYTE = 0;
    private static final int BYTE_SIZE_IN_BITS = 8;

    private static final int OFFSET_FOR_FILE_SIZE = 4;
    private static final int OFFSET_FOR_LAST_BYTE_OFFSET = 8;

    /**
     * Offset value size for last byte
     */
    private static final int OFFSET_SIZE_IN_BYTES = 1;
    private static final int PREFIX_STRING_SIZE_IN_BYTES = 4;
    private static final int FILE_SIZE_IN_BYTES = 4;

    public void compress(String path, String outputPath) throws IOException {

        byte[] bytes = Files.readAllBytes(Paths.get(path));
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
        List<Byte> prefixString = rootNode.getPrefixString();

        Map<Byte, String> map = new HashMap<>();
        rootNode.getSymbolMap("", map);

        if (map.size() == 1) {
            map.put(rootNode.symbol, String.valueOf(ZERO_BYTE));
        }

        ByteBuffer outputBuffer = ByteBuffer.allocate(PREFIX_STRING_SIZE_IN_BYTES + FILE_SIZE_IN_BYTES + OFFSET_SIZE_IN_BYTES + prefixString.size() + bytes.length + 1);

        outputBuffer.putInt(prefixString.size());
        outputBuffer.putInt(0); //size of file
        outputBuffer.put((byte) 0);

        for (Byte aByte : prefixString) {
            outputBuffer.put(aByte);
        }

        String currentString = "";
        int fileSize = 0;

        for (int i = 0; i < bytes.length; i++) {
            String encodedSymbol = map.get(bytes[i]);
            currentString += encodedSymbol;
            if (currentString.length() >= BYTE_SIZE_IN_BITS) {
                String bytePart = currentString.substring(0, BYTE_SIZE_IN_BITS);
                currentString = currentString.substring(BYTE_SIZE_IN_BITS);
                byte current = (byte) (int) Integer.valueOf(bytePart, 2);
                outputBuffer.put(current);
            }
            fileSize++;
        }
        if (!currentString.isEmpty()) {
            int length = currentString.length();
            byte offset = (byte) ((BYTE_SIZE_IN_BITS - length % BYTE_SIZE_IN_BITS) % BYTE_SIZE_IN_BITS);
            for (int i = 0; i < BYTE_SIZE_IN_BITS - length; i++) {
                currentString = '0' + currentString;
            }
            byte current = (byte) (int) Integer.valueOf(currentString, 2);
            outputBuffer.put(current);

            outputBuffer.put(OFFSET_FOR_LAST_BYTE_OFFSET, offset);

        }
        outputBuffer.putInt(OFFSET_FOR_FILE_SIZE, fileSize);

        outputBuffer.flip();

        Path filePath = Paths.get(outputPath);

        FileChannel fileChannel = FileChannel.open(filePath,
            StandardOpenOption.CREATE,
            StandardOpenOption.WRITE
        );
        fileChannel.write(outputBuffer);
        fileChannel.close();
    }

    public void decompress(String path, String outputPath) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        ByteBuffer inputBuffer = ByteBuffer.wrap(bytes);
        int prefixStringSize = inputBuffer.getInt();
        int fileSize = inputBuffer.getInt();
        byte offset = inputBuffer.get();
        Node rootNode = new Node(inputBuffer, prefixStringSize);
        Node currentNode = rootNode;
        ByteBuffer buffer = ByteBuffer.allocate(fileSize);
        while (inputBuffer.hasRemaining()) {
            byte b = inputBuffer.get();
            int lastByteOffset = inputBuffer.hasRemaining() ? 0 : offset;
            int mask = (0b10000000 >>> lastByteOffset);
            for (int i = lastByteOffset; i < BYTE_SIZE_IN_BITS; i++) {
                byte current = (byte) ((b & mask) >>> (BYTE_SIZE_IN_BITS - i - 1));
                assert current == 1 || current == 0;

                if ((currentNode.right == null && currentNode.left == null) ||
                    (rootNode.right == null && rootNode.left == null)) {
                    buffer.put(currentNode.symbol);
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
        System.out.println(LocalDateTime.now() + " writing into buffer");
        if (rootNode.left != null && rootNode.right != null) {
            buffer.put(currentNode.symbol);
        }

        buffer.flip();
        Path filePath = Paths.get(outputPath);
        try (FileChannel fileChannel = FileChannel.open(filePath,
            StandardOpenOption.CREATE,
            StandardOpenOption.WRITE)
        ) {
            fileChannel.write(buffer);
        }

    }
}
