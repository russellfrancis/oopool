package com.laureninnovations.oopool;

import com.laureninnovations.oopool.admin.AdminServer;
import com.laureninnovations.oopool.office.OfficeServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.concurrent.Callable;

public class Main implements Callable {

    static private final Logger log = LoggerFactory.getLogger(Main.class);

    static public void main(String[] args) {
        try {
            ApplicationContext applicationContext = new ClassPathXmlApplicationContext("/oopool-beans.xml");
            Main main = applicationContext.getBean(Main.class);
            main.call();
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

    public Object call() throws Exception {
        // startup our services ...
        adminServer.start();
        officeServer.start();

        // wait until we are terminated through the admin interface.
        adminServer.join();

        // trigger shutdown of the office server.
        officeServer.shutdown();
        officeServer.join();

        return null;
    }
}
