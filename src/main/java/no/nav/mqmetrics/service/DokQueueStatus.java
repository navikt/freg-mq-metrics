package no.nav.mqmetrics.service;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DokQueueStatus {

    private String queueName;
    private Integer depth;
}
