package no.nav.mqmetrics;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackageClasses = AppStarter.class)
public class LocalAppStarter {

    static void main(String[] args) {
        System.setProperty("spring.profiles.active", "local,q11");
        SpringApplication.run(LocalAppStarter.class, args);
    }
}
