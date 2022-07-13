package com.github.mkrolczyk12.kafka.metrics.streams;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.mkrolczyk12.kafka.githubAccountsApp.commitsProducer.projection.KafkaCommitRecord;
import org.apache.kafka.streams.KafkaStreams;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.internal.verification.VerificationModeFactory.times;

class TotalCommitsPerLanguageTest extends AbstractBaseStream {
    @Override
    AbstractKafkaStream createMetricsKafkaStream(Properties properties) {
        return new TotalCommitsPerLanguage(INPUT_TOPIC_NAME, OUTPUT_TOPIC_NAME, properties);
    }

    @Test
    @DisplayName("Should count total commits per language")
    void countTotalCommitsPerLanguage() {
        final KafkaCommitRecord commit1 =
            new KafkaCommitRecord.KafkaCommitRecordBuilder()
                .sha("sha1")
                .authorLogin("mockUser1")
                .authorName("name1")
                .createdTime(ZonedDateTime.now().minusDays(10))
                .language("Java")
                .message("test message 1")
                .commitRepository("repository")
                .build();
        final KafkaCommitRecord commit2 =
            new KafkaCommitRecord.KafkaCommitRecordBuilder()
                .sha("sha2")
                .authorLogin("mockUser1")
                .authorName("name1")
                .createdTime(ZonedDateTime.now().minusDays(10))
                .language("Scala")
                .message("test message 2")
                .commitRepository("repository2")
                .build();
        final KafkaCommitRecord commit3 =
            new KafkaCommitRecord.KafkaCommitRecordBuilder()
                .sha("sha3")
                .authorLogin("mockUser2")
                .authorName("name3")
                .createdTime(ZonedDateTime.now().minusDays(10))
                .language("Python")
                .message("test message 3")
                .commitRepository("repository3")
                .build();
        final KafkaCommitRecord commit4 =
            new KafkaCommitRecord.KafkaCommitRecordBuilder()
                .sha("sha4")
                .authorLogin("mockUser3")
                .authorName("name4")
                .createdTime(ZonedDateTime.now().minusDays(10))
                .language("Scala")
                .message("test message 3")
                .commitRepository("repository4")
                .build();
        final KafkaCommitRecord commit5 =
            new KafkaCommitRecord.KafkaCommitRecordBuilder()
                .sha("sha5")
                .authorLogin("mockUser3")
                .authorName("name4")
                .createdTime(ZonedDateTime.now().minusDays(10))
                .language("Python")
                .message("test message 3")
                .commitRepository("repository5")
                .build();
        final KafkaCommitRecord commit6 =
            new KafkaCommitRecord.KafkaCommitRecordBuilder()
                .sha("sha6")
                .authorLogin("mockUser3")
                .authorName("name4")
                .createdTime(ZonedDateTime.now().minusDays(10))
                .language("Scala")
                .message("test message 3")
                .commitRepository("repository6")
                .build();

        final List<KafkaCommitRecord> records =
                Arrays.asList(commit1, commit2, commit3, commit4, commit5, commit6);

        for(KafkaCommitRecord record : records) pushRecord(record);

        assertThat(outputTopic.readValuesToList())
            .contains("{\"Java\":1}")
            .contains("{\"Scala\":3}")
            .contains("{\"Python\":2}");
    }

    @Test
    @DisplayName("Should ignore duplicated records")
    void duplicateCommitsAreCountedAsOne() throws JsonProcessingException {
        final KafkaCommitRecord commit =
            new KafkaCommitRecord.KafkaCommitRecordBuilder()
                .sha("sha1")
                .authorLogin("mockUser1")
                .authorName("name1")
                .createdTime(ZonedDateTime.now().minusDays(10))
                .language("Scala")
                .message("test message 1")
                .commitRepository("repository")
                .build();
        final String commitAsJson = objectMapper.writeValueAsString(commit);

        inputTopic.pipeInput(commit.getSha(), commitAsJson);
        inputTopic.pipeInput(commit.getSha(), commitAsJson);

        assertThat(outputTopic.readValue()).isEqualTo("{\"Scala\":1}");
        assertTrue(outputTopic.isEmpty());
    }

    @Test
    @DisplayName("Should verify if total commits per language stream closed successfully")
    void close(){
        TotalCommitsPerLanguage totalCommitsPerLanguage =
                new TotalCommitsPerLanguage(INPUT_TOPIC_NAME, OUTPUT_TOPIC_NAME, new Properties());

        totalCommitsPerLanguage.kafkaStreams = mock(KafkaStreams.class);

        totalCommitsPerLanguage.close();
        verify(totalCommitsPerLanguage.kafkaStreams, times(1)).close();
    }

    private void pushRecord(final KafkaCommitRecord record) {
        String recordAsJson = null;
        try {
            recordAsJson = objectMapper.writeValueAsString(record);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            fail("TotalCommitsPerLanguageTest: JsonProcessingException error!");
        }
        inputTopic.pipeInput(record.getSha(), recordAsJson);
    }
}