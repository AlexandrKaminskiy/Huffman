package com.company;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        Compressor compressor = new Compressor();
        if (args.length == 0) {
            System.out.println();
        }
        switch (args[0]) {
            case "compress" -> compressor.compress(args[1], args[2]);
            case "decompress" -> compressor.decompress(args[1], args[2]);
        }
    }
}

