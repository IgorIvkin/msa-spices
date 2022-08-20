package com.igorivkin.msaspices.lib.ratelimiter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@DisplayName("Unit-tests of rate limiter")
public class RateLimiterTests {

    @Mock
    private SomeTask task;

    @ParameterizedTest
    @ValueSource(ints = {1, 5, 10, 20})
    @DisplayName("Check rate limiter per second - different rates")
    public void checkRateLimiterPerSecond(int rate) {
        RateLimiter rateLimiter = RateLimiter.ofRate(rate);

        Instant start = Instant.now();
        for (int i = 1; i < 100; i++) {
            rateLimiter.acquire(1);

            Instant end = Instant.now();
            if (ChronoUnit.MILLIS.between(start, end) >= 4000L) {
                break;
            }

            task.doSomething();

            end = Instant.now();
            if (ChronoUnit.MILLIS.between(start, end) >= 4000L) {
                break;
            }
        }

        verify(task, times(4 * rate)).doSomething();
    }

    @Test
    @DisplayName("Check rate limiter per period of time - rate 4 seconds")
    public void checkRateLimiterPerPeriodOfTime() {
        RateLimiter rateLimiter = RateLimiter.ofRate(4, 2000L);

        Instant start = Instant.now();
        for (int i = 1; i < 100; i++) {
            rateLimiter.acquire(1);

            Instant end = Instant.now();
            if (ChronoUnit.MILLIS.between(start, end) >= 4000L) {
                break;
            }

            task.doSomething();

            end = Instant.now();
            if (ChronoUnit.MILLIS.between(start, end) >= 4000L) {
                break;
            }
        }

        verify(task, times(8)).doSomething();
    }

    private static class SomeTask {

        public void doSomething() {
            // Do nothing here
        }
    }
}
