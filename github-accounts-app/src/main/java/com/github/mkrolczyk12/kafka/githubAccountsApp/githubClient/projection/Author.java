package com.github.mkrolczyk12.kafka.githubAccountsApp.githubClient.projection;

import com.fasterxml.jackson.annotation.JsonProperty;

class Author {
    @JsonProperty("login")
    private String login;
    @JsonProperty("id")
    private Integer id;
    @JsonProperty("node_id")
    private String nodeId;
    @JsonProperty("subscriptions_url")
    private String subscriptionsUrl;
    @JsonProperty("repos_url")
    private String reposUrl;
    @JsonProperty("type")
    private String type;
    @JsonProperty("site_admin")
    private Boolean siteAdmin;

    public Author() {}

    Author(final String login, final Integer id, final String nodeId, final String subscriptionsUrl, final String reposUrl, final String type, final Boolean siteAdmin) {
        this.login = login;
        this.id = id;
        this.nodeId = nodeId;
        this.subscriptionsUrl = subscriptionsUrl;
        this.reposUrl = reposUrl;
        this.type = type;
        this.siteAdmin = siteAdmin;
    }

    public String getLogin() { return login; }
    public void setLogin(final String login) { this.login = login; }

    public Integer getId() { return id; }
    public void setId(final Integer id) { this.id = id; }

    public String getNodeId() { return nodeId; }
    public void setNodeId(final String nodeId) { this.nodeId = nodeId; }

    public String getSubscriptionsUrl() { return subscriptionsUrl; }
    public void setSubscriptionsUrl(final String subscriptionsUrl) { this.subscriptionsUrl = subscriptionsUrl; }

    public String getReposUrl() { return reposUrl; }
    public void setReposUrl(final String reposUrl) { this.reposUrl = reposUrl; }

    public String getType() { return type; }
    public void setType(final String type) { this.type = type; }

    public Boolean getSiteAdmin() { return siteAdmin; }
    public void setSiteAdmin(final Boolean siteAdmin) { this.siteAdmin = siteAdmin; }

    @Override
    public String toString() {
        return "Author{" +
                "login='" + login + '\'' +
                ", id=" + id +
                ", nodeId='" + nodeId + '\'' +
                ", subscriptionsUrl='" + subscriptionsUrl + '\'' +
                ", reposUrl='" + reposUrl + '\'' +
                ", type='" + type + '\'' +
                ", siteAdmin=" + siteAdmin +
                '}';
    }
}
