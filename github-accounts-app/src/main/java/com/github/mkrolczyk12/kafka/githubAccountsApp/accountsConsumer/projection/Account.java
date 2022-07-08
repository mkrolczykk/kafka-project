package com.github.mkrolczyk12.kafka.githubAccountsApp.accountsConsumer.projection;

/*
 * POJO for github account
*/
public class Account {
    private String user;
    private String interval;

    public Account() {}

    public Account(final String user, final String interval) {
        this.user = user;
        this.interval = interval;
    }

    public String getUser() { return user; }
    public void setUser(final String user) { this.user = user; }

    public String getInterval() { return interval; }
    public void setInterval(final String interval) { this.interval = interval; }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final Account account = (Account) o;

        if (!user.equals(account.user)) return false;
        return interval.equals(account.interval);
    }

    @Override
    public String toString() {
        return "Account{" +
                "user='" + user + '\'' +
                ", interval='" + interval + '\'' +
                '}';
    }
}
