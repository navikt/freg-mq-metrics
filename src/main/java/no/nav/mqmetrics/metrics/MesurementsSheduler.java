package no.nav.mqmetrics.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;

import static java.time.Duration.ofSeconds;

@Component
public class MesurementsSheduler implements InitializingBean {

    @Autowired
    private MeasurementsService measurementsService;

    @Autowired
    private MeterRegistry registry;

    private Timer schedulerTimer;

    @Scheduled(fixedDelayString = "${MQMETRICS_SCHEDULER_DELAY:300000}")
    public void doUpdate() {
        Timer.Sample sample = Timer.start(registry);
        measurementsService.updateMeasurements();
        sample.stop(schedulerTimer);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        schedulerTimer = Timer.builder("freg.mq.metrics.timed")
                .tag("operation", "updateMesurementsSheduler")
                .publishPercentiles(.25, .50, .75, .90, 1.0)
                .maximumExpectedValue(ofSeconds(10))
                .sla(
                        ofSeconds(1),
                        ofSeconds(2),
                        ofSeconds(3),
                        ofSeconds(4),
                        ofSeconds(5),
                        ofSeconds(6),
                        ofSeconds(7))
                .register(registry);
    }

}
