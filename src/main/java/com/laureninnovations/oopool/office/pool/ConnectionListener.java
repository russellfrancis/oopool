package com.laureninnovations.oopool.office.pool;

import com.sun.star.io.XStreamListener;
import com.sun.star.lang.EventObject;
import com.sun.star.lib.uno.helper.ComponentBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;

/**
 * This listens for connection events on OpenOffice connections and is used to close resources after an action completes
 * as well as to update statistics for an office instance.
 *
 * @author Russell Francis (russell.francis@metro-six.com)
 */
public class ConnectionListener extends ComponentBase implements XStreamListener {

    static private final Logger log = LoggerFactory.getLogger(ConnectionListener.class);

    private String id;
    private CountDownLatch latch = new CountDownLatch(1);

    public ConnectionListener(String id) {
        this.id = id;
    }

    public void started() {
        if (log.isTraceEnabled()) {
            log.trace("connection started " + id);
        }
    }

    public void closed() {
        if (log.isTraceEnabled()) {
            log.trace("connection closed " + id);
        }
        latch.countDown();
    }

    public void terminated() {
        if (log.isTraceEnabled()) {
            log.trace("connection terminated " + id);
        }
        latch.countDown();
    }

    public void error(Object o) {
        if (log.isErrorEnabled()) {
            log.error("connection error:  " + id + ": " + o);
        }
        latch.countDown();
    }

    public void disposing(EventObject source) {
        if (log.isTraceEnabled()) {
            log.trace("connection disposing " + id);
        }
    }

    public void waitForCompletion() throws InterruptedException {
        latch.await();
    }
}
