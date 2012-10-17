package com.laureninnovations.oopool.office.pool;

import com.laureninnovations.oopool.config.Configuration;
import com.laureninnovations.util.ApplicationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This thread monitors the state of an office instance and requests it shutdown after a period of inactivity.
 *
 * @author Russell Francis (russell.francis@metro-six.com)
 */
public class OfficeInstanceReaper extends Thread implements ApplicationService {
    static private final Logger log = LoggerFactory.getLogger(OfficeInstanceReaper.class);

    private OfficeInstance officeInstance;

    @Autowired
    private Configuration configuration;

    private AtomicBoolean shutdown = new AtomicBoolean(false);

    public void startup() {
        this.start();
    }

    public void shutdown() {
        shutdown.set(true);
        this.interrupt();
    }

    public void awaitShutdown() {
        try {
            this.join();
        } catch (InterruptedException e) {
            if (log.isWarnEnabled()) {
                log.warn(OfficeInstanceReaper.class.getName() + " for " + officeInstance.getName() + " was interrupted during shutdown.");
            }
        }
    }

    public void run() {
        while (!shutdown.get()) {
            try {
                Thread.sleep(30000);
                if (isMaxJobsExceeded() || isIdleTimeExceeded()) {
                    officeInstance.stop();
                }
            } catch (InterruptedException e) {
                if (log.isTraceEnabled()) {
                    log.trace(OfficeInstanceReaper.class.getName() + " for " + officeInstance.getName() + " was interrupted.");
                }
            }
        }
    }

    protected boolean isIdleTimeExceeded() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.SECOND, -1 * configuration.getInstanceMaxIdleTime());
        Date threshold = cal.getTime();
        Date idleSince = officeInstance.getStatistics().getIdleSince();
        return idleSince != null && idleSince.before(threshold);
    }

    protected boolean isMaxJobsExceeded() {
        return officeInstance.getStatistics().getJobsProcessed() >= configuration.getInstanceMaxJobs();
    }

    public OfficeInstance getOfficeInstance() {
        return officeInstance;
    }

    public void setOfficeInstance(OfficeInstance officeInstance) {
        this.officeInstance = officeInstance;
    }

    protected Configuration getConfiguration() {
        return configuration;
    }

    protected void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }
}
