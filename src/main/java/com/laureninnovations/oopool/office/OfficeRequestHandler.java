package com.laureninnovations.oopool.office;

import com.laureninnovations.oopool.office.pool.OfficePool;
import com.sun.star.connection.XConnection;
import com.sun.star.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.Callable;

public class OfficeRequestHandler implements Callable {

    private OfficeController officeController;
    private XConnection connection;

    @Autowired
    private OfficePool officePool;

    public void init() {
    }

    public void destroy() {
    }

    public Object call() throws IOException {
        // spin up an instance of open office.
        //OfficeInstance instance = officePool.getOfficeInstance();
//        try {
            // create our server side connection
            // bridge the two connections.
            // return the open office instance to the pool when finished.
//        } finally {
//            instance.close();
//        }
        return null;
    }

    public XConnection getConnection() {
        return connection;
    }

    public void setConnection(XConnection connection) {
        this.connection = connection;
    }

    public OfficeController getOfficeController() {
        return officeController;
    }

    public void setOfficeController(OfficeController officeController) {
        this.officeController = officeController;
    }
}
