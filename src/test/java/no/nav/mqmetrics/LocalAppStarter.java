package no.nav.mqmetrics;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackageClasses = AppStarter.class)
public class LocalAppStarter {
    public static void main(String[] args) {
        System.setProperty("spring.profiles.active", "local");
        SpringApplication.run(LocalAppStarter.class, args);
    }
}
