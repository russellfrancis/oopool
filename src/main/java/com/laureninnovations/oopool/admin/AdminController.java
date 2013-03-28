/*-
 * Copyright (c) 2013, Lauren Innovations
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * 4. Neither the name of the Lauren Innovations nor the names of its contributors
 * may be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.laureninnovations.oopool.admin;

import com.laureninnovations.oopool.config.Configuration;
import com.laureninnovations.util.ApplicationService;
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

/**
 * This is the main work-horse of the AdminServer it listens for incoming requests on a Socket and dispatches new
 * AdminRequestHandler instances to a thread pool to process each incoming dialog.
 *
 * @author Russell Francis (russell.francis@metro-six.com)
 */
public class AdminController extends Thread implements ApplicationService {
    static private final Logger log = LoggerFactory.getLogger(AdminController.class);

    private AtomicBoolean initialized = new AtomicBoolean(false);
    private AtomicBoolean shutdown = new AtomicBoolean(false);

    private ServerSocket serverSocket;
    private ExecutorService executorService;

    @Autowired
    private Configuration configuration;

    @Autowired
    private ApplicationContext applicationContext;

    /**
     * Initialize the state of this instance.
     *
     * @throws IOException If there is an error initializing the state of this instance.
     */
    public void init() throws IOException {
        if (!initialize()) {
            throw new IllegalStateException("The init method may only be called one time.");
        }
        setServerSocket(newServerSocket());
        setExecutorService(newExecutorService());
    }

    /**
     * Determine if this instance has been initialized.
     *
     * @return true if we have been initialized, false otherwise.
     */
    public boolean isInitialized() {
        return initialized.get();
    }

    /**
     * Declare that we will be executing the initialization sequence.
     *
     * @return true if we are the first caller to execute this method.  All subsequent calls will return false.
     */
    protected boolean initialize() {
        return !initialized.getAndSet(true);
    }

    /**
     * Start the AdminController, this will begin listening for incoming admin requests on the designated port.
     */
    public void startup() {
        this.start();
    }

    /**
     * Shutdown the AdminController, this will stop listening for incoming connections on the designated admin port.
     */
    public void shutdown() {
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
                try {
                    getServerSocket().close();
                } catch (IOException e) {
                    if (log.isErrorEnabled()) {
                        log.error("Exception closing admin server listening socket: " + e.getMessage(), e);
                    }
                }
            }
        } finally {
            if (log.isInfoEnabled()) {
                log.info("ADMIN SHUTDOWN COMPLETE");
            }
        }
    }

    /**
     * Wait for the shutdown process to complete cleanly.
     */
    public void awaitShutdown() {
        try {
            this.join();
        } catch (InterruptedException e) {
            if (log.isWarnEnabled()) {
                log.warn(AdminController.class.getName() + " shutdown interrupted.");
            }
        }
    }

    /**
     * Determine if we have been shutdown.
     *
     * @return true if we have entered the shutdown sequence, false otherwise.
     */
    public boolean isShutdown() {
        return shutdown.get();
    }

    /**
     * Mark this instance as having entered the shutdown sequence.
     *
     * @return true if this is the first time we have been called, false each additional time.
     */
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
        return Executors.newCachedThreadPool();
    }
}
