package com.github.mkrolczyk12.kafka.metrics;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.mkrolczyk12.kafka.githubAccountsApp.GithubAccountsApplication;
import com.github.mkrolczyk12.kafka.githubAccountsApp.accountsConsumer.projection.Account;
import com.github.mkrolczyk12.kafka.githubAccountsApp.commitsProducer.projection.KafkaCommitRecord;
import com.github.mkrolczyk12.kafka.metrics.streams.AbstractKafkaStream;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.*;
import org.junit.jupiter.api.DisplayName;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.junit.runner.RunWith;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.test.rule.EmbeddedKafkaRule;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@DirtiesContext
public class E2EMetricsIT {
    private static final Logger LOG = LoggerFactory.getLogger(E2EMetricsIT.class);

    private final static Properties props = E2EProperties.getModuleProperties();

    private final static String GITHUB_ACCOUNTS_TOPIC = props.getProperty("github.accounts");

    private final static String GITHUB_COMMITS_TOPIC = props.getProperty("github.commits");

    private final static String GITHUB_METRICS_TOP_K_CONTR_BY_COMMITS_TOPIC = props.getProperty("github.metrics.top.k.contr.by.commits");

    private final static String GITHUB_METRICS_TOTAL_NUMBER_OF_COMMITS_TOPIC = props.getProperty("github.metrics.total.number.of.commits");

    private final static String GITHUB_METRICS_TOTAL_NUMBER_OF_COMMITTERS_TOPIC = props.getProperty("github.metrics.total.number.of.committers");

    private final static String GITHUB_METRICS_TOTAL_LANGUAGE_TOPIC = props.getProperty("github.metrics.total.language");

    private static KafkaProducer<String, String> accountsProducer;

    private static KafkaConsumer<String, String> commitsConsumer;

    private static KafkaConsumer<String, String> totalCommitsConsumer;

    private static KafkaConsumer<String, String> totalCommittersNumberConsumer;

    private static KafkaConsumer<String, String> topKCommittersConsumer;

    private static KafkaConsumer<String, String> usedLanguagesConsumer;

    private static GithubAccountsApplication githubAccountsApp;

    private static List<AbstractKafkaStream> streamsApp = new ArrayList<>();

    private static E2EApiMockServer apiMockServer;

    private static ObjectMapper objectMapper;

    @ClassRule
    public static EmbeddedKafkaRule embeddedKafka =
        new EmbeddedKafkaRule(
            3,  // number of brokers
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
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        apiMockServer = new E2EApiMockServer(objectMapper);

        final String bootstrapServers = (String) KafkaTestUtils
            .consumerProps("sender", "false", embeddedKafka.getEmbeddedKafka())
            .get(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG);

        LOG.info("set up bootstrapServers: " + bootstrapServers);

        final E2EMetricsBuilder e2eMetricsBuilder = new E2EMetricsBuilder(bootstrapServers);

        accountsProducer = e2eMetricsBuilder.createAccountsProducer();

        githubAccountsApp = e2eMetricsBuilder.runGithubAccountsApp();
        streamsApp = e2eMetricsBuilder.runKafkaStreamsApp();

        commitsConsumer = e2eMetricsBuilder.createCommitsConsumer();
        totalCommitsConsumer = e2eMetricsBuilder.createTotalCommitsConsumer();
        totalCommittersNumberConsumer = e2eMetricsBuilder.createTotalCommittersNumberConsumer();
        topKCommittersConsumer = e2eMetricsBuilder.createTopKCommittersConsumer();
        usedLanguagesConsumer = e2eMetricsBuilder.createTotalCommitsPerLanguagesConsumer();
    }

    @AfterClass
    public static void tearDown() { // Clean up after testing
        accountsProducer.close();

        githubAccountsApp.closeApp();
        streamsApp.forEach(AbstractKafkaStream::close);
        streamsApp.forEach(AbstractKafkaStream::cleanUp);

        commitsConsumer.close();
        totalCommitsConsumer.close();
        totalCommittersNumberConsumer.close();
        topKCommittersConsumer.close();
        usedLanguagesConsumer.close();

        embeddedKafka.getEmbeddedKafka().destroy();
        apiMockServer.stopServer();
    }

    @Test
    @DisplayName("Test streams data processing correctness")
    public void end2endPipelineFromGithubToStreamsOutput() {
        /*
         * Mock users
        */
        final Account account1 = new Account("mockUser1", "1w");
        final Account account2 = new Account("mockUser2", "1w");
        final Account account3 = new Account("mockUser3", "1w");
        /*
         * Expected commits
        */
        final KafkaCommitRecord expectedCommit1 =
            new KafkaCommitRecord.KafkaCommitRecordBuilder()
                .sha("sha1")
                .authorLogin("mockUser1")
                .authorName("name1")
                .createdTime(ZonedDateTime.now().minusDays(10))
                .language("Java")
                .message("test message 1")
                .commitRepository("repository")
                .build();
        final KafkaCommitRecord expectedCommit2 =
            new KafkaCommitRecord.KafkaCommitRecordBuilder()
                .sha("sha2")
                .authorLogin("mockUser1")
                .authorName("name1")
                .createdTime(ZonedDateTime.now().minusDays(10))
                .language("Scala")
                .message("test message 2")
                .commitRepository("repository2")
                .build();
        final KafkaCommitRecord expectedCommit3 =
            new KafkaCommitRecord.KafkaCommitRecordBuilder()
                .sha("sha3")
                .authorLogin("mockUser2")
                .authorName("name3")
                .createdTime(ZonedDateTime.now().minusDays(10))
                .language("Python")
                .message("test message 3")
                .commitRepository("repository3")
                .build();
        final KafkaCommitRecord expectedCommit4 =
            new KafkaCommitRecord.KafkaCommitRecordBuilder()
                .sha("sha4")
                .authorLogin("mockUser3")
                .authorName("name4")
                .createdTime(ZonedDateTime.now().minusDays(10))
                .language("Scala")
                .message("test message 3")
                .commitRepository("repository4")
                .build();
        final KafkaCommitRecord expectedCommit5 =
            new KafkaCommitRecord.KafkaCommitRecordBuilder()
                .sha("sha5")
                .authorLogin("mockUser3")
                .authorName("name4")
                .createdTime(ZonedDateTime.now().minusDays(10))
                .language("Python")
                .message("test message 3")
                .commitRepository("repository5")
                .build();
        final KafkaCommitRecord expectedCommit6 =
            new KafkaCommitRecord.KafkaCommitRecordBuilder()
                .sha("sha6")
                .authorLogin("mockUser3")
                .authorName("name4")
                .createdTime(ZonedDateTime.now().minusDays(10))
                .language("Scala")
                .message("test message 3")
                .commitRepository("repository6")
                .build();

        try {
            /*
             * Prepare github API mock responses
            */
            apiMockServer.createExpectedResponseForGithubCommitsSearchRequest(account1.getUser(), expectedCommit1, expectedCommit2);
            apiMockServer.createExpectedResponseForGithubCommitsSearchRequest(account2.getUser(), expectedCommit3);
            apiMockServer.createExpectedResponseForGithubCommitsSearchRequest(account3.getUser(), expectedCommit4, expectedCommit5, expectedCommit6);

            accountsProducer.send(createRecord(GITHUB_ACCOUNTS_TOPIC, "null", objectMapper.writeValueAsString(account1)));
            accountsProducer.send(createRecord(GITHUB_ACCOUNTS_TOPIC, "null", objectMapper.writeValueAsString(account2)));
            accountsProducer.send(createRecord(GITHUB_ACCOUNTS_TOPIC, "null", objectMapper.writeValueAsString(account3)));

            accountsProducer.flush();   // make all buffered records immediately available to send

            assertTopKCommittersMetric(githubAccountsApp);   // K variable can be set up in 'config.test.properties'
            assertUsedLanguagesMetric(githubAccountsApp);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            Assert.fail("end2endPipelineFromGithubToStreamsOutput: JsonProcessingException error!");
        }

    }

    private void assertTopKCommittersMetric(final GithubAccountsApplication pipelineRef) {
        assertThat(pollRecords(pipelineRef, topKCommittersConsumer))
            .containsSequence(
                "{\"account\":\"mockUser3\",\"totalCommits\":\"3\"}\n" +
                "{\"account\":\"mockUser1\",\"totalCommits\":\"2\"}"
        );
    }

    private void assertUsedLanguagesMetric(final GithubAccountsApplication pipelineRef) {
        assertThat(pollRecords(pipelineRef, usedLanguagesConsumer))
            .contains("{\"Java\":1}")
            .contains("{\"Scala\":3}")
            .contains("{\"Python\":2}");
    }

    private <K,V> List<String> pollRecords(final GithubAccountsApplication app, final KafkaConsumer<K,V> consumer) {
        final List<String> result = new ArrayList<>();
        for(int retries = 0; retries < 4; retries++) {
            consumer.poll(Duration.ofMillis(1000)).iterator().forEachRemaining(x -> result.add(String.valueOf(x.value())));
            app.runPipeline();
        }
        return result;
    }

    private <K,V> ProducerRecord<K, V> createRecord(final String topic, final K key, final V value) {
        return new ProducerRecord<>(topic, key, value);
    }
}
