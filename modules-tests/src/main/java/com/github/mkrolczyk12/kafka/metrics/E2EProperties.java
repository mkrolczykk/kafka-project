package com.github.mkrolczyk12.kafka.metrics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class E2EProperties {
    private static final Logger LOG = LoggerFactory.getLogger(E2EProperties.class);

    private E2EProperties() {}

    public static Properties getModuleProperties() {
        Properties properties = new Properties();

        try {
            InputStream input = E2EProperties.class.getClassLoader().getResourceAsStream("config.test.properties");

            if (input == null) {
                LOG.error("Failed to load 'config.test.properties' file!");
                System.exit(-1);
            }

            properties.load(input); // load a properties file from class path, inside static method

        } catch (IOException ex) {
            LOG.error("IOException while getting application properties!");
            System.exit(-2);
        }
        return properties;
    }
}
