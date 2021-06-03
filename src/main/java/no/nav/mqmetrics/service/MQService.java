package no.nav.mqmetrics.service;

import com.ibm.mq.MQException;
import com.ibm.mq.MQQueue;
import com.ibm.mq.MQQueueManager;
import com.ibm.mq.constants.MQConstants;
import com.ibm.mq.headers.MQDataException;
import com.ibm.mq.headers.pcf.PCFMessage;
import com.ibm.mq.headers.pcf.PCFMessageAgent;
import com.ibm.msg.client.jms.JmsConstants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.ibm.mq.constants.CMQC.MQCA_Q_NAME;
import static com.ibm.mq.constants.CMQC.MQIA_CURRENT_Q_DEPTH;
import static com.ibm.mq.constants.CMQC.MQIA_Q_TYPE;
import static com.ibm.mq.constants.CMQC.PASSWORD_PROPERTY;
import static com.ibm.mq.constants.CMQC.PORT_PROPERTY;
import static com.ibm.mq.constants.CMQC.USER_ID_PROPERTY;
import static com.ibm.mq.constants.CMQCFC.MQCMD_INQUIRE_Q_STATUS;

@Slf4j
@Component
public class MQService {

    public MQService() {
    }

    @SuppressWarnings(
            value = {"REC_CATCH_EXCEPTION"}
    )
    public List<DokQueueStatus> getQueueDetails(Server server, int type, boolean disconnect) {
        PCFMessageAgent agent = null;
        PCFMessage request = new PCFMessage(MQCMD_INQUIRE_Q_STATUS);
        request.addParameter(MQCA_Q_NAME, "*");
        request.addParameter(MQIA_Q_TYPE, type);
        List<DokQueueStatus> queueStatus = new ArrayList<>();
        MQQueueManager queueManager = null;


        try {
            queueManager = this.getQueueManager(server);
            agent = new PCFMessageAgent(queueManager);
            PCFMessage[] responses = agent.send(request);

            queueStatus = Arrays.stream(responses).filter(Objects::nonNull)
                    .filter(pcfMessage -> !Pattern.matches("^SYSTEM.*$", String.valueOf(pcfMessage.getParameterValue(MQCA_Q_NAME)).trim()))
                    .filter(pcfMessage -> !Pattern.matches("^AMK.*$", String.valueOf(pcfMessage.getParameterValue(MQCA_Q_NAME)).trim()))
                    .map(pcfMessage ->
                            DokQueueStatus.builder().queueName(String.valueOf(pcfMessage.getParameterValue(MQCA_Q_NAME)).trim())
                                    .depth((Integer) pcfMessage.getParameterValue(MQIA_CURRENT_Q_DEPTH))
                                    .build()
                    ).collect(Collectors.toList());

        } catch (MQDataException var14) {
            log.info("failed to autodiscover queues of type {} , Reason:{} ", type, var14.reasonCode);
        } catch (IOException | MQException e) {
            log.info("failed to autodiscover queues of type {} , Reason:{} ", type, e.getCause());
        } finally {
            closeQuietly(agent);
            if (disconnect) {
                this.closeQuietly(queueManager);
            }
        }
        return queueStatus;
    }


    private void closeQuietly(PCFMessageAgent agent) {
        if (agent != null) {
            try {
                agent.disconnect();
            } catch (MQDataException var3) {
                log.error("failed to disconnect agent", var3);
            }
        }

    }


    private void closeQuietly(MQQueueManager queueManager) {
        if (queueManager != null) {
            try {
                queueManager.disconnect();
            } catch (MQException var4) {
                log.warn("MQException while disconnecting queueManager", var4);
            }

            try {
                queueManager.close();
            } catch (MQException var3) {
                log.warn("MQException while closing queueManager", var3);
            }
        }

    }

    private void closeQuietly(MQQueue queue) {
        if (queue != null) {
            try {
                queue.close();
            } catch (MQException var3) {
                log.warn("MQException while closing queue", var3);
            }
        }

    }


    private MQQueueManager getQueueManager(Server server) throws MQException {
        Hashtable<String, Object> properties = new Hashtable<>();
        properties.put(PORT_PROPERTY, server.getPort());
        properties.put(MQConstants.HOST_NAME_PROPERTY, server.getHost());
        properties.put(MQConstants.CHANNEL_PROPERTY, server.getChannel());
        properties.put(MQConstants.MQPSC_Q_MGR_NAME, server.getQueueManagerName());
        if (StringUtils.isNotEmpty(server.getUser())) {
            properties.put(MQConstants.USE_MQCSP_AUTHENTICATION_PROPERTY, server.isMqcsp());
            properties.put(JmsConstants.USER_AUTHENTICATION_MQCSP, server.isMqcsp());
            properties.put(USER_ID_PROPERTY, server.getUser());
            properties.put(PASSWORD_PROPERTY, server.getPassword());
        }

        return new MQQueueManager(server.getQueueManagerName(), properties);
    }

}
