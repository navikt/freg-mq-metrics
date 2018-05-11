package no.nav.mqmetrics.metrics;

import lombok.extern.slf4j.Slf4j;
import no.nav.emottak.mq.MQService;
import no.nav.emottak.mq.QueueDetails;
import no.nav.emottak.mq.QueueType;
import no.nav.emottak.mq.Server;
import no.nav.mqmetrics.metrics.MqProperties.MqChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;
import static no.nav.emottak.mq.QueueType.ALIAS;

@Slf4j
@Component
public class QueueManagerConsumer {

    @Autowired
    private MQService mqService;

    public Map<String, Integer> getQueueDepths(MqChannel channel) {

        final String managerName = channel.getQueueManagerName();
        final int port = channel.getQueueManagerPort();
        final String hostName = channel.getQueueManagerHost();
        final String channelName = channel.getChannelName();

        Server server = new Server(hostName, port, channelName, managerName);
        server.setUser("srvappserver");
        server.setPassword("");

        QueueType queueType = ALIAS;
        log.info("Querying {} {} for queue depts", managerName, channelName);

        List<QueueDetails> queueDetails = mqService.getQueueDetails(server, QueueType.getType(queueType), 0);
        Map<String, Integer> result = queueDetails.stream()
                .filter(d -> 0 <= d.getDepth())//Negative depths are not accessible, skips them.
                .collect(Collectors.toMap(a -> a.getQueueName().trim(), QueueDetails::getDepth));
        log.debug("Found {} queuedepths", result.size());
        return result;
    }
}
