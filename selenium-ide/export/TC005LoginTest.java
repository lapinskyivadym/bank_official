import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsNot.not;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Alert;
import org.openqa.selenium.Keys;
import java.util.*;
import java.net.MalformedURLException;
import java.net.URL;

public class TC005LoginTest {
  private WebDriver driver;
  private Map<String, Object> vars;
  JavascriptExecutor js;
  
  @Before
  public void setUp() {
    driver = new ChromeDriver();
    js = (JavascriptExecutor) driver;
    vars = new HashMap<String, Object>();
  }
  @After
  public void tearDown() {
    driver.quit();
  }
  @Test
  public void tC005Login() {
    driver.get("https://parabank.parasoft.com/parabank/register.htm");
    driver.manage().window().setSize(new Dimension(1280, 900));
    vars.put("login", js.executeScript("return 'fk_s4_' + Date.now();"));
    {
      WebDriverWait wait = new WebDriverWait(driver, java.time.Duration.ofSeconds(20));
      wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("customer.firstName")));
    }
    driver.findElement(By.id("customer.firstName")).sendKeys("Test");
    driver.findElement(By.id("customer.lastName")).sendKeys("Client");
    driver.findElement(By.id("customer.address.street")).sendKeys("QA Street 10");
    driver.findElement(By.id("customer.address.city")).sendKeys("Testville");
    driver.findElement(By.id("customer.address.state")).sendKeys("Teststate");
    driver.findElement(By.id("customer.address.zipCode")).sendKeys("01001");
    driver.findElement(By.id("customer.phoneNumber")).sendKeys("5550100001");
    driver.findElement(By.id("customer.ssn")).sendKeys("999000001");
    driver.findElement(By.id("customer.username")).sendKeys(String.valueOf(vars.get("login")));
    driver.findElement(By.id("customer.password")).sendKeys("Test2026!");
    driver.findElement(By.id("repeatedPassword")).sendKeys("Test2026!");
    driver.findElement(By.cssSelector("#customerForm input[type=submit]")).click();
    {
      WebDriverWait wait = new WebDriverWait(driver, java.time.Duration.ofSeconds(20));
      wait.until(ExpectedConditions.visibilityOfElementLocated(By.linkText("Log Out")));
    }
    assertThat(driver.findElement(By.cssSelector("#rightPanel h1")).getText(), is("Welcome " + String.valueOf(vars.get("login"))));
    driver.findElement(By.linkText("Log Out")).click();
    {
      WebDriverWait wait = new WebDriverWait(driver, java.time.Duration.ofSeconds(20));
      wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
    }
    driver.findElement(By.name("username")).sendKeys(String.valueOf(vars.get("login")));
    driver.findElement(By.name("password")).sendKeys("Test2026!");
    driver.findElement(By.cssSelector("#loginPanel input[type=submit]")).click();
    {
      WebDriverWait wait = new WebDriverWait(driver, java.time.Duration.ofSeconds(20));
      wait.until(ExpectedConditions.visibilityOfElementLocated(By.linkText("Accounts Overview")));
    }
    driver.findElement(By.linkText("Accounts Overview")).click();
    {
      WebDriverWait wait = new WebDriverWait(driver, java.time.Duration.ofSeconds(20));
      wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#accountTable tbody a")));
    }
    assertThat(driver.findElement(By.cssSelector("#rightPanel h1")).getText(), is("Accounts Overview"));
    driver.findElement(By.linkText("Log Out")).click();
    {
      WebDriverWait wait = new WebDriverWait(driver, java.time.Duration.ofSeconds(20));
      wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
    }
    {
      List<WebElement> elements = driver.findElements(By.name("password"));
      assert(elements.size() > 0);
    }
  }
}