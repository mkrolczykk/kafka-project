package com.github.mkrolczyk12.kafka.githubAccountsApp;

import com.github.mkrolczyk12.kafka.githubAccountsApp.time.Interval;
import com.github.mkrolczyk12.kafka.githubAccountsApp.time.TimeService;
import com.github.mkrolczyk12.kafka.githubAccountsApp.accountsConsumer.AccountsConsumer;
import com.github.mkrolczyk12.kafka.githubAccountsApp.commitsProducer.CommitsProducer;
import com.github.mkrolczyk12.kafka.githubAccountsApp.githubClient.GithubClientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.Properties;

class GithubAccountsApplication {
    private final AccountsConsumer consumer;
    private final CommitsProducer producer;
    private final GithubClientService githubService;

    private static volatile boolean stopPipelineFlag = false;
    private static final Logger LOG = LoggerFactory.getLogger(GithubAccountsApplication.class);

    GithubAccountsApplication(final Properties props) {
        this.consumer = new AccountsConsumer(
                props.getProperty("kafka.bootstrap.servers"),
                props.getProperty("github.accounts.group.id"),
                Duration.ofMillis(Long.parseLong(props.getProperty("poll.duration.millis.time")))
        );
        this.producer = new CommitsProducer(
                props.getProperty("kafka.bootstrap.servers"),
                props.getProperty("github.commits.topic")
        );
        this.githubService = new GithubClientService(props.getProperty("github.api.base.url"));

        consumer.subscribe(props.getProperty("github.accounts.topic"));

        addPipelineShutdownHook();
    }

    private static Properties getAppProperties() {
        Properties properties = new Properties();

        try {
            InputStream input = GithubAccountsApplication.class.getClassLoader().getResourceAsStream("config.properties");

            if (input == null) {
                LOG.error("Failed to load 'config.properties' file!");
                System.exit(-1);
            }

            properties.load(input); // load a properties file from class path, inside static method

        } catch (IOException ex) {
            LOG.error("IOException while getting application properties!");
            System.exit(-2);
        }
        return properties;
    }

    private void addPipelineShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            stopPipelineFlag = true;
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                LOG.warn("InterruptedException occurred while shutting down!");
            }
        }));
    }

    public void runPipeline() {
        consumer
            .poll()
            .flatMap(account ->
                    githubService.getUserCommits(account.getUser(), TimeService.calculateInterval(Interval.valueOfLabel(account.getInterval())))
            )
            .subscribe(producer::push);
    }

    private void closeApp() {
        LOG.info("Ending application...");
        consumer.close();
        producer.close();
    }

    public static void main(String[] args) {
        LOG.info("Starting GithubAccounts application...");

        final Properties appConfig = GithubAccountsApplication.getAppProperties();
        final GithubAccountsApplication app = new GithubAccountsApplication(appConfig);

        try {
            while(!stopPipelineFlag) app.runPipeline();
        } finally {
            app.closeApp();
        }
    }
}