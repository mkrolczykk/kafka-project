package com.github.mkrolczyk12.kafka.githubAccountsApp.time;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UnsupportedIntervalException extends Exception {
    private static final Logger LOG = LoggerFactory.getLogger(UnsupportedIntervalException.class);

    public UnsupportedIntervalException(final String message) {
        super(message);
    }

    public UnsupportedIntervalException(final Throwable cause) {
        super(cause);
    }

    public UnsupportedIntervalException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
