package no.nav.mqmetrics.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.aspectj.lang.annotation.After;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;

import static java.time.Duration.ofMillis;

@Component
public class MesurementsSheduler implements InitializingBean {

    @Autowired
    private MeasurementsService measurementsService;

    @Autowired
    private MeterRegistry registry;

    private Timer schedulerTimer;

    @Scheduled(fixedRateString = "${MQMETRICS_SCHEDULER_DELAY:15000}")
    public void doUpdate() {
        Timer.Sample sample = Timer.start(registry);
        measurementsService.updateMeasurements();
        sample.stop(schedulerTimer);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        schedulerTimer  = Timer.builder("freg.mq.metrics.timed")
                .tag("operation", "updateMesurementsSheduler")
                .publishPercentiles(.25,.50,.75,.90)
                .maximumExpectedValue(Duration.ofSeconds(5))
                .sla(ofMillis(1000),ofMillis(1500),ofMillis(2000),ofMillis(2500),ofMillis(3000))
                .register(registry);
    }

}
