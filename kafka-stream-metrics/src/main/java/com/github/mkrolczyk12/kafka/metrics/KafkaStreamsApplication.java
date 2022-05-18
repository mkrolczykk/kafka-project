package com.github.mkrolczyk12.kafka.metrics;

import com.github.mkrolczyk12.kafka.metrics.streams.AbstractKafkaStream;
import com.github.mkrolczyk12.kafka.metrics.streams.TopKContributorsByNumberOfCommits;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class KafkaStreamsApplication {
    private static final Logger LOG = LoggerFactory.getLogger(KafkaStreamsApplication.class);

    private final Properties props;

    private final List<AbstractKafkaStream> streams = new ArrayList<>();

    public KafkaStreamsApplication(final Properties props) {
        this.props = props;
    }

    private static Properties getAppProperties() {
        Properties properties = new Properties();

        try {
            InputStream input = KafkaStreamsApplication.class.getClassLoader().getResourceAsStream("config.properties");

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
        Runtime.getRuntime().addShutdownHook(new Thread(() -> streams.forEach(AbstractKafkaStream::close)));
    }

    // TODO -> change to be more flexible
    private Properties baseProperties() {
        Properties properties = new Properties();
        properties.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, props.getProperty("kafka.bootstrap.servers"));
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        properties.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        properties.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        properties.put(StreamsConfig.PROCESSING_GUARANTEE_CONFIG, StreamsConfig.EXACTLY_ONCE);
        properties.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, "0");

        return properties;
    }

    public void launchStreams() {
        /*
         * Top 5 contributors by number of commits stream
        */
        AbstractKafkaStream topKCommittersStream =
                new TopKContributorsByNumberOfCommits(
                        Integer.parseInt(props.getProperty("topK.contributors.k.value")),
                        props.getProperty("topK.contributors.input.topic"),
                        props.getProperty("topK.contributors.output.topic"),
                        baseProperties()
                );
        topKCommittersStream.start();

        streams.add(topKCommittersStream);
        LOG.info("Top K committers stream has been launched successfully");

        addPipelineShutdownHook();
    }

    public List<AbstractKafkaStream> getStreams() {
        return streams;
    }

    public static void main(String[] args) {
        LOG.info("Starting KafkaStreams application...");

        final Properties appConfig = KafkaStreamsApplication.getAppProperties();
        KafkaStreamsApplication app = new KafkaStreamsApplication(appConfig);

        app.launchStreams();
    }
}