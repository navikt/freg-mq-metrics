package no.nav.mqmetrics.metrics;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class QueueAndManager {
    private String queue, manager;
}
