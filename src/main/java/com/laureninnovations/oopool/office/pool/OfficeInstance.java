package com.laureninnovations.oopool.office.pool;

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
import java.net.Socket;

public class OfficeInstance implements XInstanceProvider {

    static private final Logger log = LoggerFactory.getLogger(OfficeInstance.class);

    @Autowired
    private IOUtil ioUtil;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    @Qualifier("ooBridgeFactory")
    private XBridgeFactory bridgeFactory;

    private ProcessBuilder processBuilder;
    private boolean isIdle = true;
    private boolean isListening = false;
    private Process process;
    private File userInstallationDir;
    private int port;

    private XBridge bridge;

    public String getName() {
        return "instance-" + getPort();
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

    synchronized public void start() throws Exception {
        // only startup an instance if we are not already running an instance.
        if (process == null) {
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
    }

    synchronized public void awaitStartup() throws InterruptedException {
        if (process == null) {
            throw new IllegalStateException("You must start the instance before you await for startup!");
        }

        if (!isListening) {
            if (log.isDebugEnabled()) {
                log.debug("Waiting for " + getName() + " to startup.");
            }

            int attempts = 30;
            do {
                try {
                    Socket s = new Socket("127.0.0.1", getPort());
                    s.close();
                    isListening = true;
                    return;
                } catch (IOException e) {
                    --attempts;
                    Thread.sleep(1000);
                }
            } while (attempts >= 0);

            throw new IllegalStateException("Unable to start child open office instance.");
        }
    }

    synchronized public void stop() throws IOException, InterruptedException {
        log.info("STOPPING INSTANCE " + getName());
        try {
            if (process != null) {
                process.destroy();
                process.waitFor();
            }

            // Cleanup the user environment directory.
            getIoUtil().delete(userInstallationDir);
        } finally {
            process = null;
            isListening = false;
        }
    }

    synchronized public void bridgeConnection(XConnection clientConnection) throws Exception {
        isIdle = false;
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
            isIdle = true;
        }
    }

    synchronized public boolean isReapable() {
        return isIdle && process != null;
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
