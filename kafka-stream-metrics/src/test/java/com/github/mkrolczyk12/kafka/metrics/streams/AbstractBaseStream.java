package com.github.mkrolczyk12.kafka.metrics.streams;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.util.Properties;

abstract class AbstractBaseStream {
    TopologyTestDriver testDriver;

    TestInputTopic<String, String> inputTopic;

    TestOutputTopic<String, String> outputTopic;

    ObjectMapper objectMapper;

    final String INPUT_TOPIC_NAME = "inputTopic";

    final String OUTPUT_TOPIC_NAME = "outputTopic";

    @BeforeEach
    void setUp() {
        final Properties properties = new Properties();
        properties.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:1234");
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        properties.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        properties.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

        AbstractKafkaStream metricsKafkaStream = createMetricsKafkaStream(properties);
        Topology topology = metricsKafkaStream.createStreamTopology();

        testDriver = new TopologyTestDriver(topology, properties);
        inputTopic = testDriver.createInputTopic(INPUT_TOPIC_NAME, new StringSerializer(), new StringSerializer());
        outputTopic = testDriver.createOutputTopic(OUTPUT_TOPIC_NAME, new StringDeserializer(), new StringDeserializer());

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    @AfterEach
    public void closeTestDriver() {
        testDriver.close();
    }

    abstract AbstractKafkaStream createMetricsKafkaStream(Properties properties);
}
