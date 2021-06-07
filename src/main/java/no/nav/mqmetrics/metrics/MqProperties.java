package no.nav.mqmetrics.metrics;

import lombok.Data;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

@Getter
@ConfigurationProperties(prefix = "mqmetrics")
public class MqProperties {
    private final Map<String, MqChannel> channels = new HashMap<>();

    @Data
    public static class MqChannel {
        private URI managerUri;
        private String channelName;
        private List<String> queueNames = new ArrayList<>();

        public static String ensureQueueAlias(String queueName) {
            if (queueName.startsWith("QA.")) return queueName;
            else return ("QA." + queueName);
        }

        public String getQueueManagerName() {
            return this.managerUri.getPath().replace("/", "");
        }

        public int getQueueManagerPort() {
            return this.managerUri.getPort();
        }

        public String getQueueManagerHost() {
            return this.managerUri.getHost();
        }

        public List<String> getQueueNames() {
            return queueNames.stream()
                    .map(MqChannel::ensureQueueAlias)
                    .collect(toList());
        }
    }
}
