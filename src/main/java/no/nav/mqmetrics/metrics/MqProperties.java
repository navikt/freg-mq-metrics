package no.nav.mqmetrics.metrics;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.net.URI;
import java.util.List;

@ConfigurationProperties(prefix = "mqmetrics")
@Setter
@Getter
public class MqProperties {

    @NotNull
    private List<MqChannel> channels;

    @Setter
    public static class MqChannel {
        @NotNull
        private URI managerUri;
        @NotBlank
        @Getter
        private String channelName;

        public String getQueueManagerName() {
            return this.managerUri.getPath().replace("/", "");
        }

        public int getQueueManagerPort() {
            return this.managerUri.getPort();
        }

        public String getQueueManagerHost() {
            return this.managerUri.getHost();
        }
    }
}
