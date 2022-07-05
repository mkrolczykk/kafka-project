package com.github.mkrolczyk12.kafka.metrics;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mkrolczyk12.kafka.githubAccountsApp.GithubAccountsApplication;
import com.github.mkrolczyk12.kafka.metrics.streams.AbstractKafkaStream;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.test.rule.EmbeddedKafkaRule;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class E2EMetricsSpec {
    private static final Logger LOG = LoggerFactory.getLogger(E2EMetricsSpec.class);

    private final static Properties props = E2EProperties.getModuleProperties();

    private final static String GITHUB_ACCOUNTS_TOPIC = props.getProperty("github.accounts");

    private final static String GITHUB_COMMITS_TOPIC = props.getProperty("github.commits");

    private final static String GITHUB_METRICS_TOP_K_CONTR_BY_COMMITS_TOPIC = props.getProperty("github.metrics.top.k.contr.by.commits");

    private final static String GITHUB_METRICS_TOTAL_NUMBER_OF_COMMITS_TOPIC = props.getProperty("github.metrics.total.number.of.commits");

    private final static String GITHUB_METRICS_TOTAL_NUMBER_OF_COMMITTERS_TOPIC = props.getProperty("github.metrics.total.number.of.committers");

    private final static String GITHUB_METRICS_TOTAL_LANGUAGE_TOPIC = props.getProperty("github.metrics.total.language");

    private static KafkaProducer<String, String> accountsProducer;

    private static KafkaConsumer<String, String> commitsConsumer;

    private static KafkaConsumer<String, String> totalCommitsMetricConsumer;

    private static KafkaConsumer<String, String> committersNumberMetricConsumer;

    private static KafkaConsumer<String, String> topKCommittersMetricConsumer;

    private static KafkaConsumer<String, String> usedLanguagesMetricConsumer;

    private static GithubAccountsApplication githubAccountsApp;

    private static final List<AbstractKafkaStream> streams = new ArrayList<>();

    private static ObjectMapper objectMapper;

    private static E2EApiMockServer apiMockServer;

    private static E2EMetricsBuilder e2EMetricsBuilder;

    @ClassRule
    public static EmbeddedKafkaRule embeddedKafka = new EmbeddedKafkaRule(
            3,
            true,
            GITHUB_ACCOUNTS_TOPIC,
            GITHUB_COMMITS_TOPIC,
            GITHUB_METRICS_TOP_K_CONTR_BY_COMMITS_TOPIC,
            GITHUB_METRICS_TOTAL_NUMBER_OF_COMMITS_TOPIC,
            GITHUB_METRICS_TOTAL_NUMBER_OF_COMMITTERS_TOPIC,
            GITHUB_METRICS_TOTAL_LANGUAGE_TOPIC
    );

    @BeforeClass
    public static void setUp() {
        //TODO
    }

}
