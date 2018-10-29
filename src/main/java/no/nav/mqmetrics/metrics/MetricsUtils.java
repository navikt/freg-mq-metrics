package no.nav.mqmetrics.metrics;

import io.micrometer.core.instrument.Tag;
import lombok.experimental.UtilityClass;

import javax.validation.constraints.NotBlank;

import static java.util.Arrays.asList;

@UtilityClass
public class MetricsUtils {
    public static Tag queueManagerTag(String manager) {
        return Tag.of("queueManager", manager);
    }

    public static Iterable<Tag> tags(Tag... tags) {
        return asList(tags);
    }

    public static Tag channelNameTag(String channelName) {
        return Tag.of("channel", channelName);
    }

    public static Tag nameTag(String name) {
        return Tag.of("name", name);
    }
}
