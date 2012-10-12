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

    /**
     * Reads the entire content of the input stream into a String. Assumes UTF-8 encoding.
     *
     * @param ins The input stream to read from.
     * @return A string with the entire content of the input stream as a UTF-8 string.
     * @throws IOException If there is an error reading from the input stream.
     */
    public String readToString(InputStream ins) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            drain(ins, baos);
        } finally {
            baos.close();
        }
        return baos.toString("UTF-8");
    }

    /**
     * Drain all of the data from the input stream and write it to the output stream.
     *
     * @param ins The input stream to read from.
     * @param outs The output stream to write to.
     * @return The number of bytes transferred between the streams.
     * @throws IOException If there is an error reading or writing to the streams.
     */
    public int drain(InputStream ins, OutputStream outs) throws IOException {
        int bytesWritten = 0, bytesRead;
        byte[] b = new byte[4096];
        while ((bytesRead = ins.read(b)) != -1) {
            outs.write(b, 0, bytesRead);
            bytesWritten += bytesRead;
        }
        return bytesWritten;
    }
}
