package no.nav.mqmetrics.metrics;

import lombok.extern.slf4j.Slf4j;
import no.nav.mqmetrics.config.MqProperties.MqChannel;
import no.nav.mqmetrics.config.ServiceuserProperties;
import no.nav.mqmetrics.exception.MQRuntimeException;
import no.nav.mqmetrics.service.DokQueueStatus;
import no.nav.mqmetrics.service.MQService;
import no.nav.mqmetrics.service.QueueType;
import no.nav.mqmetrics.service.Server;
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

	private final MQService mqService;
	private final ServiceuserProperties serviceuserProperties;

	public QueueManagerConsumer(MQService mqService, ServiceuserProperties serviceuserProperties) {
		this.mqService = mqService;
		this.serviceuserProperties = serviceuserProperties;
	}

	public Map<String, Integer> getQueueDepths(MqChannel channel) {

		final String managerName = channel.getQueueManagerName();
		final int port = channel.getQueueManagerPort();
		final String hostName = channel.getQueueManagerHost();
		final String channelName = channel.getChannelName();

		Server server = new Server(hostName, port, channelName, managerName, false);
		server.setMqcsp(true);
		server.setUser(serviceuserProperties.getUsername());
		server.setPassword(serviceuserProperties.getPassword());

		// if list of queues are empty, autodiscover is considered enabled. Duplicates are removed
		server.setQueues(new ArrayList<>(new HashSet<>(channel.getQueueNames())));

		log.debug("Querying {} {} for queue depts", managerName, channelName);
		try {
			log.info("Mottatt kall til å hente kødybder for queuemanger={}, host={} og channelname={}", server.getQueueManagerName(), server.getHost(), server.getChannel());
			List<DokQueueStatus> queueDetails = mqService.getSecureQueueDetails(server, QueueType.getType(ALIAS));
			Map<String, Integer> result = queueDetails.stream()
					.filter(d -> 0 <= d.getDepth())//Negative depths are not accessible, skips them.
					.collect(Collectors.toMap(name -> MqChannel.ensureQueueAlias(name.getQueueName()), DokQueueStatus::getDepth));
			log.info("Funnet {} kødybder i queuemanger={}", result.size(), server.getQueueManagerName());
			return result;
		} catch (MQRuntimeException e) {
			log.warn("Not able to fetch queues from {}", server, e);
			return Collections.emptyMap();
		}
	}

}
