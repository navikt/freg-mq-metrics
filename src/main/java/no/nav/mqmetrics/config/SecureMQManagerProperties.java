package no.nav.mqmetrics.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

@Getter
@Setter
@ToString
@ConfigurationProperties("securemqmanager")
@Validated
public class SecureMQManagerProperties {

    @NotBlank
    private String hostname;
    @NotBlank
    private String name;
    @Min(0)
    private int port;

    @NotBlank
    private String channelName;
}
