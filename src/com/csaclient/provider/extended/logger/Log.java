/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.csaclient.provider.extended.logger;

import java.util.Collection;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

/**
 *
 * @author bho
 */
public class Log {
    private static final int MAX_LOG_ENTRIES = 2_000_000_000;

    private final BlockingDeque<LogRecord> log = new LinkedBlockingDeque<>(MAX_LOG_ENTRIES);

    public void drainTo(Collection<? super LogRecord> collection) {
        log.drainTo(collection);
    }

    public void offer(LogRecord record) {
        log.offer(record);
    }
}
