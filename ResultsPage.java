package UNI.UITesting.Testing.elmirua;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

class ResultsPage {
	
	public static final String CONST_CATEGORY_MONITORS = "Монітори";
	public static final String CONST_CATEGORY_LAPTOPS = "Ноутбуки";
	public static final String CONST_CATEGORY_TVS = "Телевізори";
	public static final String CONST_CATEGORY_SMARTPHONES = "Мобільні телефони";
	
	private WebDriver driver;
	private final WebDriverWait wait;

	public ResultsPage(WebDriver driver, long seconds) {
		this.driver = driver;
		this.wait = new WebDriverWait(driver, Duration.ofSeconds(seconds));
	}

	public ResultsPage clickCategoryByName(String categoryName) {
		String xpath = String.format("//li[contains(@class,'item-product')]//a[span[normalize-space(text())='%s']]",
				categoryName);
		WebElement categoryLink = driver.findElement(By.xpath(xpath));
		
		categoryLink.click();
		return this;
	}

	/*
	 * 
	 * BRAND SELECTION
	 * 
	 */
	public ResultsPage selectBrand(String brandName) {
	    toggleShowAllFilters();
	    
	    // First convert brand name to lowercase for comparison
	    String lowerBrand = brandName.toLowerCase();
	    
	    // Find all brand links and filter using Java
	    List<WebElement> brandLinks = driver.findElements(
	        By.xpath("//a[contains(@class,'filter-value-link')]//input[@type='checkbox']/..")
	    );
	    
	    for (WebElement brandLink : brandLinks) {
	        WebElement checkbox = brandLink.findElement(By.xpath(".//input[@type='checkbox']"));
	        String value = checkbox.getDomAttribute("value");
	        if (value != null && value.toLowerCase().contains(lowerBrand)) {
	            return selectCheckboxFromListItem(brandLink);
	        }
	    }
	    
	    throw new NoSuchElementException("Brand containing '" + brandName + "' not found (case insensitive)");
	}
	
	public ResultsPage selectCheckboxFromListItem(WebElement listItem) {
	    try {
	        // Find the checkbox which is a direct child of the listItem (a element)
	        WebElement checkbox = listItem.findElement(By.xpath("./input[@type='checkbox']"));

	        ((JavascriptExecutor) driver)
	                .executeScript("arguments[0].scrollIntoView({block: 'center', behavior: 'smooth'});", listItem);

	        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", checkbox);

	        waitForFilterToApply();

	        clickApplyFiltersButton();
	        
	        return this;
	    } catch (Exception e) {
	        throw new RuntimeException("Failed to select checkbox in list item", e);
	    }
	}
	
	public String getSelectedBrand(String brandName) {
	    String lowerBrand = brandName.toLowerCase();
	    
	    List<WebElement> brandCheckboxes = driver.findElements(
	        By.xpath("//a[contains(@class,'filter-value-link')]//input[@type='checkbox']")
	    );
	    
	    for (WebElement checkbox : brandCheckboxes) {
	        String value = checkbox.getDomAttribute("value");
	        if (value != null && value.toLowerCase().contains(lowerBrand)) {
	            return checkbox.getDomAttribute("checked");
	        }
	    }
	    
	    throw new NoSuchElementException("Brand checkbox containing '" + brandName + "' not found");
	}
	
	public ResultsPage clickApplyFiltersButton() {
	    try {
	        // Wait for the button to be present (you might want to adjust the timeout)
	        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
	        WebElement applyButton = wait.until(ExpectedConditions.elementToBeClickable(
	            By.xpath("//div[@id='submit-filter']/button[contains(@class, 'button') and @name='gofilter']")
	        ));

	        // Scroll to the button
	        ((JavascriptExecutor) driver).executeScript(
	            "arguments[0].scrollIntoView({block: 'center', behavior: 'smooth'});", 
	            applyButton
	        );

	        // Click using JavaScript to avoid potential interception issues
	        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", applyButton);

	        // Wait for filters to be applied (you might need to adjust this)
	        waitForFilterToApply();

	        return this;
	    } catch (Exception e) {
	        throw new RuntimeException("Failed to click apply filters button", e);
	    }
	}


	/*
	 * 
	 * PRICE SELECTION
	 * 
	 */
	public ResultsPage setPriceRange(String minPrice, String maxPrice) {
		WebElement minInput = driver.findElement(By.id("minPrice"));
		WebElement maxInput = driver.findElement(By.id("maxPrice"));

		// Clear and set values using direct JavaScript for maximum speed
		((JavascriptExecutor) driver).executeScript("arguments[0].value = ''; arguments[0].value = arguments[1];",
				minInput, minPrice);
		((JavascriptExecutor) driver).executeScript("arguments[0].value = ''; arguments[0].value = arguments[1];",
				maxInput, maxPrice);

		// Trigger change events if needed
		((JavascriptExecutor) driver).executeScript("arguments[0].dispatchEvent(new Event('change'))", minInput);
		
		submitPriceFilter();
		
		return this;
	}

	public void submitPriceFilter() {
		driver.findElement(By.xpath("//button[@name='gofilter']")).click();
	}

	private void waitForFilterToApply() {
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public Map<String, Integer> getDefaultPriceRange() {
		Map<String, Integer> prices = new HashMap<>();

		WebElement minInput = driver.findElement(By.id("minPrice"));
		WebElement maxInput = driver.findElement(By.id("maxPrice"));

		// Get placeholder values which represent defaults
		prices.put("min", Integer.parseInt(minInput.getDomAttribute("placeholder")));
		prices.put("max", Integer.parseInt(maxInput.getDomAttribute("placeholder")));

		return prices;
	}

	public Map<String, String> getAppliedPriceRange() {
		Map<String, String> prices = new HashMap<>();

		WebElement minInput = driver.findElement(By.id("minPrice"));
		WebElement maxInput = driver.findElement(By.id("maxPrice"));

		// Get current values which represent applied prices
		prices.put("min", minInput.getDomAttribute("value"));
		prices.put("max", maxInput.getDomAttribute("value"));

		return prices;
	}

	/*
	 * 
	 * SORTING
	 * 
	 */
	private WebElement getSortingRoot() {
		return wait.until(ExpectedConditions.presenceOfElementLocated(By.id("top-paging")));
	}

	private WebElement openOrderingMethods() {
		WebElement root = getSortingRoot();
		WebElement current = root.findElement(By.className("current"));
		current.click();
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("select-block")));

		return root;
	}

	public ResultsPage selectOrderingMethod(String methodName) {
		WebElement root = openOrderingMethods();

		List<WebElement> options = root.findElements(By.className("sb-item"));

		for (WebElement option : options) {
			if (option.getText().equals(methodName)) {
				option.click();
				wait.until(ExpectedConditions.stalenessOf(option));
				return this;
			}
		}

		throw new IllegalArgumentException("Ordering method '" + methodName + "' not found");
	}

	public String getSelectedOrderingMethod() {
		WebElement root = getSortingRoot();
		WebElement current = root.findElement(By.className("current"));
		return current.getText();
	}

	public void verifySelectedOrderingMethod(String expectedMethod) {
		String actual = getSelectedOrderingMethod();
		Assert.assertEquals("Selected ordering method doesn't match expected", expectedMethod, actual);
	}

	public List<String> getAllOrderingMethods() {
		WebElement root = openOrderingMethods();

		List<WebElement> options = root.findElements(By.className("sb-item"));
		return options.stream().map(WebElement::getText).filter(text -> !text.isEmpty()).collect(Collectors.toList());
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////////////

	/*
	 * 
	 * SCREEN SIZE SELECTION
	 * 
	 * 
	 */
	
	public ResultsPage selectScreensize(String category, String screenSize) {
		WebElement list = null;
		switch(category) {
		case CONST_CATEGORY_LAPTOPS:
			list = getLaptopsScreensizeSelection();
			break;
		case CONST_CATEGORY_MONITORS:
		case CONST_CATEGORY_TVS:
			list = getMonitorsAndTvScreensizeSelection();
			break;
		case CONST_CATEGORY_SMARTPHONES:
			list = getSmartphonesScreensizeSelection();
			break;
		}
		
		WebElement screensize = list.findElement(By.xpath(String
				.format("//a[contains(@class,'filter-value-link')][.//label[contains(.,'%s')]]", screenSize)));
		selectCheckboxFromListItem(screensize);
		
		return this;	
	}
	
	public String getScreensize(String category, String screenSize) {
		WebElement list = null;
		switch(category) {
		case CONST_CATEGORY_LAPTOPS:
			list = getLaptopsScreensizeSelection();
			break;
		case CONST_CATEGORY_MONITORS:
		case CONST_CATEGORY_TVS:
			list = getMonitorsAndTvScreensizeSelection();
			break;
		case CONST_CATEGORY_SMARTPHONES:
			list = getSmartphonesScreensizeSelection();
			break;
		}
		
		WebElement screensize = list.findElement(By.xpath(String
				.format("//a[contains(@class,'filter-value-link')][.//label[contains(.,'%s')]]", screenSize)));

		WebElement checkbox = screensize.findElement(By.xpath("./input[@type='checkbox' and @checked='checked']"));
		
		return checkbox.getDomAttribute("checked");
	}
	
	// FIXME OLEH: BAD PRACTISE to use ID
	public WebElement getLaptopsScreensizeSelection() {
		return driver.findElement(By.id("list-4"));
	}
	
	public WebElement getSmartphonesScreensizeSelection() {
		return driver.findElement(By.id("list-6"));
	}
	
	public WebElement getMonitorsAndTvScreensizeSelection() {
		return driver.findElement(By.id("list-3"));
	}
	
	public int getProductsCount() {
	    try {
	        WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(5));

	        // First check for "not found" message
	        List<WebElement> notFoundElements = driver.findElements(
	            By.xpath("//div[@id='search-header']//li[contains(@class,'not-found') and contains(.,'Не знайдено')]")
	        );
	        
	        if (!notFoundElements.isEmpty()) {
	            return 0;
	        }

	        // Try primary count location first
	        try {
	            WebElement countElement = shortWait.until(ExpectedConditions.visibilityOfElementLocated(
	                By.xpath("//div[@id='search-header']//span[contains(@class,'total') and contains(.,'Знайдені')]")
	            ));

	            String countText = countElement.getText().replaceAll("[^0-9]", "");
	            return countText.isEmpty() ? 0 : Integer.parseInt(countText);
	            
	        } catch (TimeoutException e1) {
	            // If primary count not found, try alternative location
	            try {
	                WebElement altCountElement = shortWait.until(ExpectedConditions.visibilityOfElementLocated(
	                    By.xpath("//div[@id='div-fix-2']//span[contains(@class,'cat-count')]")
	                ));

	                String countText = altCountElement.getText().replaceAll("[^0-9]", "");
	                return countText.isEmpty() ? 0 : Integer.parseInt(countText);

	            } catch (TimeoutException e2) {
	                // If neither count element found, count actual products as fallback
	                List<WebElement> productElements = driver.findElements(
	                    By.xpath("//div[contains(@class,'item-product')]")
	                );
	                
	                return productElements.size();
	            }
	        }

	    } catch (Exception e) {
	        Util.takeScreenshot(driver, "ResultsPage", "failed_getProductsCount");
	        throw new RuntimeException("Failed to get products count: " + e.getMessage(), e);
	    }
	}
	
	public ResultsPage toggleShowAllFilters() {
	    try {
	        WebElement toggleElement = driver.findElement(
	            By.xpath("//ul[@id='list-0']/li[contains(@class,'shower')]")
	        );

	        // Get current state
	        boolean isExpanded = toggleElement.findElement(By.xpath(".//span[contains(@class,'more')]"))
	                                .isDisplayed();

	        // Scroll and click
	        ((JavascriptExecutor) driver).executeScript(
	            "arguments[0].scrollIntoView({block: 'center', behavior: 'smooth'});", 
	            toggleElement
	        );
	        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", toggleElement);

	        // Wait for state change
	        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(2));
	        if (isExpanded) {
	            wait.until(ExpectedConditions.visibilityOfElementLocated(
	                By.xpath("//ul[@id='list-0']//span[contains(@class,'less')]")
	            ));
	        } else {
	            wait.until(ExpectedConditions.visibilityOfElementLocated(
	                By.xpath("//ul[@id='list-0']//span[contains(@class,'more')]")
	            ));
	        }

	        return this;
	    } catch (Exception e) {
	        Util.takeScreenshot(driver, "ResultsPage", "failed_toggleShowAll");
	        throw new RuntimeException("Failed to toggle 'Show All' for list: " + e.getMessage(), e);
	    }
	}
	
	public boolean doesElementContainText(String expectedText) {
        try {
            WebElement element = driver.findElement(By.id("page-title"));
            String actualText = element.getText().toLowerCase();
            return actualText.contains(expectedText.toLowerCase());
        } catch (Exception e) {
        	throw new RuntimeException("Failed to find element 'page-title': " + e.getMessage(), e);
        }
    }
}
