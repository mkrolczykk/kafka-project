package com.github.mkrolczyk12.kafka.githubAccountsApp.commitsProducer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.mkrolczyk12.kafka.githubAccountsApp.commitsProducer.projection.KafkaCommitRecord;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.ZonedDateTime;

import static org.mockito.Mockito.*;

class CommitsProducerTest {
    private static KafkaProducer<String, String> kafkaProducer;

    private static CommitsProducer commitsProducer;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() throws NoSuchFieldException, IllegalAccessException {
        kafkaProducer = mock(KafkaProducer.class);

        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        commitsProducer = new CommitsProducer("localhost:9090", "mockTopic");

        Field privateProducerField = CommitsProducer.class.getDeclaredField("producer");
        privateProducerField.setAccessible(true);
        privateProducerField.set(commitsProducer, kafkaProducer);
    }

    @Test
    @DisplayName("Should produce and push commit record")
    void push_success() throws JsonProcessingException {
        final KafkaCommitRecord expectedCommit =
            new KafkaCommitRecord.KafkaCommitRecordBuilder()
                .sha("sha1")
                .authorLogin("mockUser1")
                .authorName("name1")
                .createdTime(ZonedDateTime.now().minusDays(10))
                .language("Java")
                .message("test message 1")
                .commitRepository("repository")
                .build();

        final String commitAsJson = objectMapper.writeValueAsString(expectedCommit);

        ProducerRecord<String, String> record =
            new ProducerRecord<>("mockTopic", expectedCommit.getSha(), commitAsJson);

        commitsProducer.push(expectedCommit);

        verify(kafkaProducer, times(1)).send(record);
    }

    @Test
    @DisplayName("Should verify if commits producer closed successfully")
    void close() {
        commitsProducer.close();
        verify(kafkaProducer, times(1)).flush();
        verify(kafkaProducer, times(1)).close();
    }
}