package com.laureninnovations.oopool.office.pool;

import com.google.gson.annotations.Expose;
import com.laureninnovations.util.IOUtil;
import com.sun.star.bridge.XBridge;
import com.sun.star.bridge.XBridgeFactory;
import com.sun.star.bridge.XInstanceProvider;
import com.sun.star.connection.XConnection;
import com.sun.star.connection.XConnectionBroadcaster;
import com.sun.star.connection.XConnector;
import com.sun.star.uno.UnoRuntime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.Socket;
import java.util.Date;
import java.util.concurrent.Semaphore;

/**
 * Represents an OpenOffice instance which may or may not be started up and running on the host machine.  The instance
 * can be brought up and down using the start(), stop() and awaitStartup() methods.
 *
 * @author Russell Francis (russell.francis@metro-six.com)
 */
public class OfficeInstance implements XInstanceProvider {

    static private final Logger log = LoggerFactory.getLogger(OfficeInstance.class);

    static public class Statistics implements Serializable {
        static private final String STATE_IDLE = "IDLE";
        static private final String STATE_WORKING = "WORKING";
        static private final String STATE_STOPPED = "STOPPED";

        private transient OfficeInstance instance;

        @Expose
        private String name = "";
        @Expose
        private String state = STATE_STOPPED;
        @Expose
        private long jobsProcessed = 0;
        @Expose
        private long totalJobsProcessed = 0;
        @Expose
        private Date idleSince = new Date();

        protected Statistics(OfficeInstance instance) {
            this.instance = instance;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        synchronized public String getState() {
            return state;
        }

        synchronized public long getJobsProcessed() {
            return jobsProcessed;
        }

        synchronized public long getTotalJobsProcessed() {
            return totalJobsProcessed;
        }

        synchronized public Date getIdleSince() {
            // This indicates that the job is currently executing.
            return idleSince != null ? idleSince : new Date();
        }

        synchronized protected void setIdleSince(Date idleSince) {
            this.idleSince = idleSince;
        }

        synchronized protected void startedInstance() {
            state = STATE_IDLE;
            idleSince = new Date();
        }

        synchronized protected void startedJob() {
            state = STATE_WORKING;
            idleSince = null;
        }

        synchronized protected void finishedJob() {
            jobsProcessed++;
            totalJobsProcessed++;
            state = STATE_IDLE;
            idleSince = new Date();
        }

        synchronized protected void stoppedInstance() {
            state = STATE_STOPPED;
            jobsProcessed = 0;
        }
    }

    @Autowired
    private IOUtil ioUtil;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    @Qualifier("ooBridgeFactory")
    private XBridgeFactory bridgeFactory;

    private String name = "";
    private boolean isListening = false;
    private Semaphore permitWork = new Semaphore(0);
    private OfficeInstanceReaper reaper;
    private ProcessBuilder processBuilder;
    private Process process;
    private File userInstallationDir;
    private int port;

    private Statistics statistics = new Statistics(this);

    private XBridge bridge;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        this.statistics.setName(name);
    }

    public Object getInstance(String name) {
        if (log.isTraceEnabled()) {
            log.trace("resolving name: " + name);
        }

        Object o = bridge.getInstance(name);

        if (log.isTraceEnabled()) {
            log.trace("value = " + o);
        }
        return o;
    }

    public OfficeInstance.Statistics getStatistics() {
        return statistics;
    }

    synchronized public void start() throws Exception {
        // only startup an instance if we are not already running an instance.
        if (process == null) {
            if (getUserInstallationDir().exists()) {
                getIoUtil().delete(getUserInstallationDir());
            }
            if (!getUserInstallationDir().mkdir()) {
                throw new Exception("Unable to create user instance directory '" + getUserInstallationDir().getAbsolutePath() + "'.");
            }

            statistics.startedInstance();

            reaper = applicationContext.getBean(OfficeInstanceReaper.class);
            reaper.setOfficeInstance(this);
            reaper.startup();

            process = processBuilder.start();

            // close stdin.
            process.getOutputStream().close();

            // stdout and stderr are merged.
        }
    }

    synchronized public void awaitStartup() throws InterruptedException {
        if (process == null) {
            throw new IllegalStateException("You must start the instance before you await for startup!");
        }

        if (permitWork.availablePermits() == 0) {
            if (log.isDebugEnabled()) {
                log.debug("Waiting for " + getName() + " to startup.");
            }

            int attempts = 30;
            do {
                try {
                    Socket s = new Socket("127.0.0.1", getPort());
                    s.close();
                    isListening = true;
                    permitWork.release();
                    return;
                } catch (IOException e) {
                    --attempts;
                    Thread.sleep(1000);
                }
            } while (attempts >= 0);

            throw new IllegalStateException("Unable to start child open office instance.");
        }
    }

    synchronized public void stop() throws InterruptedException {
        if (isListening) {
            log.info("ACQUIRING WORK PERMIT FOR INSTANCE " + getName());
            permitWork.acquire();
            log.info("STOPPING INSTANCE " + getName());
            try {
                // close the reaper instance testing for a shutdown condition.
                reaper.shutdown();
                reaper.awaitShutdown();

                if (process != null) {
                    process.destroy();
                    process.waitFor();
                }

                // Cleanup the user environment directory.
                try {
                    getIoUtil().delete(userInstallationDir);
                } catch (IOException e) {
                    if (log.isErrorEnabled()) {
                        log.error("Unable to cleanup office instance " + getName() + " working directory.");
                    }
                }
            } finally {
                process = null;
                isListening = false;
                statistics.stoppedInstance();
                // Should be unnecessary but is just a precaution.
                permitWork.drainPermits();
            }
        }
    }

    synchronized public void bridgeConnection(XConnection clientConnection) throws Exception {
        statistics.startedJob();
        permitWork.acquire();
        try {
            ConnectionListener clientListener = new ConnectionListener("client");
            ConnectionListener workerListener = new ConnectionListener("worker");

            XConnector workerConnector = applicationContext.getBean("ooConnector", XConnector.class);
            XConnection workerConnection = workerConnector.connect("socket,host=0,port=" + getPort());
            try {
                XConnectionBroadcaster connectionBroadcaster = UnoRuntime.queryInterface(XConnectionBroadcaster.class, workerConnection);
                connectionBroadcaster.addStreamListener(workerListener);

                XConnectionBroadcaster clientConnectionBroadcaster = UnoRuntime.queryInterface(XConnectionBroadcaster.class, clientConnection);
                clientConnectionBroadcaster.addStreamListener(clientListener);

                bridge = bridgeFactory.createBridge("", "urp", workerConnection, null);
                bridgeFactory.createBridge("", "urp", clientConnection, this);

                clientListener.waitForCompletion();
            } finally {
                workerConnection.close();
                workerListener.waitForCompletion();
                bridge = null;
            }
        } finally {
            statistics.finishedJob();
            permitWork.release();
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
