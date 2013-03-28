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
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * @author Russell Francis (russell.francis@metro-six.com)
 */
public class PropertiesConfiguration implements Configuration {

    private Properties properties = new Properties();

    public void init() throws IOException {
        FileInputStream ins = new FileInputStream(System.getProperty("oopool.config_file"));
        try {
            properties.load(ins);
        } finally {
            ins.close();
        }
    }

    public File getOfficeBaseDirectory() {
        return new File(properties.getProperty("libreoffice.base.dir"));
    }

    public File getOfficeBaseUserDirectory() {
        return new File(properties.getProperty("libreoffice.instance.dir"));
    }

    public int getPoolPort() {
        return Integer.parseInt(properties.getProperty("oopool.pool_port"));
    }

    public int getPoolAdminPort() {
        return Integer.parseInt(properties.getProperty("oopool.admin_port"));
    }

    public int getFirstWorkerPort() {
        return Integer.parseInt(properties.getProperty("oopool.first_worker_port"));
    }

    public int getMaxPoolSize() {
        return Integer.parseInt(properties.getProperty("oopool.max_pool_size"));
    }

    public int getInstanceMaxIdleTime() {
        return Integer.parseInt(properties.getProperty("oopool.instance.max_idle_time"));
    }

    public int getInstanceMaxJobs() {
        return Integer.parseInt(properties.getProperty("oopool.instance.max_jobs"));
    }
}
