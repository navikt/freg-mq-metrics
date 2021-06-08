package no.nav.mqmetrics.service;

import com.ibm.mq.MQException;
import com.ibm.mq.MQQueue;
import com.ibm.mq.MQQueueManager;
import com.ibm.mq.constants.MQConstants;
import com.ibm.mq.headers.MQDataException;
import com.ibm.mq.headers.pcf.PCFException;
import com.ibm.mq.headers.pcf.PCFMessage;
import com.ibm.mq.headers.pcf.PCFMessageAgent;
import lombok.extern.slf4j.Slf4j;
import no.nav.mqmetrics.exception.MQRuntimeException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
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

    private static final String SRVUSER = "srvappserver";
    public MQService() {
    }

    @SuppressWarnings(
            value = {"REC_CATCH_EXCEPTION"}
    )
    public List<DokQueueStatus> getSecureQueueDetails(Server server, int type, boolean disconnect) {
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
                    .filter(pcfMessage -> !Pattern.matches("^AMQ.*$", String.valueOf(pcfMessage.getParameterValue(MQCA_Q_NAME)).trim()))
                    .map(pcfMessage ->
                            DokQueueStatus.builder().queueName(String.valueOf(pcfMessage.getParameterValue(MQCA_Q_NAME)).trim())
                                    .depth((Integer) pcfMessage.getParameterValue(MQIA_CURRENT_Q_DEPTH))
                                    .build()
                    ).collect(Collectors.toList());

        } catch (MQDataException var14) {
            log.info("Feilet til autodiscover køer i queuemanager={} av type {} , Reason:{} ", server.getQueueManagerName(), type, var14.reasonCode);
        } catch (IOException | MQException e) {
            log.info("Feilet til å liste kødetaljer fra queuemanager={} av type {} , Reason:{} ", server.getQueueManagerName(), type, e.getCause());
        } finally {
            closeQuietly(agent);
            if (disconnect) {
                this.closeQuietly(queueManager);
            }
        }
        return queueStatus;
    }


    @SuppressWarnings(
            value = {"REC_CATCH_EXCEPTION"}
    )
    public List<DokQueueStatus> getQueueDetails(Server server, int type, int managerIndex) {
        MQQueueManager queueManager = null;
        List<QueueDetails> list = null;
        String managerName = server.getName();

        try {
            queueManager = this.getQueueManager(server);
            Iterator i$;
            if (server.isAutodiscover()) {
                list = this.autodiscoverQueues(queueManager, type, false, managerName, managerIndex);
            } else {
                list = new ArrayList();
                i$ = server.getQueues().iterator();

                while (i$.hasNext()) {
                    String name = (String) i$.next();
                    list.add(new QueueDetails(name, managerName, managerIndex));
                }
            }

            i$ = ((List) list).iterator();

            while (i$.hasNext()) {
                QueueDetails qd = (QueueDetails) i$.next();
                if (qd.getQueueType() != QueueType.MODEL) {
                    try {
                        MQQueue queue = queueManager.accessQueue(qd.getQueueName(), 8226);
                        this.setQueueDetails(qd, queue, true, queueManager);
                        this.closeQuietly(queue);
                    } catch (MQException var14) {
                        qd.setStatus(var14.getMessage() + " (" + MQUtil.getCompletionCode(var14) + ", " + MQUtil.getReasonCode(var14) + ")");
                    }
                }
            }
        } catch (Exception var15) {
            log.error("Feilet til å liste kødetaljer fra queuemanager={} med feilmelding={}", server.getQueueManagerName(), var15);
            throw new MQRuntimeException("Feilet til å liste kødetaljer fra queuemanager=" + server.getQueueManagerName(), var15);
        } finally {
            this.closeQuietly(queueManager);
        }

        return list.isEmpty() ? Collections.emptyList() : list.stream()
                .map(queueDetail -> DokQueueStatus.builder().queueName(queueDetail.getQueueName()).depth(queueDetail.getDepth()).build())
                .collect(Collectors.toList());
    }


    private void setQueueDetails(QueueDetails queueDetails, MQQueue queue, boolean subtractSelf, MQQueueManager queueManager) throws MQException {
        if (queue.getQueueType() == 1) {
            queueDetails.setDepth(queue.getCurrentDepth());
        }

        if (queue.getQueueType() == 3) {
            MQQueue aliasQueue = queueManager.accessQueue(queue.getResolvedQName(), 8226);
            this.setQueueDetails(queueDetails, aliasQueue, subtractSelf, queueManager);
            queueDetails.setQueueType(QueueType.ALIAS);
            queueDetails.setResolvedQueueName(StringUtils.stripToEmpty(queue.getResolvedQName()));
        } else {
            queueDetails.setMaxDepth(queue.getMaximumDepth());
            queueDetails.setMaxMessageLength(queue.getMaximumMessageLength());
            int cnt = queue.getOpenInputCount();
            if (subtractSelf && cnt > 0) {
                --cnt;
            }

            queueDetails.setOpenInputCount(cnt);
            queueDetails.setOpenOutputCount(queue.getOpenOutputCount());
            queueDetails.setQueueType(QueueType.getQueueType(queue.getQueueType()));
        }

        queueDetails.setDescription(queue.getDescription());
    }

    private List<QueueDetails> autodiscoverQueues(MQQueueManager queueManager, int type, boolean disconnect, String managerName, int managerIndex) {
        PCFMessageAgent agent = null;
        List<QueueDetails> list = new ArrayList<>();

        try {
            agent = new PCFMessageAgent(queueManager);
            if (type == -1) {
                list.addAll(getQueuesByType(agent, 1, managerName, managerIndex));
                list.addAll(getQueuesByType(agent, 3, managerName, managerIndex));
                list.addAll(getQueuesByType(agent, 7, managerName, managerIndex));
                list.addAll(getQueuesByType(agent, 2, managerName, managerIndex));
                list.addAll(getQueuesByType(agent, 6, managerName, managerIndex));
            } else {
                list.addAll(this.getQueuesByType(agent, type, managerName, managerIndex));
            }
        } catch (MQDataException e) {
            log.warn("Feilet til autodiscover køer i queuemanger={} med type={}, reasoncode={}, feilmelding={}", managerName, e.getCompCode(), e.getReason(), e.getMessage(), e);
        } finally {
            this.closeQuietly(agent);
            if (disconnect) {
                this.closeQuietly(queueManager);
            }

        }

        return list;
    }

    private List<QueueDetails> getQueuesByType(PCFMessageAgent agent, int type, String managerName, int managerIndex) {
        PCFMessage request = new PCFMessage(18);
        request.addParameter(2016, "*");
        request.addParameter(20, type);
        List<QueueDetails> list = new ArrayList<>();

        try {
            PCFMessage[] responses = agent.send(request);
            String[] names = (String[]) ((String[]) responses[0].getParameterValue(3011));
            String[] arr$ = names;
            int len$ = names.length;

            for (int i$ = 0; i$ < len$; ++i$) {
                String name = arr$[i$];
                QueueDetails qd = new QueueDetails(name, managerName, managerIndex);
                qd.setQueueType(QueueType.getQueueType(type));
                list.add(qd);
            }
        } catch (PCFException var14) {
            log.warn("failed to autodiscover queues of type={}, Reason={} i queuemanager={}", type, var14.reasonCode, managerName);
        } catch (MQDataException | IOException e) {
            log.warn("Feilet til å liste kødetaljer fra queuemanager={} av type {} , Reason:{} ", managerName, type, e.getCause());
        }

        return list;
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
        if (SRVUSER.equals(server.getUser())) {
            properties.put(MQConstants.USE_MQCSP_AUTHENTICATION_PROPERTY, false);
        }
        properties.put(USER_ID_PROPERTY, server.getUser());
        properties.put(PASSWORD_PROPERTY, server.getPassword());

        return new MQQueueManager(server.getQueueManagerName(), properties);
    }

}
