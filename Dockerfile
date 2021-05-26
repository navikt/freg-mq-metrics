FROM navikt/java:11

ADD "target/freg-mq-metrics-exec.jar" /app/app.jar

COPY export-vault-secrets.sh /init-scripts/10-export-vault-secrets.sh

ENV JAVA_OPTS="-Xmx2048m \
               -Djava.security.egd=file:/dev/./urandom \
               -Dspring.profiles.active=nais"