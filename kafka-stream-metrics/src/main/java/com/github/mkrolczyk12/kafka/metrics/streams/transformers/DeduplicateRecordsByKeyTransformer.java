package com.github.mkrolczyk12.kafka.metrics.streams.transformers;

import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.Transformer;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.state.KeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeduplicateRecordsByKeyTransformer implements Transformer<String, String, KeyValue<String, String>> {
    private static final Logger LOG = LoggerFactory.getLogger(DeduplicateRecordsByKeyTransformer.class);

    private final String storeName;

    private KeyValueStore<String, String> store;

    public DeduplicateRecordsByKeyTransformer(final String storeName) {
        this.storeName = storeName;
    }

    @Override
    public void init(final ProcessorContext processorContext) {
        this.store = (KeyValueStore<String, String>) processorContext.getStateStore(storeName);
    }

    @Override
    public KeyValue<String, String> transform(final String key, final String value) {
        String response = store.putIfAbsent(key, value);
        LOG.debug("lookup result for key " + key + ": " + response);
        if (response != null) return null;
        return new KeyValue<>(key, value);
    }

    @Override
    public void close() {}
}
