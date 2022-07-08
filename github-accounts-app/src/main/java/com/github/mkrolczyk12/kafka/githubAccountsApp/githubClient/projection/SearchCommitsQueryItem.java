package com.github.mkrolczyk12.kafka.githubAccountsApp.githubClient.projection;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.mkrolczyk12.kafka.githubAccountsApp.commitsProducer.projection.KafkaCommitRecord;

public class SearchCommitsQueryItem {
    @JsonProperty("sha")
    private String sha;
    @JsonProperty("commit")
    private Commit commit;
    @JsonProperty("author")
    private Author author;
    @JsonProperty("committer")
    private Committer committer;
    @JsonProperty("repository")
    private Repository repository;
    private String language;

    public SearchCommitsQueryItem() {}

    public SearchCommitsQueryItem(final String sha, final Commit commit, final Author author, final Committer committer, final Repository repository, final String language) {
        this.sha = sha;
        this.commit = commit;
        this.author = author;
        this.committer = committer;
        this.repository = repository;
        this.language = language;
    }

    public String getSha() { return sha; }
    public void setSha(final String sha) { this.sha = sha; }

    public Commit getCommit() { return commit; }
    public void setCommit(final Commit commit) { this.commit = commit; }

    public Author getAuthor() { return author; }
    public void setAuthor(final Author author) { this.author = author; }

    public Committer getCommitter() { return committer; }
    public void setCommitter(final Committer committer) { this.committer = committer; }

    public Repository getRepository() { return repository; }
    public void setRepository(final Repository repository) { this.repository = repository; }

    public String getLanguage() { return language; }
    public SearchCommitsQueryItem setLanguage(final String language) {
        this.language = language;
        return this;
    }

    @Override
    public String toString() {
        return "SearchCommitsQueryItem{" +
                "sha='" + sha + '\'' +
                ", commit=" + commit +
                ", author=" + author +
                ", committer=" + committer +
                ", repository=" + repository +
                ", language='" + language + '\'' +
                '}';
    }

    public KafkaCommitRecord toKafkaCommitRecord() {
        return new KafkaCommitRecord
            .KafkaCommitRecordBuilder()
                .sha(sha)
                .authorLogin(author.getLogin())
                .authorName(commit.getAuthor().getName())
                .createdTime(commit.getAuthor().getDate())
                .language(language)
                .message(commit.getMessage())
                .commitRepository(repository.getName())
            .build();
    }
}
