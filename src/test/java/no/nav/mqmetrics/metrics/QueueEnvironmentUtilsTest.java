package no.nav.mqmetrics.metrics;


import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
public class QueueEnvironmentUtilsTest {


    public static Stream<Arguments> data() {
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


    @ParameterizedTest
    @MethodSource("data")
    public void testName(String input, String expected) {
        assertEquals(QueueEnvironmentUtils.stripEnvironmentNameFrom(input), expected);
    }
}