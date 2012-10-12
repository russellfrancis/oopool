package com.laureninnovations.oopool;

import com.sun.star.io.XStreamListener;
import com.sun.star.lang.EventObject;
import com.sun.star.lib.uno.helper.ComponentBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConnectionListener extends ComponentBase implements XStreamListener {

    static private final Logger log = LoggerFactory.getLogger(ConnectionListener.class);


    private String id;

    public ConnectionListener(String id) {
        this.id = id;
    }

    public void started() {
        if (log.isDebugEnabled()) {
            log.debug("connection started " + id);
        }
    }

    public void closed() {
        if (log.isDebugEnabled()) {
            log.debug("connection closed " + id);
        }
    }

    public void terminated() {
        if (log.isDebugEnabled()) {
            log.debug("connection terminated " + id);
        }
    }

    public void error(Object o) {
        if (log.isDebugEnabled()) {
            log.debug("connection error:  " + id + ": " + o);
        }
    }

    public void disposing(EventObject source) {
        if (log.isDebugEnabled()) {
            log.debug("connection disposing " + id);
        }
    }
}
