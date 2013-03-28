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
