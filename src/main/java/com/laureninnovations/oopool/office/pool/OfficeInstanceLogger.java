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
package com.laureninnovations.oopool.office.pool;

import com.laureninnovations.util.ApplicationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Russell Francis (russell.francis@metro-six.com)
 */
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
