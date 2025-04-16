package UNI.UITesting.Testing.elmirua;

import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

class HomePage {
	private String URL;
	private WebDriver driver;
    private final WebDriverWait wait;
    
    public HomePage(WebDriver driver, String URL, long waitSeconds) {
    	this.URL = URL;
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(waitSeconds));
    }

    public HomePage openHomePage() {
        driver.get(URL);
        return this;
    }

    public ResultsPage enterSearchRequest(String searchStr) {
    	try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    	
    	WebElement search = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("srch")));
    	
        WebElement field = search.findElement(By.id("q"));
        field.clear();
        field.sendKeys(searchStr);

        WebElement searchButton = search.findElement(By.id("find"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", searchButton);
            
        return new ResultsPage(driver, 5);
    }
}
