package ua.kpi.tzyapz.finkit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LoanCalculatorTest {

    private final LoanCalculator calculator = new LoanCalculator();

    @ParameterizedTest
    @CsvSource({
            "100000, 12, 12, 8884.88",
            "100000, 7, 12, 8652.67",
            "50000, 15, 24, 2424.33",
            "1000000, 40, 60, 38751.64"
    })
    void monthlyPaymentMatchesFormula(BigDecimal principal, BigDecimal rate, int months, BigDecimal expected) {
        assertEquals(expected, calculator.monthlyPayment(principal, rate, months));
    }

    @Test
    void totalInterestEqualsPaymentTimesMonthsMinusPrincipal() {
        BigDecimal actual = calculator.totalInterest(
                new BigDecimal("100000"),
                new BigDecimal("12"),
                12
        );
        assertEquals(new BigDecimal("6618.56"), actual);
    }

    @ParameterizedTest
    @CsvSource({
            "999.99, 12, 12, true, ",
            "1000, 12, 12, false, 88.85",
            "1000000, 12, 12, false, 88848.76",
            "1000000.01, 12, 12, true, "
    })
    void principalBoundaries(BigDecimal principal, BigDecimal rate, int months, boolean expectException, BigDecimal expectedPayment) {
        if (expectException) {
            assertThrows(IllegalArgumentException.class,
                    () -> calculator.monthlyPayment(principal, rate, months));
        } else {
            assertEquals(expectedPayment, calculator.monthlyPayment(principal, rate, months));
        }
    }

    @ParameterizedTest
    @CsvSource({
            "10000, 12, 2, true, ",
            "10000, 12, 3, false, 3400.22",
            "10000, 12, 60, false, 212.47",
            "10000, 12, 61, true, "
    })
    void monthsBoundaries(BigDecimal principal, BigDecimal rate, int months, boolean expectException, BigDecimal expectedPayment) {
        if (expectException) {
            assertThrows(IllegalArgumentException.class,
                    () -> calculator.monthlyPayment(principal, rate, months));
        } else {
            assertEquals(expectedPayment, calculator.monthlyPayment(principal, rate, months));
        }
    }

    @ParameterizedTest
    @CsvSource({
            "100000, 4.99, 12, true, ",
            "100000, 5, 12, false, 8560.75",
            "100000, 40, 12, false, 10299.42",
            "100000, 40.01, 12, true, "
    })
    void rateBoundaries(BigDecimal principal, BigDecimal rate, int months, boolean expectException, BigDecimal expectedPayment) {
        if (expectException) {
            assertThrows(IllegalArgumentException.class,
                    () -> calculator.monthlyPayment(principal, rate, months));
        } else {
            assertEquals(expectedPayment, calculator.monthlyPayment(principal, rate, months));
        }
    }

    @Test
    void nullArgumentsThrow() {
        assertThrows(IllegalArgumentException.class,
                () -> calculator.monthlyPayment(null, new BigDecimal("12"), 12));
        assertThrows(IllegalArgumentException.class,
                () -> calculator.monthlyPayment(new BigDecimal("100000"), null, 12));
        assertThrows(IllegalArgumentException.class,
                () -> calculator.totalInterest(null, new BigDecimal("12"), 12));
        assertThrows(IllegalArgumentException.class,
                () -> calculator.totalInterest(new BigDecimal("100000"), null, 12));
    }
}