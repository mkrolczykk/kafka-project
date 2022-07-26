package com.github.mkrolczyk12.kafka.metrics;

import com.github.mkrolczyk12.kafka.githubAccountsApp.GithubAccountsApplication;
import com.github.mkrolczyk12.kafka.metrics.streams.AbstractKafkaStream;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Properties;

class E2EMetricsBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(E2EMetricsBuilder.class);

    private final static Properties props = E2EProperties.getModuleProperties();

    private final String bootstrapServers;

    public E2EMetricsBuilder() {
        this.bootstrapServers = props.getProperty("kafka.bootstrap.servers");
    }

    public E2EMetricsBuilder(final String bootstrapServers) {
        this.bootstrapServers = bootstrapServers;
    }

    KafkaProducer<String, String> createAccountsProducer() {
        return createAccountsProducer(this.bootstrapServers);
    }

    KafkaProducer<String, String> createAccountsProducer(final String bootstrapServers) {
        final Properties properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");
        properties.setProperty(ProducerConfig.ACKS_CONFIG, "all");

        return new KafkaProducer<>(properties);
    }

    KafkaConsumer<String, String> createCommitsConsumer() {
        return createCommitsConsumer(this.bootstrapServers);
    }

    KafkaConsumer<String, String> createCommitsConsumer(final String bootstrapServers) {
        Properties properties = new Properties();
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, "commits-consumer");

        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(properties);
        consumer.subscribe(Collections.singleton(props.getProperty("github.commits")));

        return consumer;
    }

    KafkaConsumer<String, String> createTotalCommitsConsumer(final String bootstrapServers) {
        Properties properties = new Properties();
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, "total-commits-consumer");

        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(properties);
        consumer.subscribe(Collections.singleton(props.getProperty("github.metrics.total.number.of.commits")));

        return consumer;
    }

    KafkaConsumer<String, String> createTotalCommittersNumberConsumer(final String bootstrapServers) {
        Properties properties = new Properties();
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, "total-committers-number-consumer");

        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(properties);
        consumer.subscribe(Collections.singleton(props.getProperty("github.metrics.total.number.of.committers")));

        return consumer;
    }

    KafkaConsumer<String, String> createTopKCommittersConsumer() {
        Properties properties = new Properties();
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, "top-k-committers-consumer");

        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(properties);
        consumer.subscribe(Collections.singleton(props.getProperty("github.metrics.top.k.contr.by.commits")));

        return consumer;
    }

    KafkaConsumer<String, String> createTotalCommitsPerLanguagesConsumer() {
        Properties properties = new Properties();
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, "used-languages-consumer");

        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(properties);
        consumer.subscribe(Collections.singleton(props.getProperty("github.metrics.total.language")));

        return consumer;
    }

    GithubAccountsApplication runGithubAccountsApp() {
        return new GithubAccountsApplication(bootstrapServers, props);
    }

    GithubAccountsApplication runGithubAccountsApp(final String bootstrapServers) {
        return new GithubAccountsApplication(bootstrapServers, props);
    }

    List<AbstractKafkaStream> runKafkaStreamsApp() {
        KafkaStreamsApplication kafkaStreamsApp = new KafkaStreamsApplication(bootstrapServers, props);
        kafkaStreamsApp.launchStreams();
        return kafkaStreamsApp.getStreams();
    }
}
