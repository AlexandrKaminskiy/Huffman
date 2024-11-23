package com.company;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        Compressor compressor = new Compressor();
        compressor.compress("test.txt");
        compressor.decompress("compressed_test.txt");
    }
}

