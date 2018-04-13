package no.nav.mqmetrics.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class MesurementsSheduler {

    @Autowired
    private MeasurementsService measurementsService;

    @Autowired
    private MeterRegistry registry;

    @Scheduled(fixedRateString = "${MQMETRICS_SCHEDULER_DELAY:15000}")
    public void doUpdate() {
        Timer.Sample sample = Timer.start(registry);
        measurementsService.updateMeasurements();
        sample.stop(registry.timer("freg.mq.metrics.timed", "operation", "updateMesurementsSheduler"));
    }
}
