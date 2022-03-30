package io.github.githubAccountsApp.githubClient.projection;

class Commit {
    private CommitAuthorInfo author;
    private CommitCommiterInfo committer;
    private String message;

    public Commit() {}

    Commit(final CommitAuthorInfo author, final CommitCommiterInfo committer, final String message) {
        this.author = author;
        this.committer = committer;
        this.message = message;
    }

    public CommitAuthorInfo getAuthor() { return author; }
    public void setAuthor(final CommitAuthorInfo author) { this.author = author; }

    public CommitCommiterInfo getCommitter() { return committer; }
    public void setCommitter(final CommitCommiterInfo committer) { this.committer = committer; }

    public String getMessage() { return message; }
    public void setMessage(final String message) { this.message = message; }

    @Override
    public String toString() {
        return "Commit{" +
                "author=" + author +
                ", committer=" + committer +
                ", message='" + message + '\'' +
                '}';
    }
}
