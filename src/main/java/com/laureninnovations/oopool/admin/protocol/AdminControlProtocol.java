package com.laureninnovations.oopool.admin.protocol;

import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.*;

/**
 * This defines a protocol which is spoken over the admin interface to control the oopool.  The format includes a 4 byte
 * header which is interpreted as a signed integer in network byte order and indicates the length of the rest of the
 * message.  The message itself is a JSON encoded object which must have an "action" field and may contain a "parameters"
 * map consisting of key/value pairs.
 *
 * @author Russell Francis (russell.francis@metro-six.com)
 */
public class AdminControlProtocol implements IControlProtocol {
    private DataOutputStream out;
    private DataInputStream in;
    private byte[] buf = new byte[4096];
    private ByteArrayOutputStream baos = new ByteArrayOutputStream();

    /**
     * The google Gson instance used to encoded and decode JSON strings.
     */
    @Autowired
    private Gson gson;

    /**
     * Read a message from the wire.
     *
     * @return The serialized message instance.
     * @throws IOException If there is an issue reading the message from the wire.
     */
    @Override
    synchronized public Message read() throws IOException {
        try {
            int totalRead = 0;
            int messageLength = ((DataInputStream)getInputStream()).readInt();
            while (totalRead < messageLength) {
                int count = getInputStream().read(buf, 0, Math.min(buf.length, (messageLength - totalRead)));
                if (count == -1) {
                    throw new IOException("Invalid message format expected " + messageLength + " bytes but only received " + totalRead + ".");
                }

                baos.write(buf, 0, count);
                totalRead += count;
            }
            baos.flush();

            return gson.fromJson(new String(baos.toByteArray(), "UTF-8"), Message.class);
        } finally {
            baos.reset();
        }
    }

    /**
     * Write a message to the wire.
     *
     * @param message This is the message instance which will be serialized and transported across the wire.
     * @throws IOException If there is an error writing the message across the wire.
     */
    synchronized public void write(Message message) throws IOException {
        byte[] data = serializeMessage(message);
        ((DataOutputStream)getOutputStream()).writeInt(data.length);
        getOutputStream().write(data);
        getOutputStream().flush();
    }

    /**
     * Serialize a Message instance into a byte stream of JSON text.
     *
     * @param message The message we wish to serialize.
     * @return The UTF-8 encoded byte[] of the serialized message.
     * @throws UnsupportedEncodingException If there is a problem encoding the message as UTF-8.
     */
    protected byte[] serializeMessage(Message message) throws UnsupportedEncodingException {
        return getGson().toJson(message).getBytes("UTF-8");
    }

    /**
     * Deserialized a message string into a Message instance.
     *
     * @param data The raw bytes making up the serialized message data.
     * @return A Message instance created from that data.
     * @throws UnsupportedEncodingException If we were unable to interpret the provided bytes as a UTF-8 string.
     */
    protected Message deserializeMessage(byte[] data) throws UnsupportedEncodingException {
        return getGson().fromJson(new String(data, "UTF-8"), Message.class);
    }

    protected Gson getGson() {
        return gson;
    }

    protected void setGson(Gson gson) {
        this.gson = gson;
    }

    protected InputStream getInputStream() {
        return in;
    }

    public void setInputStream(InputStream in) {
        this.in = new DataInputStream(new BufferedInputStream(in));
    }

    protected OutputStream getOutputStream() {
        return out;
    }

    public void setOutputStream(OutputStream out) {
        this.out = new DataOutputStream(new BufferedOutputStream(out));
    }
}
