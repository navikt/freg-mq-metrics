package no.nav.mqmetrics.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;

@Getter
@Setter
@ConfigurationProperties(value = "mqadmin")
@Validated
public class MqAdminProperties {

    @NotEmpty
    private String username;

    @NotEmpty
    @ToString.Exclude
    private String password;
}
