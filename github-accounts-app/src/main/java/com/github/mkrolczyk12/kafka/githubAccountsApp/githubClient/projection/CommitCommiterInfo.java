package com.github.mkrolczyk12.kafka.githubAccountsApp.githubClient.projection;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.ZonedDateTime;

class CommitCommiterInfo {
    @JsonProperty("name")
    private String name;
    @JsonProperty("date")
    private ZonedDateTime date;

    public CommitCommiterInfo() {
    }

    CommitCommiterInfo(final ZonedDateTime date) {
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
        return "CommitCommiterInfo{" +
                "name='" + name + '\'' +
                ", date=" + date +
                '}';
    }
}
