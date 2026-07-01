package no.nav.mqmetrics.metrics;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static no.nav.mqmetrics.metrics.QueueEnvironmentUtils.extractEnvironmentNameFromQueueName;
import static org.assertj.core.api.Assertions.assertThat;

public class QueueEnvironmentUtilsTest {

    @ParameterizedTest
    @MethodSource
    public void shouldExtractEnvironmentName(String queueName, String expected) {
        var environmentName = extractEnvironmentNameFromQueueName(queueName);

        assertThat(environmentName).isEqualTo(expected);
    }

    public static Stream<Arguments> shouldExtractEnvironmentName() {
        return Stream.of(
                Arguments.of("QA.I11_HEND.SAKSBEHANDLING_BQ", "I11"),
                Arguments.of("QA.U1_SBEH.SAKSBEHANDLING", "U1"),
                Arguments.of("QA.P_SBEH.SAKSBEHANDLING", "P"),
                Arguments.of("QA.SBEH.SAKSBEHANDLING", "UNKNOWN"),
                Arguments.of("QA.P460.BREV_REPLY_QUE", "P"),
                Arguments.of("QA.PKKC.CLUSTER.TEST.MPLSC02", "P"),
                Arguments.of("QA.U5_STAT.STATISTIKK_BQ", "U5")
        );
    }
}