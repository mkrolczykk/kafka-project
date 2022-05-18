package com.github.mkrolczyk12.kafka.metrics.streams;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.Topology;

public abstract class AbstractKafkaStream {
    KafkaStreams kafkaStreams;

    /** Proxies the call to KafkaStreams `start` method. */
    public void start() {
        kafkaStreams.start();
    }

    /** Creates the topology for KafkaStreams. */
    abstract Topology createStreamTopology();

    /** Proxies the call to KafkaStreams `close` method. */
    public void close() {
        kafkaStreams.close();
    }

    /** Proxies the call to KafkaStreams `cleanUp` method. */
    public void cleanUp() {
        kafkaStreams.cleanUp();
    }
}
