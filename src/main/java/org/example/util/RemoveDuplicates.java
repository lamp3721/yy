package org.example.util;

import java.io.*;
import java.nio.file.*;
import java.util.LinkedHashSet;
import java.util.Set;

public class RemoveDuplicates {

    private static final String RESOURCE_FILE_NAME = "一言.txt";
    private static final String OUTPUT_FILE_PATH = "去重后的内容.txt";

    public static void main(String[] args) {
        try (InputStream inputStream = RemoveDuplicates.class.getClassLoader().getResourceAsStream(RESOURCE_FILE_NAME)) {
            if (inputStream == null) {
                throw new IOException("资源文件未找到：" + RESOURCE_FILE_NAME);
            }

            Set<String> uniqueRecords = processFile(inputStream);

            writeToFile(OUTPUT_FILE_PATH, uniqueRecords);

            System.out.println("去重后的内容已写入 " + OUTPUT_FILE_PATH);
        } catch (IOException e) {
            System.err.println("文件处理过程中出现错误: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static Set<String> processFile(InputStream inputStream) throws IOException {
        Set<String> uniqueRecords = new LinkedHashSet<>();
        StringBuilder entryBuilder = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 8192)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("*:")) {
                    if (!entryBuilder.isEmpty()) {
                        uniqueRecords.add(entryBuilder.toString().trim());
                        entryBuilder.setLength(0);
                    }
                }
                entryBuilder.append(line).append(System.lineSeparator());
            }

            if (!entryBuilder.isEmpty()) {
                uniqueRecords.add(entryBuilder.toString().trim());
            }
        }
        return uniqueRecords;
    }

    private static void writeToFile(String outputFilePath, Set<String> uniqueRecords) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(outputFilePath), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
             BufferedWriter bufferedWriter = new BufferedWriter(writer, 8192)) { // 添加缓冲
            for (String record : uniqueRecords) {
                bufferedWriter.write(record);
                bufferedWriter.newLine();
            }
        }
    }
}