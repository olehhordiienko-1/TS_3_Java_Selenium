package UNI.UITesting.Testing;

import junit.framework.TestCase;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class LoginFormTest extends TestCase {
    private WebDriver driver;
    private final String BASE_URL = "https://the-internet.herokuapp.com/login";

    private void takeScreenshot(String testName) {
        File srcFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String fileName = "screenshots/task1/" + testName + "_" + timestamp + ".png";

        try {
            FileUtils.copyFile(srcFile, new File(fileName));
            System.out.println("Screenshot saved to: " + fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        System.setProperty("webdriver.chrome.driver", "C:\\chromedriver-win64\\chromedriver.exe");
        driver = new ChromeDriver();
    }

    public void test1_SuccessfulLogin() {
        driver.get(BASE_URL);

        WebElement username = driver.findElement(By.id("username"));
        WebElement password = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));

        username.sendKeys("tomsmith");
        password.sendKeys("SuperSecretPassword!");
        
        loginButton.click();
        
        takeScreenshot("test1_SuccessfulLogin");

        WebElement successMessage = driver.findElement(By.id("flash"));
        assertTrue(successMessage.getText().contains("You logged into a secure area!"));

        driver.findElement(By.cssSelector("a.button.secondary.radius")).click();
    }

    public void test2_InvalidUsername() {
        driver.get(BASE_URL);

        WebElement username = driver.findElement(By.id("username"));
        WebElement password = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));

        username.sendKeys("wronguser");
        password.sendKeys("SuperSecretPassword!");
        
        loginButton.click();
        
        takeScreenshot("test2_InvalidUsername");

        WebElement errorMessage = driver.findElement(By.id("flash"));
        assertTrue(errorMessage.getText().contains("Your username is invalid!"));
    }

    public void test3_InvalidPassword() {
        driver.get(BASE_URL);

        WebElement username = driver.findElement(By.id("username"));
        WebElement password = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));

        username.sendKeys("tomsmith");
        password.sendKeys("wrongpassword");

        loginButton.click();
        
        takeScreenshot("test3_InvalidPassword");
        
        WebElement errorMessage = driver.findElement(By.id("flash"));
        assertTrue(errorMessage.getText().contains("Your password is invalid!"));
    }

    public void test4_EmptyCredentials() {
        driver.get(BASE_URL);

        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));

        loginButton.click();
        
        takeScreenshot("test4_EmptyCredentials");

        WebElement errorMessage = driver.findElement(By.id("flash"));
        assertTrue(errorMessage.getText().contains("Your username is invalid!"));
    }

    public void test5_BoundaryValues() {
        Object[][] testCases = {
            {"Very long password (256 chars)", "tomsmith", new String(new char[256]).replace('\0', 'a'), "Your password is invalid!"},
            {"Special characters in password", "tomsmith", "!@#$%^&*()_+-=[]{};':\",./<>?\\|`~", "Your password is invalid!"},
            {"Minimum password length (1 char)", "tomsmith", "a", "Your password is invalid!"},
            {"Whitespace password", "tomsmith", "   ", "Your password is invalid!"},
            {"SQL injection attempt", "tomsmith", "' OR '1'='1", "Your password is invalid!"},
            {"Empty password", "tomsmith", "", "Your password is invalid!"},
            {"HTML tags in password", "tomsmith", "<script>alert('test')</script>", "Your password is invalid!"}
        };

        for (int i = 0; i < testCases.length; i++) {
            driver.get(BASE_URL);
            
            WebElement username = driver.findElement(By.id("username"));
            WebElement password = driver.findElement(By.id("password"));
            WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));

            String testDescription = (String) testCases[i][0];
            username.sendKeys((String) testCases[i][1]);
            password.sendKeys((String) testCases[i][2]);
            
            loginButton.click();
            takeScreenshot("test5_BoundaryValues_" + (i+1) + "_" + testDescription.replace(" ", "_"));

            WebElement errorMessage = driver.findElement(By.id("flash"));
            assertTrue("Test case " + (i+1) + " failed: " + testDescription, 
                     errorMessage.getText().contains((String) testCases[i][3]));
        }
    }

    @Override
    protected void tearDown() throws Exception {
        if (driver != null) {
            driver.quit();
        }
        super.tearDown();
    }
}
