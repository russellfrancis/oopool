package com.laureninnovations.oopool.office.pool;

import com.laureninnovations.oopool.config.Configuration;
import com.laureninnovations.util.ApplicationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * This pool manages a collection of OfficeInstances allowing a user to grab an available instance before use.
 *
 * @author Russell Francis (russell.francis@metro-six.com)
 */
public class OfficePool implements ApplicationService {
    static private final Logger log = LoggerFactory.getLogger(OfficePool.class);
    static private final String LOCALHOST = "127.0.0.1";

    private CountDownLatch shutdownLatch = new CountDownLatch(1);
    private List<OfficeInstance> instances = new ArrayList<OfficeInstance>();
    private LinkedBlockingDeque<OfficeInstance> availableInstances = new LinkedBlockingDeque<OfficeInstance>();

    @Autowired
    private OfficePoolStatistics officePoolStatistics;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private Configuration configuration;

    public OfficePoolStatistics getOfficePoolStatistics() {
        return officePoolStatistics;
    }

    public void init() throws Exception {
        int maxPoolSize = getConfiguration().getMaxPoolSize();
        int port = getConfiguration().getFirstWorkerPort();
        for (int i = 0; i < maxPoolSize; ++i) {
            OfficeInstance instance = newOfficeInstance(LOCALHOST, port + i);
            instances.add(instance);
            availableInstances.add(instance);
            officePoolStatistics.addOfficeInstance(instance);
        }
    }

    public void startup() {
    }

    public void shutdown() {
        for (OfficeInstance instance : instances) {
            try {
                instance.stop();
            } catch (Exception e) {
                if (log.isErrorEnabled()) {
                    log.error(e.getMessage(), e);
                }
            }
        }
        shutdownLatch.countDown();
    }

    public void awaitShutdown() {
        try {
            shutdownLatch.await();
        } catch (InterruptedException e) {
            if (log.isWarnEnabled()) {
                log.error(OfficePool.class.getName() + " was interrupted during shutdown.");
            }
        }
    }

    public OfficeInstance acquireInstance() throws InterruptedException {
        return availableInstances.take();
    }

    public void releaseInstance(OfficeInstance instance) throws InterruptedException {
        availableInstances.addFirst(instance);
    }

    protected Configuration getConfiguration() {
        return configuration;
    }

    protected void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    protected OfficeInstance newOfficeInstance(String host, int port) throws Exception {
        File userInstallationDir = new File(getConfiguration().getOfficeBaseUserDirectory(), "instance-" + port);

        OfficeInstance officeInstance = applicationContext.getBean(OfficeInstance.class);
        officeInstance.setUserInstallationDir(userInstallationDir);
        officeInstance.setProcessBuilder(newProcessBuilder(userInstallationDir, host, port));
        officeInstance.setPort(port);
        officeInstance.setName("instance-" + port);
        return officeInstance;
    }

    protected ProcessBuilder newProcessBuilder(File userInstallationDir, String host, int port) throws Exception {
        ProcessBuilder processBuilder = new ProcessBuilder(buildSofficeCommandLine(userInstallationDir, host, port));
        processBuilder.redirectErrorStream(true);
        return processBuilder;
    }

    protected List<String> buildSofficeCommandLine(File userInstallationDir, String host, int port) throws Exception {
        List<String> commandLine = new ArrayList<String>();
        commandLine.add(getSofficeCommand());
        commandLine.add("--nologo");
        commandLine.add("--headless");
        commandLine.add("--nofirststartwizard");
        commandLine.add("--accept=socket,host=0,port=" + port + ";urp;");
        commandLine.add("-env:UserInstallation=file://" + userInstallationDir.getAbsolutePath());
        return commandLine;
    }

    protected String getSofficeCommand() throws Exception {
        // Verify that our command exists and is executable for starting open office instances.
        File command = new File(new File(configuration.getOfficeBaseDirectory(), "program"), "soffice");
        return command.getAbsolutePath();
    }
}
