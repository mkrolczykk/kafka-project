package com.github.mkrolczyk12.kafka.githubAccountsApp.time;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

class TimeServiceTest {
    private final TimeService timeService = new TimeService();
    private static final long MAX_DIFF_IN_MILLIS = 100L;   // 100 ms

    @Test
    @DisplayName("Should return month interval")
    void calculateInterval_month() {
        final LocalDateTime expectedTime = LocalDateTime.now().minusMonths(1);
        assertThat(ChronoUnit.MILLIS.between(expectedTime, timeService.calculateInterval(Interval.MONTH)))
            .isBetween(0L, MAX_DIFF_IN_MILLIS);
    }

    @Test
    @DisplayName("Should return week interval")
    void calculateInterval_week() {
        final LocalDateTime expectedTime = LocalDateTime.now().minusDays(7);
        assertThat(ChronoUnit.MILLIS.between(expectedTime, timeService.calculateInterval(Interval.WEEK)))
            .isBetween(0L, MAX_DIFF_IN_MILLIS);
    }

    @Test
    @DisplayName("Should return day interval")
    void calculateInterval_day() {
        final LocalDateTime expectedTime = LocalDateTime.now().minusDays(1);
        assertThat(ChronoUnit.MILLIS.between(expectedTime, timeService.calculateInterval(Interval.DAY)))
            .isBetween(0L, MAX_DIFF_IN_MILLIS);
    }

    @Test
    @DisplayName("Should return 12h interval")
    void calculateInterval_12hours() {
        final LocalDateTime expectedTime = LocalDateTime.now().minusHours(12);
        assertThat(ChronoUnit.MILLIS.between(expectedTime, timeService.calculateInterval(Interval.TWELVE_HOURS)))
            .isBetween(0L, MAX_DIFF_IN_MILLIS);
    }

    @Test
    @DisplayName("Should return 8h interval")
    void calculateInterval_8hours() {
        final LocalDateTime expectedTime = LocalDateTime.now().minusHours(8);
        assertThat(ChronoUnit.MILLIS.between(expectedTime, timeService.calculateInterval(Interval.EIGHT_HOURS)))
            .isBetween(0L, MAX_DIFF_IN_MILLIS);
    }

    @Test
    @DisplayName("Should return non existing interval because of unsupported interval value")
    void calculateInterval_throwsUnsupportedIntervalException() {
        final LocalDateTime currentTime = LocalDateTime.now();
        assertThat(timeService.calculateInterval(Interval.valueOfLabel("wrong_interval")))
            .isAfter(currentTime);
    }
}