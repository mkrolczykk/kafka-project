package com.github.mkrolczyk12.kafka.metrics;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mkrolczyk12.kafka.githubAccountsApp.commitsProducer.projection.KafkaCommitRecord;
import com.github.mkrolczyk12.kafka.githubAccountsApp.githubClient.projection.*;
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static org.mockserver.matchers.Times.exactly;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

public class E2EApiMockServer {
    private static final Logger LOG = LoggerFactory.getLogger(E2EApiMockServer.class);

    private final static Properties props = E2EProperties.getModuleProperties();

    private final String host = props.getProperty("github.api.host");

    private final Integer port = Integer.valueOf(props.getProperty("github.api.port"));

    private final ObjectMapper objectMapper;

    private final ClientAndServer mockServer;

    E2EApiMockServer(final ObjectMapper sourceObjectMapper) {
        this.objectMapper = sourceObjectMapper;
        this.mockServer = ClientAndServer.startClientAndServer(port);
    }

    String getMockServerUrl() {
        return props.getProperty("github.api.base.url");
    }

    void createExpectedResponseForGithubCommitsSearchRequest(final String author, final KafkaCommitRecord... commits)
            throws JsonProcessingException {

        final int batchSize = commits.length;
        final List<SearchCommitsQueryItem> items = new ArrayList<>(batchSize);

        for (final KafkaCommitRecord commit : commits) items.add(createExpectedResponseForItemLanguage(commit));

        final SearchCommitsQueryModel resp = new SearchCommitsQueryModel(batchSize, items);

        String responseAsJson = objectMapper.writeValueAsString(resp);

        new MockServerClient(host, port)
            .when(request()
                .withMethod("GET")
                .withPath("/search/commits")
                .withQueryStringParameter("q", String.format("author:%s.*", author)), exactly(1))
            .respond(response()
                .withStatusCode(200)
                .withHeader(new Header("Content-Type", "application/json; charset=utf-8"))
                .withBody(responseAsJson)
            );
    }

    private SearchCommitsQueryItem createExpectedResponseForItemLanguage(final KafkaCommitRecord commit)
            throws JsonProcessingException {

        final Map<String, Long> languages = new HashMap<>();
        languages.put("Unknown language", 0L);
        languages.put(commit.getLanguage(), 10_000L);

        new MockServerClient(host, port)
            .when(request()
                .withMethod("GET")
                .withPath(String.format("/repo/languages/%s", commit.getLanguage())), exactly(1))
            .respond(response()
                .withStatusCode(200)
                .withHeader(new Header("Content-Type", "application/json; charset=utf-8"))
                .withBody(objectMapper.writeValueAsString(languages)));

        return new SearchCommitsQueryItem(
            commit.getSha(),
            new Commit(
                new CommitAuthorInfo(commit.getAuthorLogin(), commit.getCreatedTime()),
                new CommitCommiterInfo(commit.getAuthorName(), commit.getCreatedTime()),
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
                "name",
                "full_name",
                false,
                "desc1",
                String.format("http://%s:%d/repo/languages/%s", host, port, commit.getLanguage())
            ),
            commit.getLanguage()
        );
    }

    void stopServer() {
        mockServer.stop();
    }
}
