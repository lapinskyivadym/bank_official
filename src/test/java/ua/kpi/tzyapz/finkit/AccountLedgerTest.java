package ua.kpi.tzyapz.finkit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Юніт-тести AccountLedger: BL-70 (овердрафт заборонено, суми лише додатні) і
 * BL-80 (історія операцій у порядку виконання).
 *
 * ВАЖЛИВО: ID вимог BL-70 / BL-80 і очікувані значення звірте з виправленою SRS «ФінКит»
 * (розділ BL) — тут вони взяті з README модуля. Очікувані значення НЕ підганяти під код.
 *
 * Тести, що на v1 «червоні» (дефекти модуля), позначені коментарем «ДЕФЕКТ»:
 * після створення звіту в трекері впишіть сюди його ID.
 */
@Tag("unit")
@DisplayName("AccountLedger — облік рахунку (BL-70, BL-80)")
class AccountLedgerTest {

    private AccountLedger ledger;

    @BeforeEach
    void setUp() {
        ledger = new AccountLedger();
    }

    @AfterEach
    void tearDown() {
        ledger = null;
    }

    @Test
    @DisplayName("BL-70: новий рахунок має нульовий баланс 0.00, історія порожня")
    void newLedgerIsEmpty() {
        assertAll("новий рахунок",
                () -> assertEquals(new BigDecimal("0.00"), ledger.balance()),
                () -> assertTrue(ledger.history().isEmpty()));
    }

    @Test
    @DisplayName("BL-70: зарахування і часткове списання змінюють баланс: 500 − 120.50 = 379.50")
    void depositThenPartialWithdrawChangesBalance() {
        // arrange
        ledger.deposit(new BigDecimal("500"));
        // act
        ledger.withdraw(new BigDecimal("120.50"));
        // assert (379.50 = 500.00 − 120.50, обчислено вручну)
        assertEquals(new BigDecimal("379.50"), ledger.balance());
    }

    @ParameterizedTest(name = "BL-70: операція із сумою {0} відхиляється (IllegalArgumentException)")
    @ValueSource(strings = {"0", "0.00", "-0.01", "-100"})
    void nonPositiveAmountsAreRejected(String amount) {
        BigDecimal value = new BigDecimal(amount);
        assertAll("недодатна сума",
                () -> assertThrows(IllegalArgumentException.class, () -> ledger.deposit(value)),
                () -> assertThrows(IllegalArgumentException.class, () -> ledger.withdraw(value)));
    }

    @Test
    @DisplayName("BL-70: списання більше за баланс (100.01 з 100.00) — овердрафт заборонено")
    void withdrawAboveBalanceIsRejected() {
        ledger.deposit(new BigDecimal("100.00"));

        assertThrows(IllegalStateException.class, () -> ledger.withdraw(new BigDecimal("100.01")));
        assertEquals(new BigDecimal("100.00"), ledger.balance(), "баланс не має змінитись після відмови");
    }

    @Test
    @DisplayName("BL-70: списання рівно всього балансу дозволено (межа amount = balance)")
    void withdrawExactlyWholeBalanceIsAllowed() {
        // ДЕФЕКТ (v1): модуль відхиляє списання, коли сума дорівнює балансу. ID звіту: BUG-008 (Issue #9)
        ledger.deposit(new BigDecimal("100.00"));

        assertDoesNotThrow(() -> ledger.withdraw(new BigDecimal("100.00")),
                "овердрафту немає: сума рівно дорівнює балансу");
        assertEquals(new BigDecimal("0.00"), ledger.balance());
    }

    @Test
    @DisplayName("BL-80: історія зберігає порядок виконання операцій і баланс після кожної")
    void historyKeepsOperationOrder() {
        // ДЕФЕКТ (v1): історія повертається у зворотному порядку. ID звіту: BUG-009 (Issue #10)
        ledger.deposit(new BigDecimal("10"));
        ledger.deposit(new BigDecimal("20"));
        ledger.withdraw(new BigDecimal("5"));

        List<LedgerEntry> expected = List.of(
                new LedgerEntry(LedgerEntry.Type.DEPOSIT, new BigDecimal("10.00"), new BigDecimal("10.00")),
                new LedgerEntry(LedgerEntry.Type.DEPOSIT, new BigDecimal("20.00"), new BigDecimal("30.00")),
                new LedgerEntry(LedgerEntry.Type.WITHDRAWAL, new BigDecimal("5.00"), new BigDecimal("25.00")));

        assertIterableEquals(expected, ledger.history());
    }

    @Test
    @DisplayName("BL-80: повернену історію не можна змінити ззовні")
    void historyIsUnmodifiable() {
        ledger.deposit(new BigDecimal("10"));

        List<LedgerEntry> history = ledger.history();
        LedgerEntry extra = new LedgerEntry(LedgerEntry.Type.DEPOSIT, BigDecimal.ONE, BigDecimal.ONE);

        assertThrows(UnsupportedOperationException.class, () -> history.add(extra));
    }
}