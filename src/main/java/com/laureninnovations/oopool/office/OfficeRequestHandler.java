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
