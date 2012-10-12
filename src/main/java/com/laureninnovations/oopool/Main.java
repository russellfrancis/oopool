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
/*
    private Configuration configuration;
    private OfficePool officeManager;
    private RequestListenerThread acceptorThread;

    public void init() throws Exception {
        // initialize our configuration.
        configuration = new PropertiesConfiguration();
        configuration.load();

        // create our office instance manager.
        officeManager = new OfficePool();
        officeManager.setConfiguration(configuration);
        officeManager.init();

        // create a thread which listens for incoming requests and dispatches to workers.
        acceptorThread = new RequestListenerThread();
        acceptorThread.setConfiguration(configuration);
        acceptorThread.setOfficeManager(officeManager);
    }

    public void destroy() {
        if (officeManager != null) {
            officeManager.destroy();
        }
    }
*/
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


//        RequestListenerThread requestListenerThread = applicationContext.getBean(RequestListenerThread.class);
//        AdminListenerThread adminListenerThread = applicationContext.getBean(AdminListenerThread.class);
//                              applicationContext.get

//        acceptorThread.start();
//        log.info("Waiting 120 seconds.");
//        Thread.sleep(120000);
//        log.info("Requesting shutdown.");
//        acceptorThread.waitForShutdown();
//        log.info("shutdown complete.");

//        OfficeInstance officeInstance = officeManager.newInstance();
//        log.info("Starting Instance");
//        officeInstance.start();

//        Thread.sleep(30000);
//        log.info("Stopping Instance");
//        officeInstance.stop();
        return null;
    }
}
