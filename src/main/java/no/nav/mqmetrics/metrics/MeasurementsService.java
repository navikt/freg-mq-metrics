package no.nav.mqmetrics.metrics;

import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import lombok.extern.slf4j.Slf4j;
import no.nav.mqmetrics.metrics.MqProperties.MqChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.stream.Collectors.toMap;
import static no.nav.mqmetrics.metrics.MetricsUtils.channelNameTag;
import static no.nav.mqmetrics.metrics.MetricsUtils.nameTag;
import static no.nav.mqmetrics.metrics.MetricsUtils.queueManagerTag;
import static no.nav.mqmetrics.metrics.MetricsUtils.tags;
import static no.nav.mqmetrics.metrics.QueueEnvironmentUtils.stripEnvironmentNameFrom;

@Slf4j
@Service
public class MeasurementsService {
    //consider concurrent access
    private Map<QueueAndManager, AtomicInteger> queueDepths = new HashMap<>();

    @Autowired
    private MqProperties mqProperties;
    @Autowired
    private QueueManagerConsumer prober;
    @Autowired
    private MeterRegistry registry;

    public void updateMeasurements() {
        for (MqChannel channel : mqProperties.getChannels()) {
            updateFor(channel);
        }
    }

    public void updateFor(MqChannel channel) {
        String queueManagerName = channel.getQueueManagerName();
        log.debug("Querying manager {}", queueManagerName);
        Map<QueueAndManager, AtomicInteger> newQueueDepths = mapToQueueAndManagerMap(queueManagerName, prober.getQueueDepths(channel));

        MapDifference<QueueAndManager, AtomicInteger> difference = Maps.difference(getCachedQueuesForManager(queueManagerName), newQueueDepths);
        Map<QueueAndManager, AtomicInteger> newQueues = difference.entriesOnlyOnRight();
        Map<QueueAndManager, AtomicInteger> missingQueues = difference.entriesOnlyOnLeft();
        // AtomicIntegers are never equal so here we get all objects not new or removed.
        Map<QueueAndManager, MapDifference.ValueDifference<AtomicInteger>> updatedQueues = difference.entriesDiffering();
        log.debug("Updated queues {}, New queues {}, missing/removed {}", updatedQueues.size(), newQueues.size(), missingQueues.size());

        updatedQueues.forEach((queueAndManager, diff) -> diff.leftValue().set(diff.rightValue().intValue()));
        missingQueues.forEach((queueAndManager, value) -> value.set(-1));
        newQueues.forEach((queueAndManager, depth) -> {
            try {
                Gauge.builder("queue.depth", (depth), AtomicInteger::get)
                        .baseUnit("messages")
                        .tags(
                                tags(
                                        nameTag(queueAndManager.getQueue()),
                                        channelNameTag(channel.getChannelName()),
                                        queueManagerTag(channel.getQueueManagerName()),
                                        environmentTag(queueAndManager.getQueue())
                                ))
                        .register(registry);
                this.queueDepths.put(queueAndManager, (depth));

            } catch (Exception e) {
                log.warn("Something went wrong trying to update depth of queue '" + queueAndManager + "'.", e);
            }
        });
    }

    public Map<QueueAndManager, AtomicInteger> mapToQueueAndManagerMap(String queueManagerName, Map<String, Integer> probedDepths) {
        return probedDepths
                .entrySet().stream()
                .filter(e -> 0 <= e.getValue())
                .collect(toMap(e -> new QueueAndManager(e.getKey(), queueManagerName), e -> new AtomicInteger(e.getValue())));
    }

    public Map<QueueAndManager, AtomicInteger> getCachedQueuesForManager(String queueManagerName) {
        return this.queueDepths.entrySet()
                .stream()
                .filter(entry -> entry.getKey().getManager().equals(queueManagerName))
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public static Tag environmentTag(String queueName) {
        return Tag.of("environment", stripEnvironmentNameFrom(queueName));
    }


}
