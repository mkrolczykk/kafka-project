package com.github.mkrolczyk12.kafka.githubAccountsApp.githubClient;

import com.github.mkrolczyk12.kafka.githubAccountsApp.commitsProducer.projection.KafkaCommitRecord;
import com.github.mkrolczyk12.kafka.githubAccountsApp.githubClient.projection.SearchCommitsQueryItem;
import com.github.mkrolczyk12.kafka.githubAccountsApp.githubClient.projection.SearchCommitsQueryModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class GithubClientService {
    private static final Logger LOG = LoggerFactory.getLogger(GithubClientService.class);

    private final WebClient githubClient;

    public GithubClientService(final String baseUrl) {
        this.githubClient = WebClient.builder().baseUrl(baseUrl).build();
    }

    public GithubClientService(final WebClient.Builder webClientBuilder, final String baseUrl) {
        this.githubClient = webClientBuilder.baseUrl(baseUrl).build();
    }

    public Flux<KafkaCommitRecord> getUserCommits(final String user, final LocalDateTime sinceDate) {
        String GITHUB_API_HEADER = "application/vnd.github.cloak-preview";
        return githubClient
            .get()
            .uri(uriBuilder ->
                uriBuilder
                    .path("/search/commits")
                    .queryParam("q", String.format(
                            "author:%s+author-date:>%s", user, sinceDate.format(DateTimeFormatter.ISO_DATE_TIME)
                    ))
                    .queryParam("sort", "author-date")
                    .queryParam("order", "desc")
                    .queryParam("page", "1")
                    .build()
            )
            .accept(MediaType.valueOf(GITHUB_API_HEADER))
            .retrieve()
            .bodyToMono(SearchCommitsQueryModel.class)
            .flatMapMany(searchResult -> Flux.fromIterable(searchResult.getItems()))
            .flatMap(item -> getCommitLanguage(item).map(item::setLanguage))
            .map(SearchCommitsQueryItem::toKafkaCommitRecord);
    }

    public Mono<String> getCommitLanguage(final SearchCommitsQueryItem commitData) {
        return WebClient
            .builder()
            .baseUrl(commitData.getRepository().getLanguagesUrl())
            .build()
            .get()
            .exchange()
            .flatMap(r -> {
                if (r.statusCode().is2xxSuccessful()) return r.bodyToMono(new ParameterizedTypeReference<Map<String, Long>>() {});
                else return Mono.just(new HashMap<String, Long>());
            })
            .onErrorResume(e -> {
                LOG.warn("Unexpected error occurred while fetching languages", e);
                return Mono.just(new HashMap<>());
            })
            .map(languages -> {
                if (!languages.isEmpty())
                    return languages.entrySet().stream().max((entry1, entry2) -> entry1.getValue() > entry2.getValue() ? 1 : -1).get().getKey();
                return "Unknown language";
            });
    }
}
