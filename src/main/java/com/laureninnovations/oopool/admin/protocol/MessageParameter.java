package com.laureninnovations.oopool.admin.protocol;

import com.google.gson.annotations.Expose;

import java.io.Serializable;

/**
 * Represents a message parameter which can be associated with a message and transported over the admin control
 * interface.
 *
 * @author Russell Francis (russell.francis@metro-six.com)
 */
public class MessageParameter implements Serializable {

    private static final long serialVersionUID = 0L;

    @Expose
    private String key;
    @Expose
    private Object value;

    /**
     * Provide a no-arg constructor for easy instantiation by Google-GSON.
     */
    private MessageParameter() {
    }

    public MessageParameter(final String key, final Object value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public Object getValue() {
        return value;
    }
}
