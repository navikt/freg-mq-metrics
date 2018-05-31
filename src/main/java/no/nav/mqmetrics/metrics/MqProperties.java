package no.nav.mqmetrics.metrics;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;

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

        private List<String> queueNames = new ArrayList<>();

        private static String ensureQueueAlias(String queueName) {
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

        public void setQueueNames(List<String> queueNames) {
            this.queueNames = queueNames;
        }
    }
}
