package com.github.mkrolczyk12.kafka.githubAccountsApp.time;

public enum Interval {
    /*
     * Available intervals
     */
    EIGHT_HOURS("8h"),
    TWELVE_HOURS("12h"),
    DAY("1d"),
    WEEK("1w"),
    MONTH("1m"),
    DEFAULT("unknown");

    private final String label;

    Interval(final String label) {
        this.label = label;
    }

    public static Interval valueOfLabel(final String label) {
        for (Interval e : values()) if (e.label.equals(label)) return e;
        return Interval.valueOfLabel("unknown");
    }
}
