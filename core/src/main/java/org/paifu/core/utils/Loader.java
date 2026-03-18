package org.paifu.core.utils;

import org.lwjgl.system.MemoryUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Loader {

    public static FloatBuffer storeDataInFloatBuffer(float[] data) {
        FloatBuffer buffer = MemoryUtil.memAllocFloat(data.length);
        buffer.put(data).flip();
        return buffer;
    }

    public static IntBuffer storeDataInIntBuffer(int[] data) {
        IntBuffer buffer = MemoryUtil.memAllocInt(data.length);
        buffer.put(data).flip();
        return buffer;
    }

    public static String loadResource(String fileName) throws Exception {
        String result;

        try (InputStream in = Loader.class.getResourceAsStream(fileName);
             Scanner scanner = new Scanner(in, StandardCharsets.UTF_8)) {
            result = scanner.useDelimiter("\\A").next();
        }

        return result;
    }

    public static String loadShader(String resourcePath) throws IOException {
        try (InputStream in = Loader.class.getResourceAsStream(resourcePath)) {
            if (in == null)
                throw new IOException("Shader resource not found: " + resourcePath);

            byte[] bytes = in.readAllBytes();
            String src = new String(bytes, StandardCharsets.UTF_8);
            if (src.startsWith("\uFEFF")) src = src.substring(1);
            src = src.replace("\r\n", "\n");
            src = src.trim();
            return src;
        }
    }

    public static List<String> readAllLines(String fileName) throws Exception {
        List<String> lines = new ArrayList<>();

        InputStream is = Loader.class
                .getClassLoader()
                .getResourceAsStream(fileName);

        if (is == null) {
            throw new RuntimeException("Resource not found: " + fileName);
        }

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(is, StandardCharsets.UTF_8))) {

            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
        }

        return lines;
    }

}
