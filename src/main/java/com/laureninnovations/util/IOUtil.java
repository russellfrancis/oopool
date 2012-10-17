package com.laureninnovations.util;

import java.io.*;

public class IOUtil {
    /**
     * Recursively delete all files under the provided file.
     *
     * @param file The file or directory to delete.
     * @throws IOException If there is an error deleting a file or directory.
     */
    public void delete(File file) throws IOException {
        if (file.exists()) {
            if (file.isDirectory()) {
                File[] files = file.listFiles();
                if (files != null) {
                    for (File c : files) {
                        delete(c);
                    }
                }
            }

            if (!file.delete()) {
                throw new FileNotFoundException("Failed to delete file: " + file);
            }
        }
    }
}
