package com.laureninnovations.oopool;

import com.laureninnovations.oopool.office.pool.ConnectionListener;
import junit.framework.Assert;
import org.junit.Test;

public class ConnectionListenerTest {
    @Test
    public void testStarted() {
        ConnectionListener listener = new ConnectionListener("");
        listener.started();
        Assert.assertTrue(true);
    }
}
