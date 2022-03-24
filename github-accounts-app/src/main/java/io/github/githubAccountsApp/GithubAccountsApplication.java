package io.github.githubAccountsApp;

import io.github.githubAccountsApp.accountsConsumer.AccountsConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.Properties;

class GithubAccountsApplication {
    private static final Logger logger = LoggerFactory.getLogger(GithubAccountsApplication.class);
    private final Properties appConfig;
    private final AccountsConsumer consumer;

    GithubAccountsApplication(final Properties props) {
        this.appConfig = props;
        this.consumer = new AccountsConsumer(appConfig.getProperty("kafka.bootstrap.servers"), appConfig.getProperty("github.accounts.group.id"));

        consumer.subscribe(appConfig.getProperty("github.accounts.topic"));
    }

    private static Properties getAppProperties() {
        Properties properties = new Properties();

        try {
            InputStream input = GithubAccountsApplication.class.getClassLoader().getResourceAsStream("config.properties");

            if (input == null) {
                logger.error("Failed to load 'config.properties' file!");
                System.exit(-1);
            }

            // load a properties file from class path, inside static method
            properties.load(input);

        } catch (IOException ex) {
            logger.error("IOException while getting application properties!");
            System.exit(-2);
        }
        return properties;
    }

    public void startApp() {
        consumer.poll(Duration.ofMillis(Long.parseLong(appConfig.getProperty("poll.duration.millis.time"))));
    }

    private void closeApp() {
        logger.info("Ending application...");
        consumer.close();
    }

    public static void main(String[] args) {
        logger.info("Starting GithubAccounts application...");

        Properties appConfig = GithubAccountsApplication.getAppProperties();
        GithubAccountsApplication app = new GithubAccountsApplication(appConfig);

        try {
            app.startApp();
        } finally {
            app.closeApp();
        }
    }
}