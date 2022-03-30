package io.github.githubAccountsApp;

import io.github.githubAccountsApp.accountsConsumer.AccountsConsumer;
import io.github.githubAccountsApp.commitsProducer.CommitsProducer;
import io.github.githubAccountsApp.githubClient.GithubClientService;
import io.github.githubAccountsApp.time.Interval;
import io.github.githubAccountsApp.time.TimeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.Objects;
import java.util.Properties;

class GithubAccountsApplication {
    private final AccountsConsumer consumer;
    private final CommitsProducer producer;
    private final GithubClientService githubService;

    private static volatile boolean stopPipelineFlag = false;
    private static final Logger logger = LoggerFactory.getLogger(GithubAccountsApplication.class);

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
                logger.error("Failed to load 'config.properties' file!");
                System.exit(-1);
            }

            properties.load(input); // load a properties file from class path, inside static method

        } catch (IOException ex) {
            logger.error("IOException while getting application properties!");
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
                logger.warn("InterruptedException occurred while shutting down!");
            }
        }));
    }

    public void runPipeline() {
        consumer
            .poll()
            .flatMap(account ->
                    githubService.getUserCommits(account.getUser(), TimeService.calculateInterval(Objects.requireNonNull(Interval.valueOfLabel(account.getInterval()))))
            )
            .subscribe(producer::push);
    }

    private void closeApp() {
        logger.info("Ending application...");
        consumer.close();
        producer.close();
    }

    public static void main(String[] args) {
        logger.info("Starting application...");

        final Properties appConfig = GithubAccountsApplication.getAppProperties();
        final GithubAccountsApplication app = new GithubAccountsApplication(appConfig);

        try {
            while(!stopPipelineFlag) app.runPipeline();
        } finally {
            app.closeApp();
        }
    }
}