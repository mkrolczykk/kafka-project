package com.github.mkrolczyk12.kafka.githubAccountsApp.githubClient.projection;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Repository {
    @JsonProperty("id")
    private Integer id;
    @JsonProperty("node_id")
    private String nodeId;
    @JsonProperty("name")
    private String name;
    @JsonProperty("full_name")
    private String fullName;
    @JsonProperty("private")
    private Boolean isPrivate;
    @JsonProperty("description")
    private String description;
    @JsonProperty("languages_url")
    private String languagesUrl;

    public Repository() {}

    Repository(final Integer id, final String nodeId, final String name, final String fullName, final Boolean isPrivate, final String description, final String languagesUrl) {
        this.id = id;
        this.nodeId = nodeId;
        this.name = name;
        this.fullName = fullName;
        this.isPrivate = isPrivate;
        this.description = description;
        this.languagesUrl = languagesUrl;
    }

    public Integer getId() { return id; }
    public void setId(final Integer id) { this.id = id; }

    public String getNodeId() { return nodeId; }
    public void setNodeId(final String nodeId) { this.nodeId = nodeId; }

    public String getName() { return name; }
    public void setName(final String name) { this.name = name; }

    public String getFullName() { return fullName; }
    public void setFullName(final String fullName) { this.fullName = fullName; }

    public Boolean getPrivate() { return isPrivate; }
    public void setPrivate(final Boolean aPrivate) { isPrivate = aPrivate; }

    public String getDescription() { return description; }
    public void setDescription(final String description) { this.description = description; }

    public String getLanguagesUrl() { return languagesUrl; }
    public void setLanguagesUrl(final String languagesUrl) { this.languagesUrl = languagesUrl; }

    @Override
    public String toString() {
        return "Repository{" +
                "id=" + id +
                ", nodeId='" + nodeId + '\'' +
                ", name='" + name + '\'' +
                ", fullName='" + fullName + '\'' +
                ", isPrivate=" + isPrivate +
                ", description='" + description + '\'' +
                ", languagesUrl='" + languagesUrl + '\'' +
                '}';
    }
}
