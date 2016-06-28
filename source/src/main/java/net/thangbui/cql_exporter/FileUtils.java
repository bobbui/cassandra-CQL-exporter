/*
 * Copyright (c) 2016, Bui Nguyen Thang, thang.buinguyen@gmail.com, thangbui.net. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package net.thangbui.cql_exporter;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

/**
 * Created by Bui Nguyen Thang on 6/3/2016.
 */
public class FileUtils {
    private static Scanner scanner;

    static File verify(String filePath, boolean forceOverwrite) throws IOException {
        File file;
        if (forceOverwrite) {
            file = new File(filePath);
            file.createNewFile();
        } else {
            file = verifyFileExistence(filePath);
        }

        if (!file.canWrite()) {
            throw new RuntimeException("Do not have permission to write to " + file.getAbsolutePath());
        }
        return file;
    }

    private static File verifyFileExistence(String filePath) throws IOException {
        File file = new File(filePath);
        if (file.exists()) {
            if (file.isDirectory()) {
                System.out.printf("File \"%s\" is a directory!" + Main.LINE_SEPARATOR, file.getCanonicalPath());
                System.out.println("Please enter new file path:");
                String newFileName = scanner.next();
                verifyFileExistence(newFileName);
            }

            System.out.printf("File \"%s\" already exists. Do you want to overwrite it? (Y/N)" + Main.LINE_SEPARATOR, file
                    .getCanonicalPath());

            if (scanner == null) {
                scanner = new Scanner(System.in);
            }
            String inputValue = scanner.next();
            return processOption(file, inputValue);
        } else {
            file.createNewFile();
        }
        return file;
    }

    private static File processOption(File originalFile, String inputValue) throws IOException {
        if ("y".equalsIgnoreCase(inputValue)) {
            originalFile.createNewFile();
            return originalFile;
        } else if ("n".equalsIgnoreCase(inputValue)) {
            System.out.println("Please enter new file path:");
            String newFileName = scanner.next();
            return verifyFileExistence(newFileName);
        } else {
            System.out.println("Please type Y/y or N/n only!");
            String next1 = scanner.next();
            return processOption(originalFile, next1);
        }
    }
}
