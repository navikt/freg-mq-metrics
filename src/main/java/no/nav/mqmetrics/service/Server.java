package no.nav.mqmetrics.service;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jndi.JndiTemplate;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.util.Collection;

@Getter
@Setter
@Slf4j
public class Server implements Serializable {
    private static final long serialVersionUID = 6429024078737806983L;
    private static final String NOT_RESOLVED_FROM_JNDI = "NOT_RESOLVED_FROM_JNDI";
    private static final int UTF_8_WITH_PUA = 1208;

    private String host;
    private Integer port;
    private String queueManagerName;
    private String channel;
    private int ccsid;
    private String user;
    private String password;
    private String qcfJndi;
    private Collection<String> queues;
    private transient JndiTemplate jndiTemplate;
    @SuppressWarnings({"SE_TRANSIENT_FIELD_NOT_RESTORED"})
    private transient String aliasPrefix;
    private boolean mqcsp;

    public Server() {
    }

    public Server(String qcfJndi) {
        this.qcfJndi = qcfJndi;
    }

    public Server(String host, Integer port, String channel, String queueManagerName, boolean mqcsp) {
        this.host = host;
        this.port = port;
        this.channel = channel;
        this.queueManagerName = queueManagerName;
        this.mqcsp = mqcsp;
    }

    public String toString() {
        return "host=" + this.host + (this.port != null ? ":" + this.port : "") + ", channel=" + this.channel + ", manager=" + this.queueManagerName;
    }

    public String getName() {
        return this.queueManagerName + " " + this.host + (this.port != null ? " " + this.port : "") + " " + this.channel;
    }

    public boolean isAutodiscover() {
        return CollectionUtils.isEmpty(this.queues);
    }


}