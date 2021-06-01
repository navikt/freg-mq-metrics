package no.nav.mqmetrics.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageDetails implements Serializable {
    private static final long serialVersionUID = -7486115386692136620L;
    private int length;
    private String correlationId;
    private String messageId;
    private Date receiveDate;
    private String replyToQueueName;
    private int expiration;
    private int ccsid;
    private String status;
    private String format;
    private byte[] content;
    private Map<String, Object> properties;

}
