package no.nav.mqmetrics.service;


import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

public class HentMqDetail {




    private HttpHeaders headers() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
       // headers.set(HttpHeaders.AUTHORIZATION,);
        return headers;

    }
}
