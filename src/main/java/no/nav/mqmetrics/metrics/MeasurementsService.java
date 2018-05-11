package no.nav.mqmetrics.metrics;

import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.stream.Collectors.toMap;
import static no.nav.mqmetrics.metrics.MetricsUtils.channelNameTag;
import static no.nav.mqmetrics.metrics.MetricsUtils.nameTag;
import static no.nav.mqmetrics.metrics.MetricsUtils.queueManagerTag;
import static no.nav.mqmetrics.metrics.MetricsUtils.tags;

@Slf4j
@Service
public class MeasurementsService {
    //consider concurrent access
    private Map<String, AtomicInteger> queueDepths = new HashMap<>();

    @Autowired
    private MqProperties mqProperties;
    @Autowired
    private QueueManagerConsumer prober;
    @Autowired
    private MeterRegistry registry;

    public void updateMeasurements() {
        for (MqProperties.Jms manager : mqProperties.getManager()) {
            updateFor(manager);
        }
    }

    public void updateFor(MqProperties.Jms manager) {
        log.info("Querying manager {}",  manager.getQueueManagerName());
        Map<String, AtomicInteger> queueDepths = prober.getQueueDepths(manager)
                .entrySet().stream()
                .filter(e -> 0 <= e.getValue())
                .collect(toMap(Entry::getKey, e -> new AtomicInteger(e.getValue())));

        MapDifference<String, AtomicInteger> difference = Maps.difference(this.queueDepths, queueDepths);
        Map<String, AtomicInteger> newQueues = difference.entriesOnlyOnRight();
        Map<String, AtomicInteger> missingQueues = difference.entriesOnlyOnLeft();
        // AtomicIntegers are never equal so here we get all objects not new or removed.
        Map<String, MapDifference.ValueDifference<AtomicInteger>> updatedQueues = difference.entriesDiffering();
        log.info("Updated queues {}, New queues {}, missing/removed {}", updatedQueues.size(), newQueues.size(), missingQueues.size());

        updatedQueues.forEach((k, diff) -> diff.leftValue().set(diff.rightValue().intValue()));
        missingQueues.forEach((key, value) -> value.set(-1));
        newQueues.forEach((key, value) -> {
            try {
                Gauge.builder("queue.depth", (value), AtomicInteger::get)
                        .baseUnit("messages")
                        .tags(
                                tags(
                                        nameTag(key),
                                        channelNameTag(manager.getChannelName()),
                                        queueManagerTag(manager.getQueueManagerName()),
                                        environmentTag(key)
                                ))
                        .register(registry);
                this.queueDepths.put(key, (value));

            } catch (Exception e) {
                log.warn("Something went wrong trying to update depth of queue '" + key + "'.", e);
            }
        });
    }

    public static Tag environmentTag(String key) {
        return Tag.of("environment", QueueEnvironmentUtils.stripQueueEnvironment(key));
    }


}
