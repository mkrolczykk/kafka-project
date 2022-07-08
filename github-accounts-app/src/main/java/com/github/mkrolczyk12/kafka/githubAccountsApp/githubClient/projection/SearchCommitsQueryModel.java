package com.github.mkrolczyk12.kafka.githubAccountsApp.githubClient.projection;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SearchCommitsQueryModel {
    @JsonProperty("total_count")
    private Integer totalCount;
    @JsonProperty("items")
    private List<SearchCommitsQueryItem> items;

    public SearchCommitsQueryModel() {}

    public SearchCommitsQueryModel(final Integer totalCount, final List<SearchCommitsQueryItem> items) {
        this.totalCount = totalCount;
        this.items = items;
    }

    public Integer getTotalCount() { return totalCount; }
    public void setTotalCount(final Integer totalCount) { this.totalCount = totalCount; }

    public List<SearchCommitsQueryItem> getItems() {return items;}
    public void setItems(final List<SearchCommitsQueryItem> items) { this.items = items; }

    @Override
    public String toString() {
        return "SearchCommitsQueryModel{" +
                "totalCount=" + totalCount +
                ", items=" + items +
                '}';
    }
}
