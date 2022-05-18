package com.github.mkrolczyk12.kafka.metrics.streams.model;

import java.time.ZonedDateTime;
import java.util.Objects;

/** The representation of commit record from github-commits topic */
public class CommitRecord {
    private String sha;
    private String authorName;
    private String authorLogin;
    private ZonedDateTime createdTime;
    private String language;
    private String message;
    private String commitRepository;

    public String getSha() {
        return sha;
    }

    public void setSha(final String sha) {
        this.sha = sha;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(final String authorName) {
        this.authorName = authorName;
    }

    public String getAuthorLogin() {
        return authorLogin;
    }

    public void setAuthorLogin(final String authorLogin) {
        this.authorLogin = authorLogin;
    }

    public ZonedDateTime getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(final ZonedDateTime createdTime) {
        this.createdTime = createdTime;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(final String language) {
        this.language = language;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(final String message) {
        this.message = message;
    }

    public String getCommitRepository() {
        return commitRepository;
    }

    public void setCommitRepository(final String commitRepository) {
        this.commitRepository = commitRepository;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final CommitRecord that = (CommitRecord) o;

        if (!Objects.equals(sha, that.sha)) return false;
        if (!Objects.equals(authorName, that.authorName)) return false;
        if (!Objects.equals(authorLogin, that.authorLogin)) return false;
        if (!Objects.equals(createdTime, that.createdTime)) return false;
        if (!Objects.equals(language, that.language)) return false;
        if (!Objects.equals(message, that.message)) return false;
        return Objects.equals(commitRepository, that.commitRepository);
    }

    @Override
    public String toString() {
        return "CommitRecord{" +
                "sha='" + sha + '\'' +
                ", authorName='" + authorName + '\'' +
                ", authorLogin='" + authorLogin + '\'' +
                ", createdTime=" + createdTime +
                ", language='" + language + '\'' +
                ", message='" + message + '\'' +
                ", commitRepository='" + commitRepository + '\'' +
                '}';
    }
}
