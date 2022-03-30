package io.github.githubAccountsApp.githubClient.projection;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

class CommitAuthorInfo {
    @JsonProperty("name")
    private String name;
    @JsonProperty("date")
    private ZonedDateTime date;

    public CommitAuthorInfo() {
    }

    CommitAuthorInfo(final ZonedDateTime date) {
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public ZonedDateTime getDate() {
        return date;
    }

    public void setDate(final ZonedDateTime date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "CommitAuthorInfo{" +
                "name='" + name + '\'' +
                ", date=" + date +
                '}';
    }
}
