# Матриця покриття BL: вимога → правило → тести → рецензент (модуль «ФінКит» v1)

> Файл: `docs/coverage-matrix.md`. Це **чернетка плану**: ID вимог і правила взято з README модуля;
> **звір кожен рядок із розділом BL виправленої SRS** (особливо BL-20 — з округленим чи неокругленим платежем рахується
> переплата — і те, що саме описує BL-60, якого в README модуля немає). Очікувані значення — з вимог, не з коду.
> Колонки «Автор», «Рецензент», «Статус» заповнюються по ходу спринта реальними даними (хто писав / хто перевірив).

Позначки: 🔴 — закладений дефект v1 (тест має падати на v1; потрібен звіт у трекері з міткою `module`).

## LoanCalculator (клас `LoanCalculatorTest`)

| BL | Правило | Метод | Планований тест | Дані / межі | Тип | Автор | Рецензент | Статус |
|---|---|---|---|---|---|---|---|---|
| BL-10 | A = P·i/(1−(1+i)^−n), i = r/12/100, округлення HALF_UP до копійок | `monthlyPayment` | `monthlyPaymentMatchesFormula` (параметризований) | 100000/12 %/12 → 8884.88; 🔴 100000/7 %/12 → 8652.67; 50000/15 %/24 → 2424.33; 1000000/40 %/60 → 38751.64 | норм. | | | |
| BL-20 | переплата = A·n − P | `totalInterest` | `totalInterestEqualsPaymentTimesMonthsMinusPrincipal` | 100000/12 %/12 → 6618.56 (з округленим A) або 6618.55 (без) — **звірити з SRS** | норм. | | | |
| BL-30 | P ∈ [1000; 1 000 000] | `monthlyPayment` | `principalBoundaries` (параметризований) | 999.99 → IAE; 1000 ✔; 1000000 ✔; 1000000.01 → IAE | межі | | | |
| BL-30 | n ∈ [3; 60] | `monthlyPayment` | `monthsBoundaries` (параметризований) | 2 → IAE; 🔴 3 ✔ (10000/12 %/3 → 3400.22); 60 ✔; 61 → IAE | межі | | | |
| BL-30 | r ∈ [5; 40] % | `monthlyPayment` | `rateBoundaries` (параметризований) | 4.99 → IAE; 5 ✔; 40 ✔; 40.01 → IAE | межі | | | |
| BL-30 | заборонений вхід | `monthlyPayment`, `totalInterest` | `nullArgumentsThrow` | null сума, null ставка → IAE | помилк. | | | |

## TransferValidator (клас `TransferValidatorTest`)

| BL | Правило | Метод | Планований тест | Дані / межі | Тип | Автор | Рецензент | Статус |
|---|---|---|---|---|---|---|---|---|
| BL-40 | amount > 0 | `validate` | `nonPositiveAmountIsRejected` (параметр.) | 0, −0.01, −1 → `AMOUNT_NOT_POSITIVE`; 0.01 — дозволено | межі | | | |
| BL-40 | amount ≤ balance | `validate` | `amountAboveBalanceIsRejected` | balance 1000: amount 1000 → ок; 1000.01 → `INSUFFICIENT_FUNDS` | межі | | | |
| BL-40 | todaySpent + amount ≤ 50 000 | `validate` | `dailyLimitBoundaries` (параметр.) | spent 0: 49999.99 ✔; 🔴 50000 ✔; 50000.01 → `DAILY_LIMIT_EXCEEDED`; spent 30000: 🔴 20000 ✔, 20000.01 ✗ | межі | | | |
| BL-40 | from ≠ to | `validate` | `sameAccountIsRejected` | однаковий валідний IBAN → `SAME_ACCOUNT` | негат. | | | |
| BL-40 | кілька порушень одразу | `validate` | `severalViolationsAreAllReported` (`assertAll`) | amount > balance і ліміт перевищено → обидва коди | комбін. | | | |
| BL-50 | IBAN = «UA» + 27 цифр (29 символів) | `isValidIban` | `ibanFormat` (параметр.) | валідний ✔; 28 і 30 символів ✗; «ua…» ✗; літера всередині ✗; порожній ✗; null ✗ | межі | | | |
| BL-50 | IBAN відправника | `validate` | `invalidSenderIban` | «UA123» → `INVALID_FROM_IBAN` | негат. | | | |
| BL-50 | 🔴 IBAN одержувача | `validate` | `invalidRecipientIban` | «UA123» → `INVALID_TO_IBAN` | негат. | | | |

## AccountLedger (клас `AccountLedgerTest`) — написано в `AccountLedgerTest.java`

| BL | Правило | Метод | Тест | Дані / межі | Тип | Автор | Рецензент | Статус |
|---|---|---|---|---|---|---|---|---|
| BL-70 | початковий стан | `balance`, `history` | `newLedgerIsEmpty` | 0.00; порожня історія | норм. | | | |
| BL-70 | баланс після операцій | `deposit`, `withdraw` | `depositThenPartialWithdrawChangesBalance` | 500 − 120.50 = 379.50 | норм. | | | |
| BL-70 | суми лише додатні | `deposit`, `withdraw` | `nonPositiveAmountsAreRejected` (параметр.) | 0, 0.00, −0.01, −100 → IAE | межі | | | |
| BL-70 | овердрафт заборонено | `withdraw` | `withdrawAboveBalanceIsRejected` | 100.01 з 100.00 → ISE, баланс не змінився | межі | | | |
| BL-70 | 🔴 списання рівно балансу дозволено | `withdraw` | `withdrawExactlyWholeBalanceIsAllowed` | 100.00 з 100.00 → баланс 0.00 | межі | | | |
| BL-80 | 🔴 порядок історії | `history` | `historyKeepsOperationOrder` | +10, +20, −5 → у цьому порядку; баланс після: 10, 30, 25 | норм. | | | |
| BL-80 | історія незмінювана | `history` | `historyIsUnmodifiable` | `add` → UnsupportedOperationException | негат. | | | |

## Підсумок плану (для 4 осіб: ≥ 16 юніт-тестів; параметризований метод = 1 тест)

| Клас | Тест-методів у плані |
|---|---|
| LoanCalculatorTest | 6 |
| TransferValidatorTest | 8 |
| AccountLedgerTest | 7 |
| **Разом** | **21** (норматив ≥ 16) |

## UI-сценарії (WebDriver) — повний перелік у `docs/ui-scenarios.md`

| TC (спринт 3) | Тест-метод | Клас | Автор | Рецензент | Статус |
|---|---|---|---|---|---|
| | | | | | |
