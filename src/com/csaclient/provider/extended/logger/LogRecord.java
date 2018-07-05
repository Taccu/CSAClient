/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.csaclient.provider.extended.logger;

import java.util.Date;

/**
 *
 * @author bho
 */
public class LogRecord {
    private final Date   timestamp;
    private final Level  level;
    private final String context;
    private final String message;

    public LogRecord(Level level, String context, String message) {
        this.timestamp = new Date();
        this.level     = level;
        this.context   = context;
        this.message   = message;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public Level getLevel() {
        return level;
    }

    public String getContext() {
        return context;
    }

    public String getMessage() {
        return message;
    }
}
