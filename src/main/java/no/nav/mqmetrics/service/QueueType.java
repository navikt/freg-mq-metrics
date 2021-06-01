package no.nav.mqmetrics.service;

public enum QueueType {
    LOCAL(1),
    REMOTE(6),
    ALIAS(3),
    CLUSTER(7),
    MODEL(2),
    UNKNOWN(-1);

    private int type;

    private QueueType(int type) {
        this.type = type;
    }

    public static int getType(QueueType queueType) {
        return queueType.type;
    }

    public static QueueType getQueueType(int type) {
        switch(type) {
            case 1:
                return LOCAL;
            case 2:
                return MODEL;
            case 3:
                return ALIAS;
            case 4:
            case 5:
            default:
                return UNKNOWN;
            case 6:
                return REMOTE;
            case 7:
                return CLUSTER;
        }
    }
}
