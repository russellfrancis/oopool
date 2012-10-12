package com.laureninnovations.oopool.admin.protocol;

import java.io.Serializable;

public class MessageParameter implements Serializable {

    private static final long serialVersionUID = 0L;

    private String key;
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
