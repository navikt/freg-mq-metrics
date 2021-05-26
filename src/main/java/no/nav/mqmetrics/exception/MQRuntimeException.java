package no.nav.mqmetrics.exception;

public class MQRuntimeException extends RuntimeException {
    private static final long serialVersionUID = -5683352831952821975L;

    public MQRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }
}
