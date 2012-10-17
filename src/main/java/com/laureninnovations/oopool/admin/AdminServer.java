package com.laureninnovations.oopool.admin;

import com.laureninnovations.util.ApplicationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * This is the administrative control server which binds to the designated port and handles incoming administrative
 * requests.
 *
 * @author Russell Francis (russell.francis@metro-six.com)
 */
public class AdminServer extends Thread implements ApplicationService {
    static private final Logger log = LoggerFactory.getLogger(AdminServer.class);

    private List<ApplicationService> services = new ArrayList<ApplicationService>();
    private CountDownLatch shutdownLatch = new CountDownLatch(1);

    @Autowired
    private AdminController adminController;

    @Override
    public void run() {
        try {
            adminController.startup();
            shutdownLatch.await();

            adminController.shutdown();
            adminController.awaitShutdown();
        } catch (InterruptedException e) {
            if (log.isDebugEnabled()) {
                log.debug("AdminServer interrupted");
            }
        }
    }

    /**
     * Start up the AdminServer.
     */
    public void startup() {
        this.start();
        for (ApplicationService service : services) {
            service.startup();
        }
    }

    /**
     * Request that this instance shut itself down.
     */
    public void shutdown() {
        for (ApplicationService service : services)  {
            service.shutdown();
        }
        for (ApplicationService service : services) {
            service.awaitShutdown();
        }
        shutdownLatch.countDown();
    }

    /**
     * Await the clean shutdown of this component.
     */
    public void awaitShutdown() {
        try {
            this.join();
        } catch (InterruptedException e) {
            if (log.isWarnEnabled()) {
                log.warn(AdminServer.class.getName() + " shutdown interrupted.");
            }
        }
    }

    /**
     * Register a child service which will be stopped when this service is shutdown.
     *
     * @param service The stoppable service which will be shutdown before this component is shutdown.
     */
    public void registerChildService(ApplicationService service) {
        if (service != null && !services.contains(service)) {
            services.add(service);
        }
    }
}
