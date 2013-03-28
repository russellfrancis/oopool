/*-
 * Copyright (c) 2013, Lauren Innovations
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * 4. Neither the name of the Lauren Innovations nor the names of its contributors
 * may be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
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
