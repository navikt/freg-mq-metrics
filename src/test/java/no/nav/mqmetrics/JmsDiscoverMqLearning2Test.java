package no.nav.mqmetrics;

//import com.google.common.base.Joiner;
//import com.ibm.mq.MQEnvironment;
//import com.ibm.mq.MQException;
//import com.ibm.mq.MQQueue;
//import com.ibm.mq.MQQueueManager;
//import com.ibm.mq.jms.MQQueueConnectionFactory;
//import com.ibm.msg.client.wmq.WMQConstants;
//import com.ibm.msg.client.wmq.v6.base.internal.MQC;
//import no.nav.emottak.mq.MQService;
//import no.nav.emottak.mq.QueueDetails;
//import no.nav.emottak.mq.QueueType;
//import no.nav.emottak.mq.Server;
//import org.apache.commons.collections4.MapUtils;
//import org.junit.Test;
//import org.springframework.util.comparator.Comparators;
//
//import javax.jms.ConnectionFactory;
//import javax.jms.JMSException;
//import java.net.URI;
//import java.net.URISyntaxException;
//import java.util.Comparator;
//import java.util.List;
//import java.util.Map;
//import java.util.StringJoiner;
//import java.util.stream.Collectors;
//
//import static java.util.Comparator.comparing;

public class JmsDiscoverMqLearning2Test {
//    private static final int UTF_8_WITH_PUA = 1208;
//    private final String queueName = "QA.U2_PERSON_TPS_MOTTAK.TPS_DIST";
//
//    private URI uri = new URI("mq://e26apvl100.test.local:1411/MDLCLIENT03");
//    private final String managerName = uri.getPath().replace("/", "");
//    private final int port = uri.getPort();
//    private final String hostName = uri.getHost();
//    private final String channelName = "U2_PERSON_TPS_MOTTAK";
////    private String channelName = "HERMES.SVRCONN";
//
//    public JmsDiscoverMqLearning2Test() throws URISyntaxException {
//    }
//
//    @Test
//    public void name() throws URISyntaxException, JMSException, MQException {
//
//        MQService mqService = new MQService();
//        Server server = new Server(hostName, port, channelName, managerName);
//        server.setUser("srvappserver");
//        server.setPassword("");
//
//        List<QueueDetails> queueDetails = mqService.getQueueDetails(server, QueueType.getType(QueueType.ALIAS), 1);
//        Map<String, Integer> map = queueDetails.stream()
//                .sorted(comparing(QueueDetails::getQueueName))
//                .collect(Collectors.toMap(a -> a.getQueueName().trim(), b -> b.getDepth()));
//        String out = Joiner.on("\n").withKeyValueSeparator(" = ").join(map);
//        System.out.println("out = " + out);
//    }

}
