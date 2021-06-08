package no.nav.mqmetrics.service;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@ToString
public class QueueDetails implements Serializable {
    private static final long serialVersionUID = 4508359994115995007L;
    private String queueName;
    private String resolvedQueueName;
    private String managerName;
    private int depth = -1;
    private int maxDepth = -1;
    private int maxMessageLength = -1;
    private int openInputCount = -1;
    private int openOutputCount = -1;
    private Date oldestMessage;
    private String correlationId;
    private String status;
    private QueueType queueType;
    private String description;
    private int managerIndex;

    public QueueDetails() {
        this.queueType = QueueType.UNKNOWN;
    }

    public QueueDetails(String queueName, String managerName, int managerIndex) {
        this.queueType = QueueType.UNKNOWN;
        this.queueName = queueName;
        this.managerName = managerName;
        this.managerIndex = managerIndex;
    }

}
