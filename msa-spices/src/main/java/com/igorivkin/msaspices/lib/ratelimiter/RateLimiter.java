package com.igorivkin.msaspices.lib.ratelimiter;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

/**
 * Defines rate limiting. Rate limiting allows to run something for the only defined times
 * per period, for example not more than 5 times per second.
 * This rate limiter is thread-safe in case if you use the same instance of rate limiter
 * in many threads. For Spring installations I recommend to autowire it as "Bean".
 */
public class RateLimiter {

    private final Long periodInMillis;

    private final Integer rateValue;

    private final Semaphore rateLimitingSemaphore;

    public RateLimiter(Integer rateValue, Long periodInMillis) {
        this.rateValue = rateValue;
        this.periodInMillis = periodInMillis;
        this.rateLimitingSemaphore = new Semaphore(rateValue);

        runRateResetThread();
    }

    /**
     * Acquire a given number of runs.
     * In case if they are exceeded for a given period will block until limit will be reset.
     *
     * @param takeRuns runs to acquire
     */
    public void acquire(Integer takeRuns) {
        try {
            rateLimitingSemaphore.acquire(takeRuns);
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * A static fabric method allows to create a rate limiter using given rate value and period in milliseconds.
     *
     * @param rateValue      rate value in times
     * @param periodInMillis period in milliseconds
     * @return rate limiter instance
     */
    public static RateLimiter ofRate(Integer rateValue, Long periodInMillis) {
        return new RateLimiter(rateValue, periodInMillis);
    }

    /**
     * A static fabric method allows to create a rate limiter using given rate value.
     * Takes period of one second (1000 milliseconds) by default.
     *
     * @param rateValue      rate value in times
     * @return rate limiter instance
     */
    public static RateLimiter ofRate(Integer rateValue) {
        return ofRate(rateValue, 1000L);
    }

    private void runRateResetThread() {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(new RateReset());
        executorService.shutdown();
    }


    /**
     * A helper class used to reset currently tracked values.
     * Resets count of runs and tracked time to compare with.
     */
    private class RateReset implements Runnable {

        @SuppressWarnings({"BusyWait"})
        @Override
        public void run() {
            try {
                // Runs an infinite loop to reset tracked values in a defined scheduler
                Thread currentThread = Thread.currentThread();
                while (!currentThread.isInterrupted()) {
                    Thread.sleep(periodInMillis);
                    rateLimitingSemaphore.release(rateValue);
                }
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}
