package no.nav.mqmetrics.metrics;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.net.URI;
import java.util.List;

@ConfigurationProperties(prefix = "mqmetrics.jms")
@Setter
@Getter
public class MqProperties {

    @NotNull
    private List<Jms> manager;

    @Setter
    public static class Jms {
        @NotNull
        private URI uri;
        @NotBlank
        @Getter
        private String channelName;

        public String getQueueManagerName() {
            return this.uri.getPath().replace("/", "");
        }

        public int getQueueManagerPort() {
            return this.uri.getPort();
        }

        public String getQueueManagerHost() {
            return this.uri.getHost();
        }
    }
}
