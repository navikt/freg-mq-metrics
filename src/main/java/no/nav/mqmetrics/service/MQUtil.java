package no.nav.mqmetrics.service;

import com.ibm.mq.MQException;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;

@Slf4j
public final class MQUtil {

    public static final String Q_SECURE_QUEUEMANAGER_NAME = "MQLS01";
    public static final String P_SECURE_QUEUEMANAGER_NAME = "MPLS01";

    private MQUtil() {
    }

    public static String getReasonCode(MQException ex) {
        return getCode(ex, ex.reasonCode, "MQRC_", "Unknown reason code");
    }

    public static String getCompletionCode(MQException ex) {
        return getCode(ex, ex.completionCode, "MQCC_", "Unknown completion code");
    }

    private static String getCode(MQException ex, int value, String prefix, String unknown) {
        Class<?> clazz = MQException.class;
        Field[] fields = clazz.getDeclaredFields();
        Field[] arr$ = fields;
        int len$ = fields.length;

        for (int i$ = 0; i$ < len$; ++i$) {
            Field field = arr$[i$];
            String name = field.getName();
            if (name.startsWith(prefix)) {
                try {
                    if (field.getInt(ex) == value) {
                        return name;
                    }
                } catch (IllegalArgumentException var12) {
                    log.error("failed to get field value for " + name, var12);
                } catch (IllegalAccessException var13) {
                    log.error("failed to get field value for " + name, var13);
                }
            }
        }

        return unknown;
    }

    public static boolean isSecureMqBroker(Server server) {
        return Q_SECURE_QUEUEMANAGER_NAME.equalsIgnoreCase(server.getQueueManagerName()) || P_SECURE_QUEUEMANAGER_NAME.equalsIgnoreCase(server.getQueueManagerName());
    }

}
