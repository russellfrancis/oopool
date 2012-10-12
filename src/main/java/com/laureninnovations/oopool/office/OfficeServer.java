package com.laureninnovations.oopool.office;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

public class OfficeServer extends Thread {
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
        } catch (IOException e) {
            if (log.isErrorEnabled()) {
                log.error(e.getMessage(), e);
            }
        } catch (InterruptedException e) {
            if (log.isDebugEnabled()) {
                log.debug("OfficeServer interrupted");
            }
        }
    }

    public void shutdown() throws IOException {
        shutdownLatch.countDown();
    }
}
