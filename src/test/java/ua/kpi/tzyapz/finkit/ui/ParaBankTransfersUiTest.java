package ua.kpi.tzyapz.finkit.ui;
import java.time.Duration;
import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.openqa.selenium.support.ui.ExpectedConditions.*;
@Tag("ui")
@DisplayName("ФінКит - сценарії 1–4 для ParaBank")
class ParaBankTransfersUiTest {
    private static final String BASE = "https://parabank.parasoft.com/parabank/";
    private static final String LOGIN = "fk_s4_" + System.currentTimeMillis();
    private static final String PASSWORD = "Test2026!";
    private WebDriver driver;
    private WebDriverWait wait;
    private static WebDriver browser() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new", "--window-size=1280,900");
        return new ChromeDriver(options);
    }
    @BeforeAll
    static void registerTestUsers() {
        WebDriver setup = browser();
        try {
            for (String username : new String[]{LOGIN, LOGIN + "b"}) {
                setup.manage().deleteAllCookies();
                setup.get(BASE + "register.htm");
                WebDriverWait ready = new WebDriverWait(setup, Duration.ofSeconds(20));
                ready.until(visibilityOfElementLocated(By.id("customer.firstName")));
                String[][] fields = {
                    {"customer.firstName", "Test"}, {"customer.lastName", "Client"},
                    {"customer.address.street", "QA Street 10"}, {"customer.address.city", "Testville"},
                    {"customer.address.state", "Teststate"}, {"customer.address.zipCode", "01001"},
                    {"customer.phoneNumber", "5550100001"}, {"customer.ssn", "999000001"},
                    {"customer.username", username}, {"customer.password", PASSWORD},
                    {"repeatedPassword", PASSWORD}
                };
                for (String[] field : fields) {
                    setup.findElement(By.id(field[0])).sendKeys(field[1]);
                }
                setup.findElement(By.cssSelector("#customerForm input[type=submit]")).click();
                ready.until(textToBe(By.cssSelector("#rightPanel h1"), "Welcome " + username));
            }
        } finally {
            setup.quit();
        }
    }
    @BeforeEach
    void startBrowser() {
        driver = browser();
        wait = new WebDriverWait(driver, Duration.ofSeconds(20));
    }
    @AfterEach
    void closeBrowser() {
        if (driver != null) driver.quit();
    }
    @Test
    @DisplayName("TC-005 · F-20 — вхід відкриває огляд рахунків")
    void validLoginOpensAccountOverview() {
        login(LOGIN);
        click(By.linkText("Accounts Overview"));
        assertEquals("Accounts Overview", visible(By.cssSelector("#rightPanel h1")).getText());
        assertTrue(visible(By.cssSelector("#accountTable tbody a")).isDisplayed());
        logout();
    }
    @Test
    @DisplayName("TC-035 - F-120 - після виходу пряме посилання не відкриває кабінет")
    void logoutProtectsOverview() {
        login(LOGIN);
        click(By.linkText("Accounts Overview"));
        visible(By.cssSelector("#accountTable tbody a"));
        String protectedUrl = driver.getCurrentUrl();
        logout();
        driver.get(protectedUrl);
        assertTrue(visible(By.name("username")).isDisplayed());
        assertTrue(driver.findElements(By.id("accountTable")).isEmpty());
        assertTrue(driver.findElements(By.linkText("Log Out")).isEmpty());
    }

    @Test
    @DisplayName("TC-017 · F-60 — відкриття ощадного рахунку з внеском 100.00")
    void savingsAccountReceivesOpeningDeposit() {
        login(LOGIN);
        String source = initialAccount();
        String savings = openSavings(source);
        click(By.id("newAccountId"));
        assertEquals(savings, visible(By.id("accountId")).getText());
        assertEquals("SAVINGS", visible(By.id("accountType")).getText());
        assertEquals("$100.00", visible(By.id("balance")).getText());
        click(By.linkText("Accounts Overview"));
        assertBalance(source, "1400.00");
        assertBalance(savings, "100.00");
        logout();
    }

    @Test
    @DisplayName("TC-019 · BL-40 — переказ 0.01 між власними рахунками")
    void transfersOneCentBetweenOwnAccounts() {
        login(LOGIN + "b");
        String source = initialAccount();
        String target = openSavings(source);
        click(By.linkText("Transfer Funds"));
        visible(By.id("amount")).sendKeys("0.01");
        selectAccount("fromAccountId", source);
        selectAccount("toAccountId", target);
        click(By.cssSelector("#transferForm input[type=submit]"));

        assertEquals("Transfer Complete!", visible(By.cssSelector("#showResult h1")).getText());
        assertEquals("$0.01", visible(By.id("amountResult")).getText());
        assertEquals(source, visible(By.id("fromAccountIdResult")).getText());
        assertEquals(target, visible(By.id("toAccountIdResult")).getText());
        click(By.linkText("Accounts Overview"));
        assertBalance(source, "1399.99");
        assertBalance(target, "100.01");
        logout();
    }

    private void login(String username) {
        driver.get(BASE + "index.htm");
        visible(By.name("username")).sendKeys(username);
        visible(By.name("password")).sendKeys(PASSWORD);
        click(By.cssSelector("#loginPanel input[type=submit]"));
        visible(By.linkText("Log Out"));
    }

    private void logout() {
        click(By.linkText("Log Out"));
        visible(By.name("username"));
    }

    private String initialAccount() {
        click(By.linkText("Accounts Overview"));
        String source = visible(By.cssSelector("#accountTable tbody a")).getText();
        assertBalance(source, "1500.00");
        return source;
    }

    private String openSavings(String source) {
        click(By.linkText("Open New Account"));
        new Select(visible(By.id("type"))).selectByVisibleText("SAVINGS");
        selectAccount("fromAccountId", source);
        click(By.cssSelector("#openAccountForm input[type=button]"));
        assertEquals("Account Opened!", visible(By.cssSelector("#openAccountResult h1")).getText());
        String number = visible(By.id("newAccountId")).getText();
        assertTrue(number.matches("[0-9]+"));
        assertNotEquals(source, number);
        return number;
    }

    private void selectAccount(String selectId, String account) {
        wait.until(presenceOfElementLocated(By.cssSelector("#" + selectId + " option[value='" + account + "']")));
        new Select(visible(By.id(selectId))).selectByValue(account);
    }

    private void assertBalance(String account, String expected) {
        By balance = By.xpath("//table[@id='accountTable']//tr[td/a[text()='" + account + "']]/td[2]");
        String amount = visible(balance).getText().replace(",", "");
        assertEquals("$" + expected, amount, "Баланс рахунку " + account);
    }

    private WebElement visible(By locator) {
        return wait.until(visibilityOfElementLocated(locator));
    }

    private void click(By locator) {
        wait.until(elementToBeClickable(locator)).click();
    }
}