package no.nav.mqmetrics.metrics;

import lombok.experimental.UtilityClass;

import java.util.Arrays;

@UtilityClass
public class QueueEnvironmentUtils {
    public static String stripQueueEnvironment(String input) {
        String first = input.replaceFirst("QA.", "");
        return Arrays.stream(first.split("_"))
                .findFirst()
                .filter(s-> s.matches("[A-Z]{1,2}[0-9]{0,2}"))
                .orElse("UNKNOWN");
    }
}
