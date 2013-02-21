package com.laureninnovations.oopool.office.pool;

import com.laureninnovations.util.ApplicationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.concurrent.atomic.AtomicBoolean;

public class OfficeInstanceLogger extends Thread implements ApplicationService {
    static private final Logger log = LoggerFactory.getLogger(OfficeInstanceLogger.class);

    private OfficeInstance officeInstance;
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
                log.warn(OfficeInstanceReaper.class.getName() + " for " + getOfficeInstance().getName() + " was interrupted during shutdown.");
            }
        }
    }

    public void run() {
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        getOfficeInstance().getProcess().getInputStream(),
                        Charset.forName("UTF-8")));
        try {
            while (!shutdown.get()) {
                String line;
                while ( (line = reader.readLine()) != null ) {
                    if (log.isInfoEnabled()) {
                        log.info("PROCESS [" + getOfficeInstance().getName() + "]: " + line);
                    }
                }
            }
        }
        catch (IOException e) {
            if (log.isErrorEnabled()) {
                log.error("Error reading process output stream: " + e.getMessage());
            }
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                if (log.isErrorEnabled()) {
                    log.error("Unable to close process output stream: " + e.getMessage());
                }
            }
        }
    }

    public OfficeInstance getOfficeInstance() {
        return officeInstance;
    }

    public void setOfficeInstance(OfficeInstance officeInstance) {
        this.officeInstance = officeInstance;
    }
}
