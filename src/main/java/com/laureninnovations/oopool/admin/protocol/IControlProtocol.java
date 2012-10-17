package com.laureninnovations.oopool.admin.protocol;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Implements a control protocol for the admin interface, different protocols may provided different characteristics
 * such as encryption or compression as part of the message transport.
 *
 * @author Russell Francis (russell.francis@metro-six.com)
 */
public interface IControlProtocol {
    public void setInputStream(InputStream in);
    public void setOutputStream(OutputStream out);
    public Message read() throws IOException;
    public void write(Message message) throws IOException;
}
