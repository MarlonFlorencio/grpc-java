package com.github.marlonflorencio.grpc.util;

import java.time.Duration;
import java.time.Instant;

public class TimeUtil {

    public static void timeElapsed(Instant start) {
        Instant end = Instant.now();
        Duration timeElapsed = Duration.between(start, end);
        System.out.printf("Execution in %s millis%n", timeElapsed.toMillis());
    }

    public static void timeElapsedNano(Instant start) {
        Instant end = Instant.now();
        Duration timeElapsed = Duration.between(start, end);
        System.out.printf("Execution in %s nano \n", timeElapsed.toNanos());
    }
}
