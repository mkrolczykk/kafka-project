package com.github.mkrolczyk12.kafka.githubAccountsApp.commitsProducer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.mkrolczyk12.kafka.githubAccountsApp.commitsProducer.projection.KafkaCommitRecord;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class CommitsProducer {
    private final String bootstrapServers;
    private final String topic;
    private final ObjectMapper objectMapper;
    private KafkaProducer<String, String> producer;

    private static final Logger logger = LoggerFactory.getLogger(CommitsProducer.class);


    public CommitsProducer(String bootstrapServers, String topic) {
        this.bootstrapServers = bootstrapServers;
        this.topic = topic;
        objectMapper = new ObjectMapper();

        /*
            make Jackson recognize Java 8 Date & Time API data types (JSR-310)
         */
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        Properties properties = new Properties();

        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.setProperty(ProducerConfig.ACKS_CONFIG, "all");
        properties.setProperty(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");   // provides the exactly-once-delivery
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        producer = new KafkaProducer<>(properties);
    }

    public String getBootstrapServers() { return bootstrapServers; }

    public String getTopic() { return topic; }

    public void push(KafkaCommitRecord commit) {
        logger.info("Pushing commit from user '" + commit.getAuthorLogin() + "' into kafka '" + topic + "' topic");

        String commitAsString;
        try {
            commitAsString = objectMapper.writeValueAsString(commit);
            producer.send(new ProducerRecord<>(topic, commit.getSha(), commitAsString));
        } catch (JsonProcessingException e) {
            logger.warn("JsonProcessingException while converting '" + commit + "' to string!");
        }
    }

    public void close() {
        logger.info("Ending producer...");
        producer.flush();
        producer.close();
    }
}