package no.nav.mqmetrics.metrics;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.net.URI;

@ConfigurationProperties(prefix = "mqmetrics.jms")
@Setter
@Getter
public class MqProperties {

    @NotNull
    private Jms manager;

    @Setter
    @Getter
    public static class Jms {
        @NotNull
        private URI uri;
        @NotBlank
        private String channelName;
    }
}
