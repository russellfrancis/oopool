package com.laureninnovations.oopool.office;

import com.laureninnovations.oopool.config.Configuration;
import com.laureninnovations.oopool.office.pool.OfficePool;
import com.laureninnovations.util.ApplicationService;
import com.sun.star.connection.ConnectionSetupException;
import com.sun.star.connection.XAcceptor;
import com.sun.star.connection.XConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Listen for requests on our bridge open office port and create OfficeRequestHandlers which are dispatched to a
 * thread pool to process each request as it arrives.  The thread pool naturally provides protection against resource
 * exhaustion when a spike of requests comes in during a short period and allows requests to be queued and matched up
 * with an open office instance from the pool when one becomes available.
 *
 * @author Russell Francis (russell.francis@metro-six.com)
 */
public class OfficeController extends Thread implements ApplicationService {
    static private final Logger log = LoggerFactory.getLogger(OfficeController.class);

    private AtomicBoolean initialized = new AtomicBoolean(false);
    private AtomicBoolean shutdown = new AtomicBoolean(false);

    private ExecutorService executorService;

    @Autowired
    private Configuration configuration;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private XAcceptor ooAcceptor;

    @Autowired
    private OfficePool officePool;

    public void init() throws Exception {
        if (!initialize()) {
            throw new IllegalStateException("The init method may only be called once.");
        }
        setExecutorService(newExecutorService());
    }

    public boolean isInitialized() {
        return initialized.get();
    }

    protected boolean initialize() {
        return !initialized.getAndSet(true);
    }

    public void startup() {
        this.start();
    }

    public void shutdown() {
        if (!markShutdown()) {
            throw new IllegalStateException("The shutdown method may only be called once.");
        }

        try {
            if (log.isInfoEnabled()) {
                log.info("OFFICE SHUTDOWN EXECUTOR SERVICE");
            }
            ooAcceptor.stopAccepting();
            getExecutorService().shutdown();
            officePool.shutdown();
        } finally {
            if (log.isInfoEnabled()) {
                log.info("OFFICE SERVER SHUTDOWN COMPLETE");
            }
        }
    }

    public void awaitShutdown() {
        try {
            officePool.awaitShutdown();
            this.join();
        } catch (InterruptedException e) {
            if (log.isWarnEnabled()) {
                log.warn(OfficeController.class.getName() + " was interrupted during the shutdown process.");
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
        return Executors.newFixedThreadPool(getConfiguration().getMaxPoolSize());
    }

    protected OfficeRequestHandler newOfficeRequestHandler(XConnection connection) {
        OfficeRequestHandler handler = applicationContext.getBean(OfficeRequestHandler.class);
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
}
