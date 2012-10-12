package com.laureninnovations.oopool.admin.protocol;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.*;

public class AdminControlProtocol implements IControlProtocol {
    static private final Logger log = LoggerFactory.getLogger(AdminControlProtocol.class);

    private DataOutputStream out;
    private DataInputStream in;
    private byte[] buf = new byte[4096];
    private ByteArrayOutputStream baos = new ByteArrayOutputStream();

    @Autowired
    private Gson gson;

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

    synchronized public void write(Message message) throws IOException {
        byte[] data = serializeMessage(message);
        ((DataOutputStream)getOutputStream()).writeInt(data.length);
        getOutputStream().write(data);
        getOutputStream().flush();
    }

    protected byte[] serializeMessage(Message message) throws UnsupportedEncodingException {
        return getGson().toJson(message).getBytes("UTF-8");
    }

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
