package com.laureninnovations.oopool.office.pool;

import com.laureninnovations.util.IOUtil;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;

public class OfficeInstance {

    @Autowired
    private IOUtil ioUtil;

    private ProcessBuilder processBuilder;
    private Process process;
    private File userInstallationDir;
    private int port;

    synchronized public void start() throws Exception {
        if (getUserInstallationDir().exists()) {
            getIoUtil().delete(getUserInstallationDir());
        }
        if (!getUserInstallationDir().mkdir()) {
            throw new Exception("Unable to create user instance directory '" + getUserInstallationDir().getAbsolutePath() + "'.");
        }
        process = processBuilder.start();

        // close stdin.
        process.getOutputStream().close();

        // stdout and stderr are merged.
    }

    synchronized public void stop() throws IOException, InterruptedException {
        try {
            process.destroy();
            process.waitFor();

            // Cleanup the user environment directory.
            getIoUtil().delete(userInstallationDir);
        } finally {
            userInstallationDir = null;
            process = null;
        }
    }

    public ProcessBuilder getProcessBuilder() {
        return processBuilder;
    }

    public void setProcessBuilder(ProcessBuilder processBuilder) {
        this.processBuilder = processBuilder;
    }

    public File getUserInstallationDir() {
        return userInstallationDir;
    }

    public void setUserInstallationDir(File userInstallationDir) {
        this.userInstallationDir = userInstallationDir;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    protected IOUtil getIoUtil() {
        return ioUtil;
    }

    protected void setIoUtil(IOUtil ioUtil) {
        this.ioUtil = ioUtil;
    }
}
