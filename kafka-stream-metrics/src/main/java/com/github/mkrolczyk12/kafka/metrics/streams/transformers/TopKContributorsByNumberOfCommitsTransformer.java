package com.github.mkrolczyk12.kafka.metrics.streams.transformers;

import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.Transformer;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.ValueAndTimestamp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class TopKContributorsByNumberOfCommitsTransformer implements Transformer<String, Long, KeyValue<String, String>> {
    private static final Logger LOG = LoggerFactory.getLogger(TopKContributorsByNumberOfCommitsTransformer.class);

    private final int K;

    private final String storeName;

    private KeyValueStore store;

    public TopKContributorsByNumberOfCommitsTransformer(final int k, final String storeName) {
        this.K = k;
        this.storeName = storeName;
    }

    @Override
    public void init(final ProcessorContext processorContext) {
        this.store = (KeyValueStore) processorContext.getStateStore(storeName);
    }

    @Override
    public KeyValue<String, String> transform(final String s, final Long aLong) {
        KeyValueIterator<String, ValueAndTimestamp> iterator = store.all();
        List<KeyValue<String, ValueAndTimestamp>> contributors = new ArrayList<>();

        while (iterator.hasNext()) contributors.add(iterator.next());

        contributors.sort(Comparator.comparing(o ->
                ((KeyValue<String, ValueAndTimestamp<Long>>) o).value.value()).reversed());

        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < K && i < contributors.size(); i++) {
            stringBuilder
                    .append("{")
                    .append("\"account\": \"" + contributors.get(i).key + "\",")
                    .append("\"totalCommits\": \"" + contributors.get(i).value.value() + "\"")
                    .append("}");
            if (i != (K - 1) && i != contributors.size() - 1) {
                stringBuilder.append("\n");
            }
        }

        return new KeyValue<>("top5-committers",  stringBuilder.toString());
    }

    @Override
    public void close() {}
}
