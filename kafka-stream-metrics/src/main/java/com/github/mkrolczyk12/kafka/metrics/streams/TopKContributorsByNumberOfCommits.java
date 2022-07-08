package com.github.mkrolczyk12.kafka.metrics.streams;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.mkrolczyk12.kafka.metrics.streams.model.CommitRecord;
import com.github.mkrolczyk12.kafka.metrics.streams.transformers.DeduplicateRecordsByKeyTransformer;
import com.github.mkrolczyk12.kafka.metrics.streams.transformers.TopKContributorsByNumberOfCommitsTransformer;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class TopKContributorsByNumberOfCommits extends AbstractKafkaStream {
    private static final Logger LOG = LoggerFactory.getLogger(TopKContributorsByNumberOfCommits.class);

    private static final String DEDUPLICATE_COMMITS_STORE = "top5-committers-distinct-commits";

    private static final String TOTAL_COMMITS_BY_AUTHOR_STORE = "top5-committers-total-commits-by-author";

    private final int K;

    private final String inputTopic;

    private final String outputTopic;

    private final Properties props;

    private final ObjectMapper objectMapper;

    public TopKContributorsByNumberOfCommits(final int k, final String inputTopic, final String outputTopic, final Properties properties) {
        this.K = k;
        this.inputTopic = inputTopic;
        this.outputTopic = outputTopic;
        this.props = properties;
        this.objectMapper = new ObjectMapper();

        this.objectMapper.registerModule(new JavaTimeModule());
        this.props.put(StreamsConfig.APPLICATION_ID_CONFIG, "github-metrics-top5-contributors-by-commits");
    }

    @Override
    Topology createStreamTopology() {
        StreamsBuilder streamsBuilder = new StreamsBuilder();

        StoreBuilder<KeyValueStore<String, String>> keyValueStoreBuilder =
            Stores.keyValueStoreBuilder(
                Stores.persistentKeyValueStore(DEDUPLICATE_COMMITS_STORE),
                Serdes.String(), Serdes.String()
            );
        streamsBuilder.addStateStore(keyValueStoreBuilder);

        KStream<String, String> topKCommittersStream = streamsBuilder
            .stream(inputTopic, Consumed.with(Serdes.String(), Serdes.String()))
            .transform(() -> new DeduplicateRecordsByKeyTransformer(DEDUPLICATE_COMMITS_STORE), DEDUPLICATE_COMMITS_STORE)
            .selectKey((key, value) -> {
                CommitRecord kafkaCommitRecord = null;
                try {
                    kafkaCommitRecord = objectMapper.readValue(value, CommitRecord.class);
                } catch (Exception e) {
                    LOG.warn("Cannot read the value - data may be malformed", e);
                }
                return kafkaCommitRecord != null ? kafkaCommitRecord.getAuthorLogin() : null;
            })
            .groupByKey()
            .count(Materialized.as(TOTAL_COMMITS_BY_AUTHOR_STORE))
            .toStream()
            .transform(
                () -> new TopKContributorsByNumberOfCommitsTransformer(K, TOTAL_COMMITS_BY_AUTHOR_STORE), TOTAL_COMMITS_BY_AUTHOR_STORE);

        topKCommittersStream.to(outputTopic, Produced.with(Serdes.String(), Serdes.String()));

        return streamsBuilder.build();
    }

    @Override
    public void start() {
        kafkaStreams = new KafkaStreams(createStreamTopology(), props);
        super.start();
    }
}
