package com.github.mkrolczyk12.kafka.githubAccountsApp.githubClient;

import com.github.mkrolczyk12.kafka.githubAccountsApp.githubClient.projection.SearchCommitsQueryItem;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

public class MockGithubClientService extends GithubClientService {

    public MockGithubClientService(final WebClient.Builder webClientBuilder, String baseUrl) {
        super(webClientBuilder, baseUrl);
    }

    @Override
    public Mono<String> getCommitLanguage(final SearchCommitsQueryItem item) {
        return Mono.just("Scala");
    }
}
