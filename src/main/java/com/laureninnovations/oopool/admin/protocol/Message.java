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
