package no.nav.mqmetrics.service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class MQUtil {

    public static final String Q_SECURE_QUEUEMANAGER_NAME = "MQLS01";
    public static final String P_SECURE_QUEUEMANAGER_NAME = "MPLS01";

    private MQUtil() {
    }

    public static boolean isSecureMqBroker(Server server) {
        return Q_SECURE_QUEUEMANAGER_NAME.equalsIgnoreCase(server.getQueueManagerName()) || P_SECURE_QUEUEMANAGER_NAME.equalsIgnoreCase(server.getQueueManagerName());
    }

}
