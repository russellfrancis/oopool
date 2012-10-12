package com.laureninnovations.oopool.admin.protocol;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface IControlProtocol {
    public void setInputStream(InputStream in);
    public void setOutputStream(OutputStream out);
    public Message read() throws IOException;
    public void write(Message message) throws IOException;
}
