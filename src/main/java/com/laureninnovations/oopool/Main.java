package com.laureninnovations.oopool;

import com.sun.star.bridge.XBridge;
import com.sun.star.bridge.XBridgeFactory;
import com.sun.star.comp.helper.Bootstrap;
import com.sun.star.connection.XAcceptor;
import com.sun.star.connection.XConnection;
import com.sun.star.connection.XConnectionBroadcaster;
import com.sun.star.connection.XConnector;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

    static private final Logger log = LoggerFactory.getLogger(Main.class);

    static private final String ACCEPTOR_CLASSNAME = "com.sun.star.connection.Acceptor";
    static private final String BRIDGE_FACTORY_CLASSNAME = "com.sun.star.bridge.BridgeFactory";
    static private final String CONNECTOR_CLASSNAME = "com.sun.star.connection.Connector";
    static private final String ACCEPTOR_CONFIG = "socket,host=localhost,port=8200";

    static public void main(String[] args) {
        try {
            XComponentContext context = Bootstrap.createInitialComponentContext(null);
            XMultiComponentFactory manager = context.getServiceManager();

            Object acceptorObj = manager.createInstanceWithContext(ACCEPTOR_CLASSNAME, context);
            XAcceptor acceptor = UnoRuntime.queryInterface(XAcceptor.class, acceptorObj);

            Object bridgeFactoryObj = manager.createInstanceWithContext(BRIDGE_FACTORY_CLASSNAME, context);
            XBridgeFactory bridgeFactory = UnoRuntime.queryInterface(XBridgeFactory.class, bridgeFactoryObj);

            do {
                XConnection connection = acceptor.accept(ACCEPTOR_CONFIG);
                if (connection != null) {
                    String connectionDesc = extractContactInfor(connection.getDescription());
                    if (log.isInfoEnabled()) {
                        log.info("Incoming request for a worker from " + connectionDesc);
                    }

                    XConnectionBroadcaster connectionBroadcaster = UnoRuntime.queryInterface(XConnectionBroadcaster.class, connection);
                    connectionBroadcaster.addStreamListener(new ConnectionListener());

                    Object poolConnectorObj = manager.createInstanceWithContext(CONNECTOR_CLASSNAME, context);
                    XConnector poolConnector = UnoRuntime.queryInterface(XConnector.class, poolConnectorObj);
                    XConnection poolConnection = poolConnector.connect("socket,host=localhost,port=8100");
                    XBridge poolBridge = bridgeFactory.createBridge("", "urp", poolConnection, null);
                    Object poolContextObj = poolBridge.getInstance("StarOffice.ComponentContext");
                    XComponentContext poolContext = UnoRuntime.queryInterface(XComponentContext.class, poolContextObj);

                    bridgeFactory.createBridge("", "urp", connection, new OfficeInstanceProvider(poolBridge));
                }
                if (log.isDebugEnabled()) {
                    log.debug("BRIDGED");
                }
            } while (true);
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error(e.getMessage(), e);
            }
        }
    }

    static String extractContactInfor(String value) {
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
}
