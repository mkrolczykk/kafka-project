package io.github.githubAccountsApp.accountsConsumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.githubAccountsApp.accountsConsumer.projection.Account;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.*;

public class AccountsConsumer {
    private static final Logger logger = LoggerFactory.getLogger(AccountsConsumer.class);
    private final String bootstrapServers;
    private final String groupId;
    private final Set<String> subscribedTopics;
    private final ObjectMapper objectMapper;
    private final KafkaConsumer<String, String> consumer;

    public AccountsConsumer(final String bootstrapServers, final String groupId) {
        this.bootstrapServers = bootstrapServers;
        this.groupId = groupId;
        this.subscribedTopics = new HashSet<>();

        this.objectMapper = new ObjectMapper();

        Properties properties = new Properties();
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, this.bootstrapServers);
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

        consumer = new KafkaConsumer<>(properties);
    }

    public String getBootstrapServers() { return bootstrapServers; }

    public String getGroupId() { return groupId; }

    public Set<String> getSubscribedTopics() { return subscribedTopics; }

    public void subscribe(final String topic) {
        consumer.subscribe(Collections.singleton(topic));
        subscribedTopics.add(topic);
    }

    public void subscribe(final Set<String> topics) {
        consumer.subscribe(topics);
        subscribedTopics.addAll(topics);
    }

    public List<Account> poll(Duration duration) {
        List<Account> accounts = new ArrayList<>();
        ConsumerRecords<String, String> records = consumer.poll(duration);

        for (ConsumerRecord<String, String> record : records) {
            try {
                logger.info(
                        "Processing record from partition " + record.partition() +
                        ", with offset: " + record.offset() +
                        ", key: " + record.key() +
                        ", value: " + record.value()
                );
                accounts.add(fromJsonStringToAccount(record.value()));
            } catch (JsonProcessingException e) {
                logger.warn("JsonProcessingException while converting record '" + record + "'");
            }
        }

        return accounts;
    }

    private Account fromJsonStringToAccount(String jsonString) throws JsonProcessingException {
        return objectMapper.readValue(jsonString, Account.class);
    }

    public void close() {
        consumer.close();
    }

    @Override
    public String toString() {
        return "AccountsConsumer{" +
                "bootstrapServers='" + bootstrapServers + '\'' +
                ", groupId='" + groupId + '\'' +
                ", subscribedTopics=" + subscribedTopics +
                '}';
    }
}