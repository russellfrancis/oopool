package com.laureninnovations.oopool;

import com.sun.star.bridge.XBridge;
import com.sun.star.bridge.XInstanceProvider;
import com.sun.star.lib.uno.helper.ComponentBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OfficeInstanceProvider extends ComponentBase implements XInstanceProvider {

    static private final Logger log = LoggerFactory.getLogger(OfficeInstanceProvider.class);

    private XBridge bridge;

    public OfficeInstanceProvider(XBridge bridge) {
        this.bridge = bridge;
    }

    public Object getInstance(String name) {
        log.info("resolving name: " + name);
        Object o = bridge.getInstance(name);
        log.info("value = " + o);
        return o;
    }
}
