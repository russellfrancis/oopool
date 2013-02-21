package com.laureninnovations.oopool.office;

import com.laureninnovations.oopool.office.pool.OfficeInstance;
import com.laureninnovations.oopool.office.pool.OfficePool;
import com.sun.star.connection.XConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.Callable;

/**
 * This is the worker which is responsible for satisfying a request to the OpenOffice listening port.  The connection
 * is matched up and bridged with an open office instance from our pool which does the work.
 *
 * @author Russell Francis (russell.francis@metro-six.com)
 */
public class OfficeRequestHandler implements Callable {
    static private final Logger log = LoggerFactory.getLogger(OfficeRequestHandler.class);
    private XConnection connection;

    @Autowired
    private OfficePool officePool;

    public Object call() {
        try {
            OfficeInstance instance = officePool.acquireInstance();
            log.info("ACQUIRED INSTANCE " + instance.getName());
            try {
                try {
                    instance.start();
                    instance.awaitStartup();
                    instance.bridgeConnection(connection);
                } finally {
                    officePool.releaseInstance(instance);
                    if (log.isInfoEnabled()) {
                        log.info("RELEASED INSTANCE " + instance.getName());
                    }
                }
            } finally {
                connection.close();
                if (log.isInfoEnabled()) {
                    log.info("CLOSED XCONNECTION");
                }
            }
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error(e.getMessage(), e);
            }
        }
        return null;
    }

    public XConnection getConnection() {
        return connection;
    }

    public void setConnection(XConnection connection) {
        this.connection = connection;
    }
}
