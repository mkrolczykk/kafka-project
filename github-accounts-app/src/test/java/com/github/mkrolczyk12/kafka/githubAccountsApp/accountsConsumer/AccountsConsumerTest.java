package com.github.mkrolczyk12.kafka.githubAccountsApp.accountsConsumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mkrolczyk12.kafka.githubAccountsApp.accountsConsumer.projection.Account;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.lang.reflect.Field;
import java.time.Duration;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

class AccountsConsumerTest {
    private KafkaConsumer<String, String> kafkaConsumer;

    private AccountsConsumer accountsConsumer;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() throws NoSuchFieldException, IllegalAccessException {
        kafkaConsumer = mock(KafkaConsumer.class);
        accountsConsumer =
                new AccountsConsumer("localhost:9090", "groupId", Duration.ofMillis(1000));
        Field privateConsumerField = AccountsConsumer.class.getDeclaredField("consumer");
        privateConsumerField.setAccessible(true);
        privateConsumerField.set(accountsConsumer, kafkaConsumer);
    }

    @Test
    @DisplayName("Should assert subscription success")
    void subscribe() {
        accountsConsumer.subscribe("mockTopic");
        verify(kafkaConsumer, times(1))
            .subscribe(new HashSet<>(Collections.singletonList("mockTopic")));
    }

    @Test
    @DisplayName("Should poll data successfully")
    void poll_success() throws JsonProcessingException {
        final Account expectedAccount = new Account("mockUser1", "1w");
        final String expectedAccountAsJson = objectMapper.writeValueAsString(expectedAccount);

        ConsumerRecords<String, String> consumerRecords = prepareRecords(expectedAccountAsJson);
        when(kafkaConsumer.poll(any())).thenReturn(consumerRecords);

        StepVerifier.
            create(accountsConsumer.poll())
            .expectSubscription()
            .expectNext(expectedAccount)
            .expectComplete()
            .verify();
    }

    @Test
    @DisplayName("Should poll nothing because of incorrect account format")
    void poll_incorrectAccountFormat() {
        ConsumerRecords<String, String> consumerRecords = prepareRecords("");
        when(kafkaConsumer.poll(any())).thenReturn(consumerRecords);

        StepVerifier
            .create(accountsConsumer.poll())
            .expectSubscription()
            .expectNextCount(0)
            .expectComplete()
            .verify();
    }

    @Test
    @DisplayName("Should verify if accounts consumer closed successfully")
    void close() {
        accountsConsumer.close();
        verify(kafkaConsumer, times(1)).close();
    }

    private ConsumerRecords<String, String> prepareRecords(final String expectedJson) {
        ConsumerRecords<String, String> consumerRecords = mock(ConsumerRecords.class);

        final List<ConsumerRecord<String, String>> records =
            Collections.singletonList(new ConsumerRecord<>("mockTopic", 1, 10, "null", expectedJson));

        when(consumerRecords.iterator()).thenReturn(records.iterator());
        when(consumerRecords.spliterator()).thenReturn(Spliterators.spliteratorUnknownSize(records.iterator(), 0));

        return consumerRecords;
    }
}