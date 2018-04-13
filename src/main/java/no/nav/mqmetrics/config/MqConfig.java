package no.nav.mqmetrics.config;

import no.nav.emottak.mq.MQService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MqConfig {

    @Bean
    public MQService mqService() {
        return new MQService();
    }
}
