package org.example;


import org.junit.jupiter.api.Test;
import org.springframework.scheduling.support.CronExpression;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

/**
 * Parse the given
 * <a href="https://www.manpagez.com/man/5/crontab/">crontab expression</a>
 * string into a {@code CronExpression}.
 * The string has six single space-separated time and date fields:
 * <pre>
 * &#9484;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472; second (0-59)
 * &#9474; &#9484;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472; minute (0 - 59)
 * &#9474; &#9474; &#9484;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472; hour (0 - 23)
 * &#9474; &#9474; &#9474; &#9484;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472; day of the month (1 - 31)
 * &#9474; &#9474; &#9474; &#9474; &#9484;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472; month (1 - 12) (or JAN-DEC)
 * &#9474; &#9474; &#9474; &#9474; &#9474; &#9484;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472; day of the week (0 - 7)
 * &#9474; &#9474; &#9474; &#9474; &#9474; &#9474;          (0 or 7 is Sunday, or MON-SUN)
 * &#9474; &#9474; &#9474; &#9474; &#9474; &#9474;
 * &#42; &#42; &#42; &#42; &#42; &#42;
 * </pre>
 *
 * <p>The following rules apply:
 * <ul>
 * <li>
 * A field may be an asterisk ({@code *}), which always stands for
 * "first-last". For the "day of the month" or "day of the week" fields, a
 * question mark ({@code ?}) may be used instead of an asterisk.
 * </li>
 * <li>
 * Ranges of numbers are expressed by two numbers separated with a hyphen
 * ({@code -}). The specified range is inclusive.
 * </li>
 * <li>Following a range (or {@code *}) with {@code /n} specifies
 * the interval of the number's value through the range.
 * </li>
 * <li>
 * English names can also be used for the "month" and "day of week" fields.
 * Use the first three letters of the particular day or month (case does not
 * matter).
 * </li>
 * <li>
 * The "day of month" and "day of week" fields can contain a
 * {@code L}-character, which stands for "last", and has a different meaning
 * in each field:
 * <ul>
 * <li>
 * In the "day of month" field, {@code L} stands for "the last day of the
 * month". If followed by an negative offset (i.e. {@code L-n}), it means
 * "{@code n}th-to-last day of the month". If followed by {@code W} (i.e.
 * {@code LW}), it means "the last weekday of the month".
 * </li>
 * <li>
 * In the "day of week" field, {@code L} stands for "the last day of the
 * week".
 * If prefixed by a number or three-letter name (i.e. {@code dL} or
 * {@code DDDL}), it means "the last day of week {@code d} (or {@code DDD})
 * in the month".
 * </li>
 * </ul>
 * </li>
 * <li>
 * The "day of month" field can be {@code nW}, which stands for "the nearest
 * weekday to day of the month {@code n}".
 * If {@code n} falls on Saturday, this yields the Friday before it.
 * If {@code n} falls on Sunday, this yields the Monday after,
 * which also happens if {@code n} is {@code 1} and falls on a Saturday
 * (i.e. {@code 1W} stands for "the first weekday of the month").
 * </li>
 * <li>
 * The "day of week" field can be {@code d#n} (or {@code DDD#n}), which
 * stands for "the {@code n}-th day of week {@code d} (or {@code DDD}) in
 * the month".
 * </li>
 * </ul>
 *
 * <p>Example expressions:
 * <ul>
 * <li>{@code "0 0 * * * *"} = the top of every hour of every day.</li>
 * <li><code>"*&#47;10 * * * * *"</code> = every ten seconds.</li>
 * <li>{@code "0 0 8-10 * * *"} = 8, 9 and 10 o'clock of every day.</li>
 * <li>{@code "0 0 6,19 * * *"} = 6:00 AM and 7:00 PM every day.</li>
 * <li>{@code "0 0/30 8-10 * * *"} = 8:00, 8:30, 9:00, 9:30, 10:00 and 10:30 every day.</li>
 * <li>{@code "0 0 9-17 * * MON-FRI"} = on the hour nine-to-five weekdays</li>
 * <li>{@code "0 0 0 25 12 ?"} = every Christmas Day at midnight</li>
 * <li>{@code "0 0 0 L * *"} = last day of the month at midnight</li>
 * <li>{@code "0 0 0 L-3 * *"} = third-to-last day of the month at midnight</li>
 * <li>{@code "0 0 0 1W * *"} = first weekday of the month at midnight</li>
 * <li>{@code "0 0 0 LW * *"} = last weekday of the month at midnight</li>
 * <li>{@code "0 0 0 * * 5L"} = last Friday of the month at midnight</li>
 * <li>{@code "0 0 0 * * THUL"} = last Thursday of the month at midnight</li>
 * <li>{@code "0 0 0 ? * 5#2"} = the second Friday in the month at midnight</li>
 * <li>{@code "0 0 0 ? * MON#1"} = the first Monday in the month at midnight</li>
 * </ul>
 *
 * <p>The following macros are also supported:
 * <ul>
 * <li>{@code "@yearly"} (or {@code "@annually"}) to run un once a year, i.e. {@code "0 0 0 1 1 *"},</li>
 * <li>{@code "@monthly"} to run once a month, i.e. {@code "0 0 0 1 * *"},</li>
 * <li>{@code "@weekly"} to run once a week, i.e. {@code "0 0 0 * * 0"},</li>
 * <li>{@code "@daily"} (or {@code "@midnight"}) to run once a day, i.e. {@code "0 0 0 * * *"},</li>
 * <li>{@code "@hourly"} to run once an hour, i.e. {@code "0 0 * * * *"}.</li>
 * </ul>
 */
@SuppressWarnings("NonAsciiCharacters")
public class CronUttrykkTest {
    private final ZoneId tidssone = ZoneId.of("Europe/Berlin");

    // hvrodan bruke: https://spring.io/blog/2020/11/10/new-in-spring-5-3-improved-cron-expressions#new-features
    @Test
    void testCron() {
        //String cronExpression = "0 0/15 * * * *"; // 15. minutt
        //String cronExpression = "0 0/10 * ? * *"; // 10. minutt
        String cronExpression = "0 2/5 0-23 * * *"; //  - hvert annet minutt, fem minutt intervall : 2022-12-12T08:17,  2022-12-12T08:22
        int MAKS_KJØRINGER = 15;

        CronExpression generator = CronExpression.parse(cronExpression);
        LocalDateTime nå = LocalDateTime.now(tidssone);

        for (int i = 0; i < MAKS_KJØRINGER; i++) {
            LocalDateTime nesteKjøring = generator.next(requireNonNull(nå));
            System.out.println(nesteKjøring);
            nå = nesteKjøring;
        }
    }
}
