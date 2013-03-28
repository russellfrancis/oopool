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
package com.laureninnovations.oopool.config;

import java.io.File;
import java.io.IOException;

/**
 * @author Russell Francis (russell.francis@metro-six.com)
 */
public interface Configuration {
    /**
     * Load the properties, concrete implementations may set any state they need prior to calling this method.
     *
     * @throws IOException If there is an error loading the configuration.
     */
    public void init() throws IOException;

    /**
     * Retrieve the File instance representing the base directory where open office is installed.
     *
     * @return The file path representing the base directory where open office / libre office is installed.
     */
    public File getOfficeBaseDirectory();

    /**
     * Retrieve the file instance representing the base directory where user instance data is stored.
     *
     * @return The file instance representing the base directory where user instance data is stored.
     */
    public File getOfficeBaseUserDirectory();

    /**
     * Retrieve the port which we should listen for incoming open office connections on.
     *
     * @return The port which should be used to listen for incoming connections on.
     */
    public int getPoolPort();

    /**
     * Retrieve the port which we should listen for incoming admin connections on.
     *
     * @return The port which should be used to listen for admin connections on.
     */
    public int getPoolAdminPort();

    /**
     * The ooPool manages a pool of worker processes, each process will listen on a port within a contiguous block
     * of ports starting with this port.  For example if this is set to 8200 and there are 5 workers, they will listen
     * on 8200, 8201, 8202, 8203, 8204 respectively.
     *
     * @return The first worker port within the contiguous block.
     */
    public int getFirstWorkerPort();

    /**
     * The maximum number of open office instances to have running simultaineously at any given time.
     *
     * @return The maximum number of open office instances to have running simultaneously at any given time.
     */
    public int getMaxPoolSize();

    public int getInstanceMaxIdleTime();

    public int getInstanceMaxJobs();
}
