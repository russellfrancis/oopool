package com.laureninnovations.oopool;

import com.sun.star.bridge.XBridge;
import com.sun.star.bridge.XInstanceProvider;
import com.sun.star.lib.uno.helper.ComponentBase;

public class OfficeInstanceProvider extends ComponentBase implements XInstanceProvider {

    private XBridge bridge;

    public OfficeInstanceProvider(XBridge bridge) {
        this.bridge = bridge;
    }

    public Object getInstance(String name) {
        return bridge.getInstance(name);
    }
}
