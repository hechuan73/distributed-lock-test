package com.chuan.lock.util;

import java.io.*;
import java.util.Objects;

/**
 * @author hechuan
 */
public class FileUtils {

    public static String getScript(String fileName) {
        String path = Objects.requireNonNull(FileUtils.class.getClassLoader().getResource(fileName)).getPath();
        return readFileByLines(path);
    }

    public static String readFileByLines(String filePath) {
        FileInputStream fileInputStream;
        InputStreamReader inputStreamReader;
        BufferedReader bufferedReader = null;
        StringBuilder sb = new StringBuilder();
        String temp;

        try {
            fileInputStream = new FileInputStream(filePath);
            inputStreamReader = new InputStreamReader(fileInputStream);
            bufferedReader = new BufferedReader(inputStreamReader);

            while ((temp = bufferedReader.readLine()) != null) {
                sb.append(temp);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return sb.toString();
    }
}
