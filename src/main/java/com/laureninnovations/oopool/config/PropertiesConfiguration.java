package com.laureninnovations.oopool.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

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
