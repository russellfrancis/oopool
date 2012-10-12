package com.laureninnovations.oopool.office;

import com.laureninnovations.oopool.config.Configuration;
import com.sun.star.bridge.XBridgeFactory;
import com.sun.star.comp.helper.Bootstrap;
import com.sun.star.connection.ConnectionSetupException;
import com.sun.star.connection.XAcceptor;
import com.sun.star.connection.XConnection;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.io.IOException;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class OfficeController extends Thread {
    static private final Logger log = LoggerFactory.getLogger(OfficeController.class);

    private AtomicBoolean initialized = new AtomicBoolean(false);
    private AtomicBoolean shutdown = new AtomicBoolean(false);
    private int minThreadPoolSize = 1;
    private long keepAliveTime = 60;
    private int queueCapacity = 1000;

    private XComponentContext ooContext;
    private XMultiComponentFactory ooManager;
    private XAcceptor ooAcceptor;
    private XBridgeFactory ooBridgeFactory;

    private ExecutorService executorService;

    @Autowired
    private Configuration configuration;

    @Autowired
    private ApplicationContext applicationContext;

    public void init() throws Exception {
        if (!initialize()) {
            throw new IllegalStateException("The init method may only be called once.");
        }
        setExecutorService(newExecutorService());

        ooContext = Bootstrap.createInitialComponentContext(null);
        ooManager = ooContext.getServiceManager();
        ooAcceptor = UnoRuntime.queryInterface(XAcceptor.class,
                ooManager.createInstanceWithContext("com.sun.star.connection.Acceptor", ooContext));
        ooBridgeFactory = UnoRuntime.queryInterface(XBridgeFactory.class,
                ooManager.createInstanceWithContext("com.sun.star.bridge.BridgeFactory", ooContext));
    }

    public boolean isInitialized() {
        return initialized.get();
    }

    protected boolean initialize() {
        return !initialized.getAndSet(true);
    }

    public void shutdown() throws IOException {
        if (!markShutdown()) {
            throw new IllegalStateException("The shutdown method may only be called once.");
        }

        try {
            if (log.isInfoEnabled()) {
                log.info("OFFICE SHUTDOWN EXECUTOR SERVICE");
            }
            ooAcceptor.stopAccepting();
            getExecutorService().shutdown();
        } finally {
            if (log.isInfoEnabled()) {
                log.info("OFFICE SERVER SHUTDOWN COMPLETE");
            }
        }
    }

    public boolean isShutdown() {
        return shutdown.get();
    }

    protected boolean markShutdown() {
        return !shutdown.getAndSet(true);
    }

    public void run() {
        if (!isInitialized()) {
            throw new RuntimeException("The init() method must be called before execution.");
        }

        try {
            if (log.isInfoEnabled()) {
                log.info("OFFICE LISTENING ON " + getConfiguration().getPoolPort());
            }
            while (!isShutdown()) {
                // The incoming client connection.
                XConnection connection = ooAcceptor.accept(getOOAccepterConfig());
                OfficeRequestHandler handler = newOfficeRequestHandler(connection);
                getExecutorService().submit(handler);
            }
        } catch (Exception e) {
            // If we were interrupted as part of the shutdown process don't show
            // the exception method.
            if (!isShutdown() || !(e instanceof ConnectionSetupException)) {
                if (log.isErrorEnabled()) {
                    log.error(e.getMessage(), e);
                }
            }
        }
    }

    protected ExecutorService newExecutorService() {
        return new ThreadPoolExecutor(
                getMinThreadPoolSize(),
                getConfiguration().getMaxPoolSize(),
                getKeepAliveTime(),
                TimeUnit.SECONDS,
                newBlockingQueue(getQueueCapacity())
        );
    }

    protected BlockingQueue<Runnable> newBlockingQueue(int capacity) {
        return new ArrayBlockingQueue<Runnable>(capacity);
    }

    protected OfficeRequestHandler newOfficeRequestHandler(XConnection connection) {
        OfficeRequestHandler handler = applicationContext.getBean(OfficeRequestHandler.class);
        handler.setOfficeController(this);
        handler.setConnection(connection);
        return handler;
    }

    protected String getOOAccepterConfig() {
        return "socket,host=0,port=" + getConfiguration().getPoolPort();
    }

    protected Configuration getConfiguration() {
        return configuration;
    }

    protected void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    protected ExecutorService getExecutorService() {
        return executorService;
    }

    protected void setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
    }

    public int getMinThreadPoolSize() {
        return minThreadPoolSize;
    }

    public void setMinThreadPoolSize(int minThreadPoolSize) {
        this.minThreadPoolSize = minThreadPoolSize;
    }

    public long getKeepAliveTime() {
        return keepAliveTime;
    }

    public void setKeepAliveTime(long keepAliveTime) {
        this.keepAliveTime = keepAliveTime;
    }

    public int getQueueCapacity() {
        return queueCapacity;
    }

    public void setQueueCapacity(int queueCapacity) {
        this.queueCapacity = queueCapacity;
    }
}
