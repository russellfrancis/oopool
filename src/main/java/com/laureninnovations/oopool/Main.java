package com.laureninnovations.oopool;

import com.laureninnovations.oopool.admin.AdminServer;
import com.laureninnovations.oopool.office.OfficeServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Main implements Runnable {

    static private final Logger log = LoggerFactory.getLogger(Main.class);

    static public void main(String[] args) {
        try {
            // Since we are a daemon we need to close the input, output and error streams to let the spawning process
            // cleanly disconnect from ourselves.
            System.err.close();
            System.out.close();
            System.in.close();

            ApplicationContext applicationContext = new ClassPathXmlApplicationContext("/oopool-beans.xml");
            Main main = applicationContext.getBean(Main.class);
            main.run();
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error(e.getMessage(), e);
            }
        }
    }

    @Autowired
    private AdminServer adminServer;

    @Autowired
    private OfficeServer officeServer;

    public void run() {
        try {
            adminServer.registerChildService(officeServer);
            adminServer.startup();
            adminServer.awaitShutdown();
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error(e.getMessage(), e);
            }
        }
    }
}
