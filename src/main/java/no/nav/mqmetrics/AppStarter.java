package no.nav.mqmetrics;

import no.nav.mqmetrics.config.MqAdminProperties;
import no.nav.mqmetrics.config.SecureMQManagerProperties;
import no.nav.mqmetrics.metrics.MqProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
@EnableConfigurationProperties({MqProperties.class, MqAdminProperties.class, SecureMQManagerProperties.class})
public class AppStarter {

    public static void main(String[] args) {
        SpringApplication.run(AppStarter.class, args).registerShutdownHook();
    }
}
