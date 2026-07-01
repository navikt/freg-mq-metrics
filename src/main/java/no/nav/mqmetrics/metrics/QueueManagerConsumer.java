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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Collections.emptyMap;
import static no.nav.mqmetrics.config.MqProperties.MqChannel.ensureQueueAlias;
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

		Server server = new Server(hostName, port, channelName, managerName, true);
		server.setUser(serviceuserProperties.getUsername());
		server.setPassword(serviceuserProperties.getPassword());

		// if list of queues are empty, autodiscover is considered enabled. Duplicates are removed
		server.setQueues(new ArrayList<>(new HashSet<>(channel.getQueueNames())));

		try {
			log.info("Henter kødybder fra queuemanager={}, host={} og channelname={}", server.getQueueManagerName(), server.getHost(), server.getChannel());

			List<DokQueueStatus> queueDetails = mqService.getSecureQueueDetails(server, QueueType.getType(ALIAS));
			Map<String, Integer> mapMellomKoenavnOgKoedybde = queueDetails.stream()
					.filter(koestatus -> koestatus.getDepth() >= 0) // Koedybde må være positiv
					.collect(Collectors.toMap(name -> ensureQueueAlias(name.getQueueName()), DokQueueStatus::getDepth));
			log.info("Hentet {} kødybder fra queuemanager={}", mapMellomKoenavnOgKoedybde.size(), server.getQueueManagerName());

			return mapMellomKoenavnOgKoedybde;
		} catch (MQRuntimeException e) {
			log.warn("Kunne ikke hente kødybder fra {}", server, e);
			return emptyMap();
		}
	}

}