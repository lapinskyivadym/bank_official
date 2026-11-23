package ua.kpi.tzyapz.finkit;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import static org.junit.jupiter.api.Assertions.assertEquals;

class TransferValidatorTest {
    private static final String FROM = "UA213223130000026007233566001";
    private static final String TO = "UA903052992990004149123456789";
    private final TransferValidator validator = new TransferValidator();
    @ParameterizedTest
    @CsvSource({"-1, false", "-0.01, false", "0, false", "0.01, true"})
    @DisplayName("BL-40 - сума переказу має бути додатною")
    void nonPositiveAmountIsRejected(String amount, boolean allowed) {
        var errors = validate(TO, amount, "1000", "0");
        assertEquals(allowed ? List.of() : List.of(TransferError.AMOUNT_NOT_POSITIVE), errors);
    }
    @ParameterizedTest
    @CsvSource({"999.99, true", "1000, true", "1000.01, false"})
    @DisplayName("BL-40 — можна переказати весь залишок включно")
    void amountAboveBalanceIsRejected(String amount, boolean allowed) {
        var errors = validate(TO, amount, "1000", "0");
        assertEquals(allowed ? List.of() : List.of(TransferError.INSUFFICIENT_FUNDS), errors);
    }
    //BUG-013 - модуль відхиляє переказ при досягненні денного ліміту рівно 50000 грн.
    @ParameterizedTest(name = "Вже переказано {0} грн, новий переказ {1} грн, дозволено = {2}")
    @CsvSource({"0, 49999.99, true", "0, 50000.00, true", "0, 50000.01, false",
                "30000, 19999.99, true", "30000, 20000.00, true", "30000, 20000.01, false"})
    @DisplayName("BL-40 — загальна сума переказів за день може дорівнювати 50000 грн")
    void dailyLimitBoundaries(String spent, String amount, boolean allowed) {
        var errors = validate(TO, amount, "60000", spent);
        assertEquals(allowed ? List.of() : List.of(TransferError.DAILY_LIMIT_EXCEEDED), errors);
    }
    @ParameterizedTest
    @CsvSource({FROM + ", false", TO + ", true"})
    @DisplayName("BL-40 - рахунки відправника й одержувача мають відрізнятися")
    void sameAccountIsRejected(String recipient, boolean allowed) {
        var errors = validate(recipient, "1", "1000", "0");
        assertEquals(allowed ? List.of() : List.of(TransferError.SAME_ACCOUNT), errors);
    }
    private List<TransferError> validate(String recipient, String amount, String balance, String spent) {
        return validator.validate(FROM, recipient, new BigDecimal(amount),
                new BigDecimal(balance), new BigDecimal(spent));
    }
}
