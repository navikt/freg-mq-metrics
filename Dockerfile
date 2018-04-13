FROM navikt/java:8
LABEL maintainer="Team Registre"

ADD "target/freg-mq-metrics-exec.jar" /app/app.jar