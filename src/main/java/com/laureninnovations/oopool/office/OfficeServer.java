package com.laureninnovations.oopool.office;

import com.laureninnovations.util.ApplicationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.CountDownLatch;

/**
 * This is the main server which manages all of our child open office instances and other necessary services they
 * depend on.
 *
 * @author Russell Francis (russell.francis@metro-six.com)
 */
public class OfficeServer extends Thread implements ApplicationService {
    static private final Logger log = LoggerFactory.getLogger(OfficeServer.class);

    private CountDownLatch shutdownLatch = new CountDownLatch(1);

    @Autowired
    private OfficeController officeController;

    @Override
    public void run() {
        officeController.start();
        try {
            shutdownLatch.await();

            officeController.shutdown();
            officeController.join();
        } catch (InterruptedException e) {
            if (log.isDebugEnabled()) {
                log.debug("OfficeServer interrupted");
            }
        }
    }

    /**
     * Startup this service.
     */
    public void startup() {
        this.start();
    }

    /**
     * Request that this service shutdown.
     */
    public void shutdown() {
        shutdownLatch.countDown();
    }

    /**
     * Await the completion of the shutdown sequence.
     */
    public void awaitShutdown() {
        try {
            this.join();
        } catch (InterruptedException e) {
            if (log.isWarnEnabled()) {
                log.warn(OfficeServer.class.getName() + " was interrupted during shutdown.");
            }
        }
    }
}
