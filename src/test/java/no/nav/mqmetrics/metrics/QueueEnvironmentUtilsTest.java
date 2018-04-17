package no.nav.mqmetrics.metrics;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;

import static org.hamcrest.Matchers.is;

@RunWith(Parameterized.class)
public class QueueEnvironmentUtilsTest {


    private String input, expexted;

    public QueueEnvironmentUtilsTest(String input, String expexted) {
        this.input = input;
        this.expexted = expexted;
    }

    @Parameterized.Parameters(name = "{index}: queue[{0}] = env= {1}")
    public static Iterable<Object[]> data() {
        return Arrays.asList(
                testCase("QA.I11_HEND.SAKSBEHANDLING_BQ", "I11"),
                testCase("QA.U1_SBEH.SAKSBEHANDLING", "U1"),
                testCase("QA.P_SBEH.SAKSBEHANDLING", "P"),
                testCase("QA.SBEH.SAKSBEHANDLING", "UNKNOWN"),
                testCase("QA.U5_STAT.STATISTIKK_BQ", "U5")
        );
    }

    private static Object[] testCase(String input, String expected) {
        return new Object[]{input, expected};
    }

    @Test
    public void testName() {
        Assert.assertThat(QueueEnvironmentUtils.stripQueueEnvironment(input), is(expexted));
    }
}