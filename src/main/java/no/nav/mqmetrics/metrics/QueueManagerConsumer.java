package no.nav.mqmetrics.metrics;

import com.ibm.mq.constants.MQConstants;
import com.ibm.mq.jms.MQConnectionFactory;
import com.ibm.msg.client.jms.JmsConstants;
import com.ibm.msg.client.wmq.WMQConstants;
import lombok.extern.slf4j.Slf4j;
import no.nav.mqmetrics.config.ServiceuserProperties;
import no.nav.mqmetrics.exception.MQRuntimeException;
import no.nav.mqmetrics.metrics.MqProperties.MqChannel;
import no.nav.mqmetrics.service.MQService;
import no.nav.mqmetrics.service.QueueDetails;
import no.nav.mqmetrics.service.QueueType;
import no.nav.mqmetrics.service.Server;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.connection.UserCredentialsConnectionFactoryAdapter;
import org.springframework.stereotype.Component;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
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

        QueueType queueType = ALIAS;
        log.debug("Querying {} {} for queue depts", managerName, channelName);
        try {
            List<QueueDetails> queueDetails = mqService.getQueueDetails(server, QueueType.getType(queueType), 0);
            Map<String, Integer> result = queueDetails.stream()
                    .filter(d -> 0 <= d.getDepth())//Negative depths are not accessible, skips them.
                    .collect(Collectors.toMap(a -> a.getQueueName().trim(), QueueDetails::getDepth));
            log.debug("Found {} queuedepths", result.size());
            return result;
        } catch (MQRuntimeException e) {
            log.warn("Not able to fetch queues from " + server, e);
            return Collections.emptyMap();
        }

    }

    private ConnectionFactory createConnectionFactory(Server server) throws JMSException {
        MQConnectionFactory connectionFactory = new MQConnectionFactory();
        connectionFactory.setHostName(server.getHost());
        connectionFactory.setPort(server.getPort());
        connectionFactory.setChannel(server.getChannel());
        connectionFactory.setQueueManager(server.getQueueManagerName());
        connectionFactory.setTransportType(WMQConstants.WMQ_CM_CLIENT);
        connectionFactory.setCCSID(server.getCcsid());
        connectionFactory.setIntProperty(WMQConstants.JMS_IBM_ENCODING, MQConstants.MQENC_NATIVE);
        UserCredentialsConnectionFactoryAdapter adapter = new UserCredentialsConnectionFactoryAdapter();
        adapter.setTargetConnectionFactory(connectionFactory);

        if (server.getQueueManagerName().equalsIgnoreCase("MQLS01")) {
            // Konfigurasjon for IBM MQ broker med TLS og autorisasjon med serviceuser mot onpremise Active Directory.
            adapter.setUsername(serviceuserProperties.getUsername());
            adapter.setPassword(serviceuserProperties.getPassword());
        } else {
            // Legacy IBM MQ broker
            connectionFactory.setBooleanProperty(JmsConstants.USER_AUTHENTICATION_MQCSP, false);
            adapter.setUsername("srvappserver");
            adapter.setPassword("");
        }
        return adapter;
    }


}
