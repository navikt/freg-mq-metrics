package no.nav.mqmetrics.service;

public enum QueueType {
    LOCAL(1),
    REMOTE(6),
    ALIAS(3),
    CLUSTER(7),
    MODEL(2),
    UNKNOWN(-1);

    private final int type;

    QueueType(int type) {
        this.type = type;
    }

    public static int getType(QueueType queueType) {
        return queueType.type;
    }

}
