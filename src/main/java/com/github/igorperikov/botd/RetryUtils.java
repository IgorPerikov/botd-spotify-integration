package com.github.igorperikov.botd;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class RetryUtils {
    private static final int MILLIS_BETWEEN_ATTEMPTS = 25;
    private static final Logger log = LoggerFactory.getLogger(RetryUtils.class);

    public static <T> T execute(Supplier<T> supplier, int attempts) {
        for (int attempt = 1; attempt <= attempts; attempt++) {
            try {
                return supplier.get();
            } catch (RuntimeException e) {
                if (attempt == attempts) {
                    throw e;
                }
                log.warn("Retrying with {}ms interval, attempt={}, total={}", MILLIS_BETWEEN_ATTEMPTS, attempt, attempts);
                try {
                    TimeUnit.MILLISECONDS.sleep(MILLIS_BETWEEN_ATTEMPTS);
                } catch (InterruptedException ignored) {
                }
            }
        }

        throw new RuntimeException("Incorrect implementation of retry library, should be unreachable");
    }
}
