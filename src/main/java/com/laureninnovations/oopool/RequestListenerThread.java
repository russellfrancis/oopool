package com.laureninnovations.oopool;

import com.laureninnovations.oopool.config.Configuration;
import com.laureninnovations.oopool.office.pool.OfficeInstance;
import com.laureninnovations.oopool.office.pool.OfficePool;
import com.sun.star.bridge.XBridge;
import com.sun.star.bridge.XBridgeFactory;
import com.sun.star.comp.helper.Bootstrap;
import com.sun.star.connection.*;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This thread accepts incoming requests and delegates jobs to be processed.
 */
public class RequestListenerThread extends Thread {

    static private final Logger log = LoggerFactory.getLogger(RequestListenerThread.class);

    static private final String ACCEPTOR_CLASSNAME = "com.sun.star.connection.Acceptor";
    static private final String BRIDGE_FACTORY_CLASSNAME = "com.sun.star.bridge.BridgeFactory";
    static private final String CONNECTOR_CLASSNAME = "com.sun.star.connection.Connector";

    private boolean shutdown = false;
    private Configuration configuration;
    private OfficePool officeManager;
    private XAcceptor acceptor;
/*
    @Override
    public void run() {
        try {
            OfficeInstance instance = officeManager.newInstance();
            instance.start();
            Thread.sleep(3000);

            XComponentContext context = Bootstrap.createInitialComponentContext(null);
            XMultiComponentFactory manager = context.getServiceManager();

            Object acceptorObj = manager.createInstanceWithContext(ACCEPTOR_CLASSNAME, context);
            acceptor = UnoRuntime.queryInterface(XAcceptor.class, acceptorObj);

            Object bridgeFactoryObj = manager.createInstanceWithContext(BRIDGE_FACTORY_CLASSNAME, context);
            XBridgeFactory bridgeFactory = UnoRuntime.queryInterface(XBridgeFactory.class, bridgeFactoryObj);

            while (!shutdown) {
                Object poolConnectorObj = manager.createInstanceWithContext(CONNECTOR_CLASSNAME, context);
                XConnector poolConnector = UnoRuntime.queryInterface(XConnector.class, poolConnectorObj);
                XConnection poolConnection = poolConnector.connect("socket,host=0,port=" + instance.getPort());
                XBridge poolBridge = bridgeFactory.createBridge("", "urp", poolConnection , null);
                Object poolContextObj = poolBridge.getInstance("StarOffice.ComponentContext");
                XComponentContext poolContext = UnoRuntime.queryInterface(XComponentContext.class, poolContextObj);
                XDesktop desktop = (XDesktop) poolContext.getServiceManager().createInstanceWithContext("com.sun.star.frame.Desktop", poolContext);
            }
        } catch (Exception e) {
            if (!shutdown) {
                if (log.isErrorEnabled()) {
                    log.error(e.getMessage(), e);
                }
            }
        }
    }*/

    @Override
    public void run() {
        try {
            OfficeInstance instance = officeManager.newInstance();
            instance.start();
            Thread.sleep(3000);

            XComponentContext context = Bootstrap.createInitialComponentContext(null);
            XMultiComponentFactory manager = context.getServiceManager();

            Object acceptorObj = manager.createInstanceWithContext(ACCEPTOR_CLASSNAME, context);
            acceptor = UnoRuntime.queryInterface(XAcceptor.class, acceptorObj);

            Object bridgeFactoryObj = manager.createInstanceWithContext(BRIDGE_FACTORY_CLASSNAME, context);
            XBridgeFactory bridgeFactory = UnoRuntime.queryInterface(XBridgeFactory.class, bridgeFactoryObj);

            while (!shutdown) {
                XConnection connection = acceptor.accept(getAcceptorConfig());

                Object poolConnectorObj = manager.createInstanceWithContext(CONNECTOR_CLASSNAME, context);
                XConnector poolConnector = UnoRuntime.queryInterface(XConnector.class, poolConnectorObj);

//                log.info("A " + instance.getPort());
                XConnection poolConnection = poolConnector.connect("socket,host=0,port=" + instance.getPort());
                //XConnection poolConnection = poolConnector.connect("pipe,name=daemon-instance-" + instance.getPort());

//                XBridge poolBridge = bridgeFactory.createBridge("", "urp", poolConnection , null);
//                Object poolContextObj = poolBridge.getInstance("StarOffice.ComponentContext");
//                XComponentContext poolContext = UnoRuntime.queryInterface(XComponentContext.class, poolContextObj);
//                com.sun.star.frame.Desktop desktop = poolContext.getServiceManager().createInstanceWithContext("com.sun.star.frame.Desktop", poolContext);


//                bridgeFactory.createBridge("", "urp", connection, new OfficeInstanceProvider(poolBridge));



                XConnectionBroadcaster connectionBroadcaster = UnoRuntime.queryInterface(XConnectionBroadcaster.class, connection);
                connectionBroadcaster.addStreamListener(new ConnectionListener("remote"));

                XConnectionBroadcaster connectionBroadcaster1 = UnoRuntime.queryInterface(XConnectionBroadcaster.class, poolConnection);
                connectionBroadcaster1.addStreamListener(new ConnectionListener("worker"));

                XBridge bridge = bridgeFactory.createBridge("", "urp", poolConnection, null);
                if (bridge == null) {
                    throw new NoConnectException("Cannot create bridge from bridge factory.");
                }

                Object contextObj = bridge.getInstance("StarOffice.ComponentContext");
                XComponentContext localContext = (XComponentContext) UnoRuntime.queryInterface(XComponentContext.class, contextObj);
                if (localContext == null) {
                    throw new NoConnectException("Cannot get instance of ComponentContext");
                }

                XBridge poolBridge = bridgeFactory.createBridge("", "urp", connection, new OfficeInstanceProvider(bridge));
            }
        } catch (Exception e) {
            if (!shutdown) {
                if (log.isErrorEnabled()) {
                    log.error(e.getMessage(), e);
                }
            }
        }
    }

    protected String getAcceptorConfig() {
        return "socket,host=0,port=" + getConfiguration().getPoolPort();
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    synchronized public void waitForShutdown() throws InterruptedException {
        shutdown = true;
        acceptor.stopAccepting();
        this.wait();
    }

    public OfficePool getOfficeManager() {
        return officeManager;
    }

    public void setOfficeManager(OfficePool officeManager) {
        this.officeManager = officeManager;
    }

/*
    protected String extractContactInfor(String value) {
        String[] 	list 	= value.split(",");
        String 		host 	= "";
        String		port 	= "";
        for (String str : list) {
            if (str.startsWith("peerHost")) {
                host = str.split("=")[1];
            } else if (str.startsWith("peerPort")) {
                port = str.split("=")[1];
            }
        }
        return host + ":" + port;
    }
*/
}
