package io.github.githubAccountsApp.githubClient;

import io.github.githubAccountsApp.commitsProducer.projection.KafkaCommitRecord;
import io.github.githubAccountsApp.githubClient.projection.SearchCommitsQueryItem;
import io.github.githubAccountsApp.githubClient.projection.SearchCommitsQueryModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class GithubClientService {
    private final WebClient githubClient;
    private final String GITHUB_API_HEADER = "application/vnd.github.cloak-preview";

    private static final Logger logger = LoggerFactory.getLogger(GithubClientService.class);

    public GithubClientService(final String baseUrl) {
        this.githubClient = WebClient.builder().baseUrl(baseUrl).build();
    }

    public Flux<KafkaCommitRecord> getUserCommits(final String user, final LocalDateTime sinceDate) {
        return githubClient
            .get()
            .uri(uriBuilder ->
                uriBuilder
                    .path("search/commits")
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

    private Mono<String> getCommitLanguage(final SearchCommitsQueryItem commitData) {
        return WebClient
            .builder()
            .baseUrl(commitData.getRepository().getLanguagesUrl())
            .build()
            .get()
            .accept(MediaType.valueOf(GITHUB_API_HEADER))
            .exchangeToMono(response -> {
                if (response.statusCode() == HttpStatus.OK) return response.bodyToMono(new ParameterizedTypeReference<Map<String, Long>>() {});
                return Mono.just(new HashMap<String, Long>() {});
            })
            .map(languages -> {
                if (!languages.isEmpty())
                    return languages.entrySet().stream().max((entry1, entry2) -> entry1.getValue() > entry2.getValue() ? 1 : -1).get().getKey();
                return "Unknown language";
            });
    }
}
