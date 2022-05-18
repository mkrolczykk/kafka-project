package com.github.mkrolczyk12.kafka.githubAccountsApp.commitsProducer.projection;

import java.time.ZonedDateTime;
import java.util.Objects;

public class KafkaCommitRecord {
    private final String sha;
    private final String authorName;    // optional
    private final String authorLogin;
    private final ZonedDateTime createdTime;
    private final String language;
    private final String message;
    private final String commitRepository;

    private KafkaCommitRecord(KafkaCommitRecordBuilder builder) {
        this.sha = builder.sha;
        this.authorName = builder.authorName;
        this.authorLogin = builder.authorLogin;
        this.createdTime = builder.createdTime;
        this.language = builder.language;
        this.message = builder.message;
        this.commitRepository = builder.commitRepository;
    }

    public String getSha() { return sha; }

    public String getAuthorName() { return authorName; }

    public String getAuthorLogin() { return authorLogin; }

    public ZonedDateTime getCreatedTime() { return createdTime; }

    public String getLanguage() { return language; }

    public String getMessage() { return message; }

    public String getCommitRepository() { return commitRepository; }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final KafkaCommitRecord that = (KafkaCommitRecord) o;

        if (!sha.equals(that.sha)) return false;
        if (!authorName.equals(that.authorName)) return false;
        if (!authorLogin.equals(that.authorLogin)) return false;
        if (!createdTime.equals(that.createdTime)) return false;
        if (!language.equals(that.language)) return false;
        if (!Objects.equals(message, that.message)) return false;
        return commitRepository.equals(that.commitRepository);
    }

    @Override
    public String toString() {
        return "KafkaCommitRecord{" +
                "sha='" + sha + '\'' +
                ", authorName='" + authorName + '\'' +
                ", authorLogin='" + authorLogin + '\'' +
                ", createdTime=" + createdTime +
                ", language='" + language + '\'' +
                ", message='" + message + '\'' +
                ", commitRepository='" + commitRepository + '\'' +
                '}';
    }

    public static class KafkaCommitRecordBuilder {
        private String sha;
        private String authorLogin;
        private String authorName;
        private ZonedDateTime createdTime;
        private String language;
        private String message;
        private String commitRepository;

        public KafkaCommitRecordBuilder sha(final String sha) {
            this.sha = sha;
            return this;
        }

        public KafkaCommitRecordBuilder authorLogin(final String authorLogin) {
            this.authorLogin = authorLogin;
            return this;
        }

        public KafkaCommitRecordBuilder authorName(final String authorName) {
            this.authorName = authorName;
            return this;
        }

        public KafkaCommitRecordBuilder createdTime(final ZonedDateTime createdTime) {
            this.createdTime = createdTime;
            return this;
        }

        public KafkaCommitRecordBuilder language(final String language) {
            this.language = language;
            return this;
        }

        public KafkaCommitRecordBuilder message(final String message) {
            this.message = message;
            return this;
        }

        public KafkaCommitRecordBuilder commitRepository(final String commitRepository) {
            this.commitRepository = commitRepository;
            return this;
        }

        public KafkaCommitRecord build() {
            return new KafkaCommitRecord(this);
        }
    }
}
