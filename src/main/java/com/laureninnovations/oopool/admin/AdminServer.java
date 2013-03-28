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
