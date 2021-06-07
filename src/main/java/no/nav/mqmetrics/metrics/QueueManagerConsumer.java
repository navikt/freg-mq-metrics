package no.nav.mqmetrics.metrics;

import lombok.extern.slf4j.Slf4j;
import no.nav.mqmetrics.config.ServiceuserProperties;
import no.nav.mqmetrics.exception.MQRuntimeException;
import no.nav.mqmetrics.metrics.MqProperties.MqChannel;
import no.nav.mqmetrics.service.DokQueueStatus;
import no.nav.mqmetrics.service.MQService;
import no.nav.mqmetrics.service.QueueType;
import no.nav.mqmetrics.service.Server;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static no.nav.mqmetrics.service.QueueType.ALIAS;


@Slf4j
@Component
public class QueueManagerConsumer {

    private static final String Q_SECURE_QUEUEMANAGER_NAME = "MQLS01";
    private static final String P_SECURE_QUEUEMANAGER_NAME = "MPLS01";

    @Autowired
    private MQService mqService;

    @Autowired
    private ServiceuserProperties serviceuserProperties;


    public Map<String, Integer> getQueueDepths(MqChannel channel) {

        final String managerName = channel.getQueueManagerName();
        final int port = channel.getQueueManagerPort();
        final String hostName = channel.getQueueManagerHost();
        final String channelName = channel.getChannelName();

        Server server = new Server(hostName, port, channelName, managerName, true);
        if (Q_SECURE_QUEUEMANAGER_NAME.equalsIgnoreCase(managerName) || P_SECURE_QUEUEMANAGER_NAME.equalsIgnoreCase(managerName)) {
            server.setUser(serviceuserProperties.getUsername());
            server.setPassword(serviceuserProperties.getPassword());
        } else {
            server.setMqcsp(false);
            server.setUser("srvappserver");
            server.setPassword("");
        }

        // if list of queues are empty, autodiscover is considered enabled. Duplicates are removed
        server.setQueues(new ArrayList<>(new HashSet<>(channel.getQueueNames())));

        log.debug("Querying {} {} for queue depts", managerName, channelName);
        try {
            List<DokQueueStatus> queueDetails = mqService.getQueueDetails(server, QueueType.getType(ALIAS), true);
            Map<String, Integer> result = queueDetails.stream()
                    .filter(d -> 0 <= d.getDepth())//Negative depths are not accessible, skips them.
                    .collect(Collectors.toMap(a -> MqChannel.ensureQueueAlias(a.getQueueName()), DokQueueStatus::getDepth));
            log.debug("Found {} queuedepths", result.size());
            return result;
        } catch (MQRuntimeException e) {
            log.warn("Not able to fetch queues from " + server, e);
            return Collections.emptyMap();
        }

    }

}
