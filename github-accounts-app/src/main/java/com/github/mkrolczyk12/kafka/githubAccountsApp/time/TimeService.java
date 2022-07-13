package com.github.mkrolczyk12.kafka.githubAccountsApp.time;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;

public class TimeService {
    private static final Logger logger = LoggerFactory.getLogger(TimeService.class);

    public LocalDateTime calculateInterval(final Interval range) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());   // set current time

        switch (range) {
            case EIGHT_HOURS:
                cal.add(Calendar.HOUR, -8);                     // last 8 hours
                return convertFromDateToLocalDateTime(cal.getTime());
            case TWELVE_HOURS:
                cal.add(Calendar.HOUR, -12);                    // last 12 hours
                return convertFromDateToLocalDateTime(cal.getTime());
            case DAY:
                cal.add(Calendar.DATE, -1);                     // last day
                return convertFromDateToLocalDateTime(cal.getTime());
            case WEEK:
                cal.add(Calendar.DATE, -7);                     // last week
                return convertFromDateToLocalDateTime(cal.getTime());
            case MONTH:
                cal.add(Calendar.MONTH, -1);                    // last month
                return convertFromDateToLocalDateTime(cal.getTime());
            default:
                cal.add(Calendar.YEAR, 10);                     // if not match, set non existing interval to skip any further actions
                try {
                    throw new UnsupportedIntervalException("UnsupportedIntervalException: Given interval value is not supported!");
                } catch (UnsupportedIntervalException e) {
                    logger.warn(e.getMessage());
                }
                return convertFromDateToLocalDateTime(cal.getTime());
        }
    }

    public static LocalDateTime convertFromDateToLocalDateTime(final Date toConvert) {
        return toConvert
            .toInstant()
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime();
    }
}
