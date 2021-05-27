package no.nav.mqmetrics.service;

import com.ibm.mq.MQException;
import com.ibm.mq.MQGetMessageOptions;
import com.ibm.mq.MQMessage;
import com.ibm.mq.MQPutMessageOptions;
import com.ibm.mq.MQQueue;
import com.ibm.mq.MQQueueManager;
import com.ibm.mq.constants.MQConstants;
import com.ibm.mq.headers.MQDataException;
import com.ibm.mq.headers.pcf.PCFException;
import com.ibm.mq.headers.pcf.PCFMessage;
import com.ibm.mq.headers.pcf.PCFMessageAgent;
import com.ibm.msg.client.jms.JmsConstants;
import lombok.SneakyThrows;
import no.nav.mqmetrics.exception.MQRuntimeException;
import no.nav.mqmetrics.utils.ByteUtil;
import no.nav.mqmetrics.utils.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import javax.jms.JMSException;
import java.io.EOFException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.ibm.mq.constants.CMQC.PASSWORD_PROPERTY;
import static com.ibm.mq.constants.CMQC.PORT_PROPERTY;
import static com.ibm.mq.constants.CMQC.USER_ID_PROPERTY;

public class MQService {
    private static final int GET_MESSAGE_OPTIONS = 8224;
    private static final int OPEN_QUEUE_OPTIONS = 8226;
    private static final int BROWSE_QUEUE_OPTIONS = 8234;
    private static final int MQRC_NO_MSG_AVAILABLE = 2033;
    private static final String FAILED_TO_WRITE_MESSAGE = "failed to write message";
    private static final String FAILED_TO_GET_MESSAGE_CONTENT = "failed to get message content";
    private static final String FAILED_TO_LIST_QUEUE_DETAILS = "failed to list queue details";
    private static final String FAILED_TO_GET_MESSAGE_DETAILS = "failed to get message details";
    private static final int RC_NO_MESSAGE_AVAILABLE = 2033;
    private static final String REC_CATCH_EXCEPTION = "REC_CATCH_EXCEPTION";
    private static final String REC_CATCH_EXCEPTION_JUSTIFICATION = "to avoid duplcating catch clauses";
    private static final Logger LOG = Logger.getLogger(MQService.class);
    private static final int UTF_8_WITH_PUA = 1208;

    public MQService() {
    }

    @SuppressWarnings(
            value = {"REC_CATCH_EXCEPTION"}
    )
    public List<QueueDetails> getQueueDetails(Server server, int type, int managerIndex) {
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
                    ((List) list).add(new QueueDetails(name, managerName, managerIndex));
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
            LOG.error("failed to list queue details", var15);
            throw new MQRuntimeException("failed to list queue details", var15);
        } finally {
            this.closeQuietly(queueManager);
        }

        return (List) list;
    }

    @SuppressWarnings(
            value = {"REC_CATCH_EXCEPTION"}
    )
    public List<MessageDetails> getMessageDetails(Server server, String queueName) {
        MQQueueManager queueManager = null;
        MQQueue queue = null;
        ArrayList list = new ArrayList();

        try {
            queueManager = this.getQueueManager(server);
            int openOptions = 8234;
            queue = queueManager.accessQueue(queueName, openOptions);
            MQGetMessageOptions getMessageOptions = new MQGetMessageOptions();
            getMessageOptions.options = 8224;
            this.addMessageDetails(queue, list, getMessageOptions);
        } catch (Exception var11) {
            LOG.error("failed to get message details", var11);
            throw new MQRuntimeException("failed to get message details", var11);
        } finally {
            this.closeQuietly(queue);
            this.closeQuietly(queueManager);
        }

        return list;
    }

    private void addMessageDetails(MQQueue queue, List<MessageDetails> list, MQGetMessageOptions getMessageOptions) throws IOException {
        while (true) {
            try {
                list.add(this.getMessageDetails(queue, getMessageOptions));
            } catch (MQException var6) {
                if (var6.reasonCode == 2033) {
                    return;
                }

                MessageDetails md = new MessageDetails();
                md.setStatus(var6.getMessage());
                list.add(md);
            }
        }
    }

    @SuppressWarnings(
            value = {"REC_CATCH_EXCEPTION"}
    )
    public byte[] getMessageContent(Server server, String queueName, String messageId) {
        MQQueueManager queueManager = null;
        MQQueue queue = null;

        byte[] var8;
        try {
            queueManager = this.getQueueManager(server);
            int openOptions = 8234;
            queue = queueManager.accessQueue(queueName, openOptions);
            MQGetMessageOptions getMessageOptions = new MQGetMessageOptions();
            getMessageOptions.options = 8224;
            var8 = getMessage(queue, getMessageOptions, ByteUtil.hexStringToByteArray(messageId));
        } catch (Exception var12) {
            LOG.error("failed to get message content", var12);
            throw new MQRuntimeException("failed to get message content", var12);
        } finally {
            this.closeQuietly(queue);
            this.closeQuietly(queueManager);
        }

        return var8;
    }

    @SuppressWarnings(
            value = {"REC_CATCH_EXCEPTION"}
    )
    public void deleteMessages(Server server, String queueName, String[] ids) {
        MQQueueManager queueManager = null;
        MQQueue queue = null;

        try {
            queueManager = this.getQueueManager(server);
            int openOptions = 8234;
            queue = queueManager.accessQueue(queueName, openOptions);
            MQGetMessageOptions getMessageOptions = new MQGetMessageOptions();
            getMessageOptions.options = 8192;
            String[] arr$ = ids;
            int len$ = ids.length;

            for (int i$ = 0; i$ < len$; ++i$) {
                String id = arr$[i$];
                this.deleteMessage(queue, getMessageOptions, ByteUtil.hexStringToByteArray(id));
            }
        } catch (Exception var15) {
            LOG.error("failed to get message content", var15);
            throw new MQRuntimeException("failed to get message content", var15);
        } finally {
            this.closeQuietly(queue);
            this.closeQuietly(queueManager);
        }

    }

    @SuppressWarnings(
            value = {"REC_CATCH_EXCEPTION"}
    )
    public void deleteMessages(Server server, String queueName) {
        MQQueueManager queueManager = null;
        MQQueue queue = null;

        try {
            queueManager = this.getQueueManager(server);
            int openOptions = 8234;
            queue = queueManager.accessQueue(queueName, openOptions);
            MQGetMessageOptions getMessageOptions = new MQGetMessageOptions();
            getMessageOptions.options = 8256;
            int depth = queue.getCurrentDepth();

            for (int i = depth; i > 0; --i) {
                MQMessage message = new MQMessage();
                queue.get(message, getMessageOptions);
            }
        } catch (Exception var13) {
            LOG.error("failed to get message content", var13);
            throw new MQRuntimeException("failed to get message content", var13);
        } finally {
            this.closeQuietly(queue);
            this.closeQuietly(queueManager);
        }

    }

    public void copyMessages(Server server, String source, String destination, String[] ids) {
        this.copyMessages(server, source, destination, ids, false, false);
    }

    public void moveMessages(Server server, String source, String destination, String[] ids) {
        this.copyMessages(server, source, destination, ids, true, false);
    }

    public void moveMessagesReliably(Server server, String source, String destination, String[] ids) {
        this.copyMessages(server, source, destination, ids, false, true);
    }

    private void copyMessages(Server server, String source, String destination, String[] ids, boolean delete, boolean deleteReliably) {
        MQQueueManager queueManager = null;
        MQQueue queueSource = null;
        MQQueue queueDestination = null;

        try {
            queueManager = this.getQueueManager(server);
            int sourceOptions = 8234;
            int destinationOptions = 8208;
            queueSource = queueManager.accessQueue(source, sourceOptions);
            queueDestination = queueManager.accessQueue(destination, destinationOptions);
            MQGetMessageOptions getMessageOptions = new MQGetMessageOptions();
            getMessageOptions.options = 8192 + (delete && !deleteReliably ? 0 : 32);
            MQGetMessageOptions getMessageOptions2 = new MQGetMessageOptions();
            getMessageOptions2.options = 8192;
            MQPutMessageOptions putMessageOptions = new MQPutMessageOptions();
            putMessageOptions.options = 8256;
            String[] arr$ = ids;
            int len$ = ids.length;

            for (int i$ = 0; i$ < len$; ++i$) {
                String id = arr$[i$];
                byte[] msgid = ByteUtil.hexStringToByteArray(id);
                this.copyMessage(queueSource, queueDestination, getMessageOptions, putMessageOptions, msgid);
                if (deleteReliably) {
                    this.deleteMessage(queueSource, getMessageOptions2, msgid);
                }
            }
        } catch (Exception var23) {
            LOG.error("failed to get message content", var23);
            throw new MQRuntimeException("failed to get message content", var23);
        } finally {
            this.closeQuietly(queueSource);
            this.closeQuietly(queueDestination);
            this.closeQuietly(queueManager);
        }

    }

    @SuppressWarnings(
            value = {"REC_CATCH_EXCEPTION"}
    )
    public MessageDetails getMessageDetails(Server server, String queueName, String messageId) {
        MQQueueManager queueManager = null;
        MQQueue queue = null;

        MessageDetails var8;
        try {
            queueManager = this.getQueueManager(server);
            int openOptions = 8234;
            queue = queueManager.accessQueue(queueName, openOptions);
            MQGetMessageOptions getMessageOptions = new MQGetMessageOptions();
            getMessageOptions.options = 8224;
            var8 = this.getMessageDetails(queue, getMessageOptions, ByteUtil.hexStringToByteArray(messageId));
        } catch (Exception var12) {
            LOG.error("failed to get message details", var12);
            throw new MQRuntimeException("failed to get message details", var12);
        } finally {
            this.closeQuietly(queue);
            this.closeQuietly(queueManager);
        }

        return var8;
    }

    @SuppressWarnings(
            value = {"REC_CATCH_EXCEPTION"}
    )
    public void put(Server server, String queueName, byte[] msg, String correlationId, String msgId, int expiry, String replyToQueueManager, String replyToQueue, boolean binary) {
        MQQueueManager queueManager = null;
        MQQueue queue = null;

        try {
            queueManager = this.getQueueManager(server);
            int openOptions = 8208;
            queue = queueManager.accessQueue(queueName, openOptions);
            MQMessage message = this.createMQMessage(correlationId, msgId, expiry, replyToQueueManager, replyToQueue, binary);
            MQPutMessageOptions putMessageOptions = new MQPutMessageOptions();
            putMessageOptions.options = 8192;
            if (binary) {
                message.write(msg);
            } else {
                message.writeString(new String(msg));
            }

            queue.put(message, putMessageOptions);
        } catch (Exception var18) {
            LOG.error("failed to write message", var18);
            throw new MQRuntimeException("failed to write message", var18);
        } finally {
            this.closeQuietly(queue);
            this.closeQuietly(queueManager);
        }

    }

    private MQMessage createMQMessage(String correlationId, String msgId, int expiry, String replyToQueueManager, String replyToQueue, boolean binary) {
        MQMessage message = new MQMessage();
        message.format = binary ? "        " : "MQSTR   ";
        message.expiry = expiry;
        message.messageId = msgId.getBytes();
        message.correlationId = correlationId.getBytes();
        if (StringUtils.isNotEmpty(replyToQueueManager)) {
            message.replyToQueueManagerName = replyToQueueManager;
        }

        if (StringUtils.isNotEmpty(replyToQueue)) {
            message.replyToQueueName = replyToQueue;
        }

        return message;
    }

    private void deleteMessage(MQQueue queue, MQGetMessageOptions options, byte[] id) throws MQException, IOException {
        MQMessage message = new MQMessage();
        message.messageId = new byte[id.length];
        System.arraycopy(id, 0, message.messageId, 0, id.length);
        queue.get(message, options);
    }

    private void copyMessage(MQQueue sourceQueue, MQQueue destinationQueue, MQGetMessageOptions getOptions, MQPutMessageOptions putOptions, byte[] id) throws MQException, IOException {
        MQMessage message = new MQMessage();
        message.messageId = new byte[id.length];
        System.arraycopy(id, 0, message.messageId, 0, id.length);
        sourceQueue.get(message, getOptions);
        destinationQueue.put(message, putOptions);
    }

    private MessageDetails getMessageDetails(MQQueue queue, MQGetMessageOptions options) throws MQException, IOException {
        return this.getMessageDetails(queue, new MQMessage(), options);
    }

    private MessageDetails getMessageDetails(MQQueue queue, MQGetMessageOptions options, byte[] id) throws MQException, IOException {
        MQMessage message = new MQMessage();
        message.messageId = new byte[id.length];
        System.arraycopy(id, 0, message.messageId, 0, id.length);
        MessageDetails messageDetails = this.getMessageDetails(queue, message, options);
        Map<String, Object> map = this.addPropertiesFromMessage(message);
        int dataLength = message.getDataLength();
        map.put("PayloadLength", dataLength);
        this.addPropertiesFromPropertyNames(message, map);
        messageDetails.setProperties(map);
        this.addMessage(message, messageDetails, dataLength);
        return messageDetails;
    }

    private void addMessage(MQMessage message, MessageDetails messageDetails, int dataLength) throws IOException {
        byte[] buffer = new byte[dataLength];

        try {
            message.readFully(buffer);
            messageDetails.setContent(buffer);
        } catch (EOFException var8) {
            LOG.error("failed to read " + dataLength + " bytes into buffer with readFully", var8);
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            var8.printStackTrace(pw);
            messageDetails.setContent(sw.toString().getBytes());
        }

    }

    private Map<String, Object> addPropertiesFromMessage(MQMessage message) throws IOException {
        Map<String, Object> map = new HashMap();
        map.put("AccountingToken", ByteUtil.byteArrayToHexString(message.accountingToken));
        map.put("ApplicationIdData", message.applicationIdData);
        map.put("ApplicationOriginData", message.applicationOriginData);
        map.put("BackoutCount", message.backoutCount);
        map.put("CharacterSet", message.characterSet);
        map.put("CorrelationID", getCorrelationId(message));
        map.put("Encoding", message.encoding);
        map.put("Expiry", message.expiry);
        map.put("Feedback", message.feedback);
        map.put("Format", message.format);
        map.put("DataLength", message.getDataLength());
        map.put("DataOffset", message.getDataOffset());
        map.put("TotalMessageLength", message.getTotalMessageLength());
        map.put("Version", message.getVersion());
        map.put("GroupId", ByteUtil.byteArrayToHexString(message.groupId));
        map.put("MessageFlags", message.messageFlags);
        map.put("MessageId", ByteUtil.byteArrayToHexString(message.messageId));
        map.put("MessageSequenceNumber", message.messageSequenceNumber);
        map.put("MessageType", message.messageType);
        map.put("Offset", message.offset);
        map.put("OriginalLength", message.originalLength);
        map.put("Persistence", message.persistence);
        map.put("PutApplicationName", message.putApplicationName);
        map.put("PutApplicationType", message.putApplicationType);
        map.put("PutDateTime", message.putDateTime.getTime());
        map.put("ReplyToQueueManager", message.replyToQueueManagerName);
        map.put("ReplyToQueueName", message.replyToQueueName);
        map.put("Report", message.report);
        return map;
    }

    private MessageDetails getMessageDetails(MQQueue queue, MQMessage message, MQGetMessageOptions options) throws MQException, IOException {
        queue.get(message, options);
        MessageDetails messageDetails = new MessageDetails();
        messageDetails.setLength(message.getMessageLength());
        messageDetails.setCorrelationId(getCorrelationId(message));
        messageDetails.setMessageId(getMessageId(message));
        messageDetails.setReplyToQueueName(message.replyToQueueName.trim());
        messageDetails.setReceiveDate(message.putDateTime.getTime());
        messageDetails.setExpiration(message.expiry);
        messageDetails.setCcsid(message.characterSet);
        messageDetails.setFormat(message.format);
        Map<String, Object> props = new HashMap();
        this.addPropertiesFromPropertyNames(message, props);
        messageDetails.setProperties(props);
        messageDetails.setStatus((String) props.get("_exceptionMessage"));
        return messageDetails;
    }

    private void addPropertiesFromPropertyNames(MQMessage message, Map<String, Object> map) throws MQException {
        Enumeration props = message.getPropertyNames("%");

        while (props.hasMoreElements()) {
            String p = (String) props.nextElement();
            map.put(p, message.getObjectProperty(p));
        }

    }

    private static byte[] getMessage(MQQueue queue, MQGetMessageOptions options, byte[] id) throws MQException, IOException {
        MQMessage message = new MQMessage();
        message.messageId = new byte[id.length];
        System.arraycopy(id, 0, message.messageId, 0, id.length);
        queue.get(message, options);
        int length = message.getMessageLength();
        byte[] buffer = new byte[length];
        message.readFully(buffer);
        return buffer;
    }

    private static String getCorrelationId(MQMessage message) {
        String corrId = (new String(message.correlationId)).trim();

        for (int i = 0; i < corrId.length(); ++i) {
            if (!StringUtil.isPrintable(corrId.charAt(i))) {
                return "ID:" + ByteUtil.byteArrayToHexString(message.correlationId);
            }
        }

        return corrId;
    }

    private static String getMessageId(MQMessage message) {
        return ByteUtil.byteArrayToHexString(message.messageId);
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

    @SneakyThrows
    private List<QueueDetails> autodiscoverQueues(MQQueueManager queueManager, int type, boolean disconnect, String managerName, int managerIndex) {
        PCFMessageAgent agent = null;
        ArrayList list = new ArrayList();

        try {
            agent = new PCFMessageAgent(queueManager);
            if (type == -1) {
                list.addAll(this.getQueuesByType(agent, 1, managerName, managerIndex));
                list.addAll(this.getQueuesByType(agent, 3, managerName, managerIndex));
                list.addAll(this.getQueuesByType(agent, 7, managerName, managerIndex));
                list.addAll(this.getQueuesByType(agent, 2, managerName, managerIndex));
                list.addAll(this.getQueuesByType(agent, 6, managerName, managerIndex));
            } else {
                list.addAll(this.getQueuesByType(agent, type, managerName, managerIndex));
            }
        } catch (MQException var13) {
            this.handleMQExceptionInAutoDiscover(var13);
        } catch (IOException var14) {
            throw new MQRuntimeException("failed to autodiscover queues", var14);
        } catch (MQDataException e) {
            throw new MQDataException(e.completionCode, e.reasonCode, e.exceptionSource);
        } finally {
            this.closeQuietly(agent);
            if (disconnect) {
                this.closeQuietly(queueManager);
            }

        }

        return list;
    }

    private void handleMQExceptionInAutoDiscover(MQException ex) {
        if (ex.reasonCode != 2033) {
            throw new MQRuntimeException("failed to autodiscover queues", ex);
        } else {
            LOG.debug("PCF calls gave a 2033 reason code, ignoring");
        }
    }

    private void closeQuietly(PCFMessageAgent agent) {
        if (agent != null) {
            try {
                agent.disconnect();
            } catch (MQDataException var3) {
                LOG.error("failed to disconnect agent", var3);
            }
        }

    }

    private List<QueueDetails> getQueuesByType(PCFMessageAgent agent, int type, String managerName, int managerIndex) throws MQException, IOException {
        PCFMessage request = new PCFMessage(18);
        request.addParameter(2016, "*");
        request.addParameter(20, type);
        ArrayList list = new ArrayList();

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
            LOG.info("failed to autodiscover queues of type " + type + ", Reason: " + var14.reasonCode);
        } catch (MQDataException e) {
            e.printStackTrace();
        }

        return list;
    }

    private void closeQuietly(MQQueueManager queueManager) {
        if (queueManager != null) {
            try {
                queueManager.disconnect();
            } catch (MQException var4) {
                LOG.warn("MQException while disconnecting queueManager", var4);
            }

            try {
                queueManager.close();
            } catch (MQException var3) {
                LOG.warn("MQException while closing queueManager", var3);
            }
        }

    }

    private void closeQuietly(MQQueue queue) {
        if (queue != null) {
            try {
                queue.close();
            } catch (MQException var3) {
                LOG.warn("MQException while closing queue", var3);
            }
        }

    }


    private MQQueueManager getQueueManager(Server server) throws MQException, JMSException {
        Hashtable<String, Object> properties = new Hashtable();
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
