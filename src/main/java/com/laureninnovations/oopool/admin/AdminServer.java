package com.laureninnovations.oopool.admin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

public class AdminServer extends Thread {
    static private final Logger log = LoggerFactory.getLogger(AdminServer.class);

    private CountDownLatch shutdownLatch = new CountDownLatch(1);

    @Autowired
    private AdminController adminController;

    @Override
    public void run() {
        adminController.start();
        try {
            shutdownLatch.await();

            adminController.shutdown();
            adminController.join();
        } catch (IOException e) {
            if (log.isErrorEnabled()) {
                log.error(e.getMessage(), e);
            }
        } catch (InterruptedException e) {
            if (log.isDebugEnabled()) {
                log.debug("AdminServer interrupted");
            }
        }
    }

    public void shutdown() throws IOException {
        shutdownLatch.countDown();
    }
}
