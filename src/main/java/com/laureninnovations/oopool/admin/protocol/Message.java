package com.laureninnovations.oopool.admin.protocol;

import com.google.gson.annotations.Expose;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a message which can be transported across the admin control interface.
 *
 * @author Russell Francis (russell.francis@metro-six.com)
 */
public class Message implements Serializable {
    static private final long serialVersionUID = 0L;

    @Expose
    private String action;
    @Expose
    private List<MessageParameter> parameters;

    /**
     * Provide a no-arg constructor for easy instantiation by Google-GSON.
     */
    private Message() {}

    public Message(final String action) {
        setAction(action);
    }

    public String getAction() {
        return action;
    }

    private void setAction(String action) {
        this.action = action;
    }

    public void remove(String key) {
        if (parameters != null && key != null) {
            for (int i = 0; i < parameters.size(); ++i) {
                MessageParameter option = parameters.get(i);
                if (key.equals(option.getKey())) {
                    parameters.remove(i);
                }
            }
            if (parameters.isEmpty()) {
                parameters = null;
            }
        }
    }

    public Object get(String key) {
        Object result = null;
        if (parameters != null) {
            for (MessageParameter option : parameters) {
                if (key.equals(option.getKey())) {
                    result = option.getValue();
                    break;
                }
            }
        }
        return result;
    }

    public void set(String key, Object value) {
        add(new MessageParameter(key, value));
    }

    private void add(MessageParameter parameter) {
        remove(parameter.getKey());
        if (parameters == null) {
            parameters = new ArrayList<MessageParameter>();
        }
        parameters.add(parameter);
    }
}
