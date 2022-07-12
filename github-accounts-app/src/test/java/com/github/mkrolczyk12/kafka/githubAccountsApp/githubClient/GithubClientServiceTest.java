package com.github.mkrolczyk12.kafka.githubAccountsApp.githubClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.mkrolczyk12.kafka.githubAccountsApp.commitsProducer.projection.KafkaCommitRecord;
import com.github.mkrolczyk12.kafka.githubAccountsApp.githubClient.projection.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

class GithubClientServiceTest {

    @Test
    @DisplayName("Should successfully get commits from github API")
    void getUserCommits_success() throws JsonProcessingException {
        final KafkaCommitRecord expectedCommit1 =
            new KafkaCommitRecord.KafkaCommitRecordBuilder()
                .sha("sha1")
                .authorLogin("mockUser1")
                .authorName("name1")
                .createdTime(ZonedDateTime.now().minusDays(10).withZoneSameInstant(ZoneId.of("UTC")))
                .language("Scala")
                .message("test message 1")
                .commitRepository("repository")
                .build();

        final ClientResponse resp = prepareCommitsGithubResponse(expectedCommit1);

        final WebClient.Builder webClientBuilder = WebClient.builder().exchangeFunction(request -> Mono.just(resp));
        final GithubClientService mockService = new MockGithubClientService(webClientBuilder, "github.mock.api");

        StepVerifier
            .create(mockService.getUserCommits("mockUser1", LocalDateTime.from(ZonedDateTime.now().minusDays(10))))
            .expectSubscription()
            .expectNext(expectedCommit1)
            .expectComplete()
            .verify();
    }

    @Test
    @DisplayName("Should return nothing because of API service error")
    void getUserCommits_error() {
        ClientResponse resp = ClientResponse
            .create(HttpStatus.SERVICE_UNAVAILABLE)
            .body("")
            .build();

        final WebClient.Builder webClientBuilder = WebClient.builder().exchangeFunction(request -> Mono.just(resp));
        final GithubClientService mockService = new MockGithubClientService(webClientBuilder, "github.mock.api");

        StepVerifier
            .create(mockService.getUserCommits("mockUser1", LocalDateTime.from(ZonedDateTime.now().minusDays(10))))
            .expectSubscription()
            .expectNextCount(0)
            .expectError()
            .verify();
    }

    @Test
    @DisplayName("Should return most often used language from list of languages")
    void getCommitLanguage_success() throws JsonProcessingException {
        final String expectedLang = "Scala";
        final Map<String, Long> expectedLangResp = new HashMap<String, Long>() {{
            put(expectedLang, 10_000L);
            put("Python", 5000L);
            put("C++", 3000L);
        }};
        final ClientResponse resp = prepareLanguagesListGithubResponse(expectedLangResp);

        SearchCommitsQueryItem item =
            new SearchCommitsQueryItem(
                "sha1",
                new Commit(
                    new CommitAuthorInfo("author1", ZonedDateTime.now()),
                    new CommitCommiterInfo("login1", ZonedDateTime.now()),
                    "message1"
                ),
                new Author(
                    "login1",
                    10,
                    "1234",
                    "subscrUrl",
                    "reposUrl",
                    "type1",
                    true
                ),
                new Committer(
                    "login1",
                    10,
                    "1234",
                    "reposUrl",
                    "type1",
                    true
                ),
                new Repository(
                    10,
                    "4567",
                    "repository",
                    "full_name",
                    false,
                    "desc1",
                    "https://github.com/languages"
                ),
                null
            );

        final WebClient.Builder webClientBuilder = WebClient.builder().exchangeFunction(request -> Mono.just(resp));
        final GithubClientService mockService = new MockGithubClientService(webClientBuilder, "github.mock.api");

        StepVerifier
            .create(mockService.getCommitLanguage(item))
            .expectSubscription()
            .expectNext(expectedLang)
            .expectComplete()
            .verify();
    }

    @Test
    @DisplayName("Should return 'Unknown language' as language")
    void getCommitLanguage_noLanguage() throws JsonProcessingException {
        final String expectedLang = "Unknown language";
        final Map<String, Long> expectedLangResp = new HashMap<String, Long>() {};
        final ClientResponse resp = prepareLanguagesListGithubResponse(expectedLangResp);

        SearchCommitsQueryItem item =
            new SearchCommitsQueryItem(
                "sha2",
                new Commit(
                    new CommitAuthorInfo("author1", ZonedDateTime.now()),
                    new CommitCommiterInfo("login1", ZonedDateTime.now()),
                    "message1"
                ),
                new Author(
                    "login1",
                    10,
                    "1234",
                    "subscrUrl",
                    "reposUrl",
                    "type1",
                    true
                ),
                new Committer(
                    "login1",
                    10,
                    "1234",
                    "reposUrl",
                    "type1",
                    true
                ),
                new Repository(
                    10,
                    "4567",
                    "repository",
                    "full_name",
                    false,
                    "desc1",
                    "https://github.com/languages"
                ),
                null
            );

        final GithubClientService mockService = new GithubClientService("github.mock.api");

        StepVerifier
            .create(mockService.getCommitLanguage(item))
            .expectSubscription()
            .expectNext(expectedLang)
            .expectComplete()
            .verify();
    }

    private ClientResponse prepareCommitsGithubResponse(final KafkaCommitRecord commit) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        SearchCommitsQueryItem searchCommitsQueryItem =
            new SearchCommitsQueryItem(
                commit.getSha(),
                new Commit(
                    new CommitAuthorInfo(commit.getAuthorName(), commit.getCreatedTime()),
                    new CommitCommiterInfo(commit.getAuthorLogin(), commit.getCreatedTime()),
                    commit.getMessage()
                ),
                new Author(
                    commit.getAuthorLogin(),
                    10,
                    "1234",
                    "subscrUrl",
                    "reposUrl",
                    "type1",
                    true
                ),
                new Committer(
                    commit.getAuthorLogin(),
                    10,
                    "1234",
                    "reposUrl",
                    "type1",
                    true
                ),
                new Repository(
                    10,
                    "4567",
                    "repository",
                    "full_name",
                    false,
                    "desc1",
                    String.format("http://%s:%d/repo/languages/%s", "localhost", 5555, commit.getLanguage())
                ),
                commit.getLanguage()
            );

        final SearchCommitsQueryModel resp =
            new SearchCommitsQueryModel(1, Collections.singletonList(searchCommitsQueryItem));

        final String responseAsJson = objectMapper.writeValueAsString(resp);

        return ClientResponse
            .create(HttpStatus.OK)
            .header("Content-Type", "application/json")
            .body(responseAsJson).build();
    }

    private ClientResponse prepareLanguagesListGithubResponse(final Map<String, Long> languagesResp)
            throws JsonProcessingException {

        final ObjectMapper objectMapper = new ObjectMapper();

        String responseAsJson = objectMapper.writeValueAsString(languagesResp);

        return ClientResponse
            .create(HttpStatus.OK)
            .header("Content-Type", "application/json")
            .body(responseAsJson)
            .build();
    }
}