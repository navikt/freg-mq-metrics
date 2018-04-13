package no.nav.mqmetrics.nais;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(("/internal/"))
public class NaisProvider {

    @GetMapping(path = "isready")
    public boolean isReady() {
        return true;
    }

    @GetMapping(path = "isalive")
    public boolean isAlive() {
        return true;
    }
}
