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

import com.laureninnovations.oopool.admin.protocol.AdminControlProtocol;
import com.laureninnovations.oopool.admin.protocol.Message;
import com.laureninnovations.oopool.office.pool.OfficePool;
import com.laureninnovations.oopool.office.pool.OfficePoolStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * This class will carry on a conversation with a client over the admin connection port.  It reads and interprets requests
 * and sends responses until the client closes the connection.
 *
 * @author Russell Francis (russell.francis@metro-six.com)
 */
public class AdminRequestHandler implements Runnable {
    static private final Logger log = LoggerFactory.getLogger(AdminRequestHandler.class);

    private Socket socket;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private AdminServer adminServer;

    @Autowired
    private OfficePool officePool;

    @Override
    public void run() {
        try {
            InputStream ins = getSocket().getInputStream();
            try {
                OutputStream outs = getSocket().getOutputStream();
                try {
                    AdminControlProtocol protocol = applicationContext.getBean(AdminControlProtocol.class);
                    protocol.setInputStream(ins);
                    protocol.setOutputStream(outs);
                    boolean quit = false;
                    do {
                        Message command = protocol.read();
                        Message response = null;
                        if (command != null) {
                            if ("EXIT".equals(command.getAction())) {
                                response = new Message("OK");
                                quit = true;
                            } else if ("SHUTDOWN".equals(command.getAction())) {
                                if (log.isInfoEnabled()) {
                                    log.info("ADMIN SHUTDOWN REQUESTED BY " + ((InetSocketAddress)(getSocket().getRemoteSocketAddress())).getAddress().getHostAddress());
                                }
                                adminServer.shutdown();
                                response = new Message("OK");
                                quit = true;
                            } else if ("STATUS".equals(command.getAction())) {
                                response = new Message("OK");
                                // for each office instance write out the stats.
                                OfficePoolStatistics statistics = officePool.getOfficePoolStatistics();
                                response.set("statistics", statistics);
                            }
                        }
                        if (response == null) {
                            response = new Message("UNRECOGNIZED COMMAND '" + command + "'");
                        }
                        protocol.write(response);
                    } while (!quit);
                } finally {
                    outs.close();
                }
            } finally {
                ins.close();
            }
        }
        catch (EOFException e) {
            if (log.isTraceEnabled()) {
                log.trace(e.getMessage(), e);
            }
        } catch (IOException e) {
            if (log.isErrorEnabled()) {
                log.error(e.getMessage(), e);
            }
        }
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }
}
