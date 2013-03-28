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
        if (log.isTraceEnabled()) {
            log.trace("connection error:  " + id + ": " + o);
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
