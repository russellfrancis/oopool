package com.laureninnovations.oopool.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesConfiguration implements Configuration {

    private Properties properties = new Properties();

    public void init() throws IOException {
        InputStream ins = PropertiesConfiguration.class.getResourceAsStream("/config.properties");
        try {
            properties.load(ins);
        } finally {
            if (ins != null) {
                ins.close();
            }
        }
    }

    public void destroy() {
    }

    public File getOfficeBaseDirectory() {
        return new File(properties.getProperty("libreoffice.base.dir"));
    }

    public File getOfficeBaseUserDirectory() {
        return new File(properties.getProperty("libreoffice.instance.dir"));
    }

    public int getPoolPort() {
        return 8100;
    }

    public int getPoolAdminPort() {
        return 8099;
    }

    public int getFirstWorkerPort() {
        return 8101;
    }

    public int getMinPoolSize() {
        return 0;
    }

    public int getMaxPoolSize() {
        return 5;
    }
}
