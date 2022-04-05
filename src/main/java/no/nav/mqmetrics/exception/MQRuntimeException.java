package no.nav.mqmetrics.exception;

import java.io.Serial;

public class MQRuntimeException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = -5683352831952821975L;

    public MQRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }
}
