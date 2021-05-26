package no.nav.mqmetrics.config;

import com.ibm.mq.MQException;
import no.nav.mqmetrics.service.MQService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MqConfig {

    @Bean
    public MQService mqService() {
        // MQ loogs much crap directly to stderr. Omits this.
        MQException.log = null;
        return new MQService();
    }
}
