package no.nav.mqmetrics.service;


import com.ibm.mq.MQException;
import org.apache.log4j.Logger;

import java.lang.reflect.Field;

public final class MQUtil {
    private static final Logger LOG = Logger.getLogger(MQUtil.class);

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

        for(int i$ = 0; i$ < len$; ++i$) {
            Field field = arr$[i$];
            String name = field.getName();
            if (name.startsWith(prefix)) {
                try {
                    if (field.getInt(ex) == value) {
                        return name;
                    }
                } catch (IllegalArgumentException var12) {
                    LOG.error("failed to get field value for " + name, var12);
                } catch (IllegalAccessException var13) {
                    LOG.error("failed to get field value for " + name, var13);
                }
            }
        }

        return unknown;
    }
}

