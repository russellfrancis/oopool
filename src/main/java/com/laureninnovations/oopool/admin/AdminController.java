package com.laureninnovations.oopool.admin;

import com.laureninnovations.oopool.config.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class AdminController extends Thread {
    static private final Logger log = LoggerFactory.getLogger(AdminController.class);

    private AtomicBoolean initialized = new AtomicBoolean(false);
    private AtomicBoolean shutdown = new AtomicBoolean(false);
    private int minThreadPoolSize = 1;
    private int maxThreadPoolSize = 5;
    private long keepAliveTime = 60;
    private int queueCapacity = 10;

    private ServerSocket serverSocket;
    private ExecutorService executorService;

    @Autowired
    private Configuration configuration;

    @Autowired
    private ApplicationContext applicationContext;

    public void init() throws IOException {
        if (!initialize()) {
            throw new IllegalStateException("The init method may only be called one time.");
        }
        setServerSocket(newServerSocket());
        setExecutorService(newExecutorService());
    }

    public boolean isInitialized() {
        return initialized.get();
    }

    protected boolean initialize() {
        return !initialized.getAndSet(true);
    }

    public void shutdown() throws IOException {
        if (!markShutdown()) {
            throw new IllegalStateException("The shutdown method may only be called one time.");
        }

        try {
            if (isInitialized()) {
                // Initiate shutdown of our executor thread pool.
                if (log.isInfoEnabled()) {
                    log.info("ADMIN SHUTDOWN EXECUTOR SERVICE");
                }
                getExecutorService().shutdown();

                // Initiate shutdown of our listening socket.
                if (log.isInfoEnabled()) {
                    log.info("ADMIN SHUTDOWN LISTENING SOCKET");
                }
                getServerSocket().close();
            }
        } finally {
            if (log.isInfoEnabled()) {
                log.info("ADMIN SHUTDOWN COMPLETE");
            }
        }
    }

    public boolean isShutdown() {
        return shutdown.get();
    }

    private boolean markShutdown() {
        return !shutdown.getAndSet(true);
    }

    public void run() {
        if (!isInitialized()) {
            throw new RuntimeException("The init() method must be called before execution.");
        }

        try {
            if (log.isInfoEnabled()) {
                log.info("ADMIN LISTENING ON " + getServerSocket().getLocalPort());
            }
            while (!isShutdown()) {
                Socket socket = getServerSocket().accept();
                AdminRequestHandler handler = newAdminRequestHandler(socket);
                getExecutorService().execute(handler);
            }
        } catch (IOException e) {
            if (isShutdown() && e instanceof SocketException) {
                if (log.isInfoEnabled()) {
                    log.info("Received and responded to socket close request.");
                }
            } else {
                if (log.isErrorEnabled()) {
                    log.error(e.getMessage(), e);
                }
            }
        }
    }

    public int getMinThreadPoolSize() {
        return minThreadPoolSize;
    }

    public void setMinThreadPoolSize(int minThreadPoolSize) {
        this.minThreadPoolSize = minThreadPoolSize;
    }

    public int getMaxThreadPoolSize() {
        return maxThreadPoolSize;
    }

    public void setMaxThreadPoolSize(int maxThreadPoolSize) {
        this.maxThreadPoolSize = maxThreadPoolSize;
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

    protected Configuration getConfiguration() {
        return configuration;
    }

    protected void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    protected ServerSocket getServerSocket() {
        return serverSocket;
    }

    protected void setServerSocket(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    protected ExecutorService getExecutorService() {
        return executorService;
    }

    protected void setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
    }

    protected ServerSocket newServerSocket() throws IOException {
        return new ServerSocket(getConfiguration().getPoolAdminPort());
    }

    protected AdminRequestHandler newAdminRequestHandler(Socket socket) {
        AdminRequestHandler handler = applicationContext.getBean(AdminRequestHandler.class);
        handler.setSocket(socket);
        return handler;
    }

    protected ExecutorService newExecutorService() {
        return new ThreadPoolExecutor(
                getMinThreadPoolSize(),
                getMaxThreadPoolSize(),
                getKeepAliveTime(),
                TimeUnit.SECONDS,
                newBlockingQueue(getQueueCapacity())
        );
    }

    protected BlockingQueue<Runnable> newBlockingQueue(int capacity) {
        return new ArrayBlockingQueue<Runnable>(capacity);
    }
}
