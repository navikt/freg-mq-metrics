package no.nav.mqmetrics;

//import com.ibm.mq.MQEnvironment;
//import com.ibm.mq.MQException;
//import com.ibm.mq.MQQueue;
//import com.ibm.mq.MQQueueManager;
//import com.ibm.mq.jms.MQQueueConnectionFactory;
//import com.ibm.msg.client.wmq.WMQConstants;
//import com.ibm.msg.client.wmq.v6.base.internal.MQC;
//import lombok.extern.slf4j.Slf4j;
//import org.junit.Test;
//
//import javax.jms.ConnectionFactory;
//import javax.jms.JMSException;
//import java.net.URI;
//import java.net.URISyntaxException;

//@Slf4j
public class JmsDiscoverMqLearningTest {
//    private static final int UTF_8_WITH_PUA = 1208;
//    private final String queueName = "QA.U2_PERSON_TPS_MOTTAK.TPS_DIST";
//
//    private URI uri = new URI("mq://e26apvl100.test.local:1411/MDLCLIENT03");
//    private final String managerName = uri.getPath().replace("/", "");
//    private final int port = uri.getPort();
//    private final String hostName = uri.getHost();
//    private String channelName = "U2_PERSON_TPS_MOTTAK";
//
//    public JmsDiscoverMqLearningTest() throws URISyntaxException {
//    }
//
//
//    @Test
//    public void name() throws URISyntaxException, JMSException, MQException {
//        //ConnectionFactory connectionFactory = getConnectionFactory();
//
//        //Connection connection = connectionFactory.createConnection("srvappserver", "");
//        MQQueueManager queueManager = createQueueManager();
//        int depth = depthOf(queueManager, queueName);
//        System.out.println("depth = " + depth);
//
//    }
//
//    public int depthOf(MQQueueManager queueManager, String queueName) throws MQException {
////        MQQueue queue = queueManager.accessQueue(queueName, MQC.MQOO_INQUIRE | MQC.MQOO_INPUT_AS_Q_DEF, null, null, null);
//        MQQueue queue = queueManager.accessQueue(queueName, MQC.MQOO_INQUIRE /*| MQC.MQOO_INPUT_AS_Q_DEF*/, null, null, null);
//        int currentDepth = -1;
//        try {
//            currentDepth = queue.getCurrentDepth();
//        } catch (MQException e) {
//            log.error("", e);
//        }
//        return currentDepth;
//    }
//
//    @SuppressWarnings("unchecked")
//    private MQQueueManager createQueueManager() throws MQException {
//        MQEnvironment.channel = channelName;
//        MQEnvironment.port = port;
//        MQEnvironment.hostname = hostName;
//        MQEnvironment.userID = "srvappserver";
//        MQEnvironment.password = "";
//        MQEnvironment.properties.put(MQC.TRANSPORT_PROPERTY, MQC.TRANSPORT_MQSERIES);
//        return new MQQueueManager(managerName);
//    }
//
//    private ConnectionFactory getConnectionFactory() throws URISyntaxException, JMSException {
//        MQQueueConnectionFactory connectionFactory = new MQQueueConnectionFactory();
//
////        URI uri = new URI(env.getRequiredProperty("distribusjon.tps.jms.managerName.uri"));
////        String channelName = env.getRequiredProperty("distribusjon.tps.jms.managerName.channelName-name");
//
//        connectionFactory.setHostName(hostName);
//        connectionFactory.setPort(port);
//        connectionFactory.setQueueManager(managerName);
//        connectionFactory.setChannel(channelName);
//        connectionFactory.setTransportType(WMQConstants.WMQ_CM_CLIENT);
//        connectionFactory.setCCSID(UTF_8_WITH_PUA);
//        connectionFactory.setIntProperty(WMQConstants.JMS_IBM_ENCODING, MQC.MQENC_NATIVE);
//        connectionFactory.setIntProperty(WMQConstants.JMS_IBM_CHARACTER_SET, UTF_8_WITH_PUA);
//
//
//        return connectionFactory;
//    }
}
