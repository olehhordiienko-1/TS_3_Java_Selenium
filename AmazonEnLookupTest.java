package UNI.UITesting.Testing.ecommerce;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;

import junit.framework.TestCase;

public class AmazonEnLookupTest extends TestCase {

	protected WebDriver driver;

	protected void setUp() throws Exception {
		System.setProperty("webdriver.chrome.driver", "C:\\chromedriver-win64\\chromedriver.exe");
		driver = new ChromeDriver();
	}

	protected void takeScreenshot(String testName) {
		File srcFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
		String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		String fileName = "screenshots/ecommerce/" + testName + "_" + timestamp + ".png";

		try {
			FileUtils.copyFile(srcFile, new File(fileName));
			System.out.println("Screenshot saved to: " + fileName);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	protected final String BASE_URL = "https://www.amazon.com/";

	protected static HashMap<String, String> brandsNameId = new HashMap<>();

	static {
		brandsNameId.put("ACER", "p_123/247341");
		brandsNameId.put("HP", "p_123/308445");
		brandsNameId.put("LENOVO", "p_123/391242");
		brandsNameId.put("APPLE", "p_123/110955");
	}

	public void testDecisionTable() {
		try {
			Object[][] testCases = {
					{ "Monitors", SortOption.FEATURED, "LENOVO", DiscountType.ALL_DISCOUNTS, "100", "600" },
					{ "Tablets", SortOption.CUSTOMER_REVIEW, "APPLE", DiscountType.TODAYS_DEALS, "400", "2000" },
					{ "Laptops", SortOption.PRICE_LOW_TO_HIGH, "ACER", DiscountType.NONE, "300", "1000" },
					{ "Laptops", SortOption.PRICE_HIGH_TO_LOW, "HP", DiscountType.NONE, "500", "1500" } };

			for (Object[] testCase : testCases) {
				String category = (String) testCase[0];
				SortOption sortOption = (SortOption) testCase[1];
				String brand = (String) testCase[2];
				String minPrice = (String) testCase[4];
				String maxPrice = (String) testCase[5];

				ResultsPage resultsPage = new HomePage(driver, 5, 10)
						.openHomePage()
						.enterSearchRequest(category)
						.selectSortingMethod(sortOption)
						.selectPriceRange(Integer.parseInt(minPrice), Integer.parseInt(maxPrice))
						.selectBrand(brand)
						// .selectDiscountType(discount)
						;

				takeScreenshot("DecisionTable_" + category + "_" + sortOption.name());

				String currentSort = resultsPage.getCurrentSortingMethod();
				assertEquals(sortOption.getDisplayName(), currentSort);

				assertTrue(resultsPage.getCurrentPriceRange().equals(resultsPage.settedPrice));
			}
		} catch (Exception e) {
			e.printStackTrace();
			fail("Test failed: " + e.getMessage());
		}
	}

	class HomePage {
		private WebDriver driver;
		private final WebDriverWait wait;
		private long waitResultsPageSeconds;

		public HomePage(WebDriver driver, long waitSeconds, long waitResultsPageSeconds) {
			this.driver = driver;
			this.waitResultsPageSeconds = waitResultsPageSeconds;
			this.wait = new WebDriverWait(driver, Duration.ofSeconds(waitSeconds));
		}

		public HomePage openHomePage() {
			driver.get(BASE_URL);
			return this;
		}

		public ResultsPage enterSearchRequest(String search) {
			WebElement field = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("twotabsearchtextbox")));
			field.clear();
			field.sendKeys(search);
			WebElement searchButton = wait
					.until(ExpectedConditions.elementToBeClickable(By.id("nav-search-submit-button")));
			searchButton.click();

			return new ResultsPage(driver, waitResultsPageSeconds);
		}
	}

	class ResultsPage {
		private WebDriver driver;
		private final WebDriverWait wait;

		public ResultsPage(WebDriver driver, long seconds) {
			this.driver = driver;
			this.wait = new WebDriverWait(driver, Duration.ofSeconds(seconds));
		}

		/*
		 * BRAND SELECTION
		 * 
		 */
		public ResultsPage selectBrand(String brandName) {
			// expand see more
			try {
				WebElement seeMore = driver.findElement(By.xpath(
						"//a[contains(@class, 'a-expander-header') and contains(@aria-label, 'See more, Brands')]"));
				((JavascriptExecutor) driver).executeScript("arguments[0].click();", seeMore);
			} catch (NoSuchElementException e) {
				// ignore !
			}
			WebElement brandLabel = wait.until(
					ExpectedConditions.visibilityOfElementLocated(By.id(brandsNameId.get(brandName.toUpperCase()))));

			return selectCheckboxFromListItem(brandLabel);
		}

		public ResultsPage selectCheckboxFromListItem(WebElement listItem) {
			try {

				WebElement checkbox = listItem.findElement(By.xpath(".//input[@type='checkbox']"));

				((JavascriptExecutor) driver)
						.executeScript("arguments[0].scrollIntoView({block: 'center', behavior: 'smooth'});", listItem);

				((JavascriptExecutor) driver).executeScript("arguments[0].click();", checkbox);
				
				waitForFilterToApply();

				return this;
			} catch (Exception e) {

				takeScreenshot("failed_checkbox_selection");
				throw new RuntimeException("Failed to select checkbox in list item", e);
			}
		}

		private void waitForFilterToApply() {
			wait.until(ExpectedConditions
					.invisibilityOfElementLocated(By.xpath("//div[contains(@class, 'a-spinner-wrapper')]")));
		}

		/*
		 * SORTING
		 * 
		 * 
		 */
		public ResultsPage selectSortingMethod(SortOption sortOption) {
			try {
				
				WebElement sortDropdown = wait
						.until(ExpectedConditions.presenceOfElementLocated(By.id("s-result-sort-select")));

				
				Select dropdown = new Select(sortDropdown);

				
				dropdown.selectByValue(sortOption.getValue());

				
				waitForResultsToReload();

				return this;
			} catch (Exception e) {
				takeScreenshot("failed_sort_selection_" + sortOption.name());
				throw new RuntimeException("Failed to select sorting method: " + sortOption.getDisplayName(), e);
			}
		}

		public String getCurrentSortingMethod() {
			try {
				WebElement sortDropdown = wait
						.until(ExpectedConditions.presenceOfElementLocated(By.id("s-result-sort-select")));
				Select dropdown = new Select(sortDropdown);
				return dropdown.getFirstSelectedOption().getText();
			} catch (Exception e) {
				takeScreenshot("failed_get_current_sort");
				throw new RuntimeException("Failed to get current sorting method", e);
			}
		}

		private void waitForResultsToReload() {
		
			try {
			
				wait.until(ExpectedConditions
						.invisibilityOfElementLocated(By.xpath("//div[contains(@class, 'a-spinner-wrapper')]")));

				wait.until(ExpectedConditions
						.presenceOfElementLocated(By.xpath("//div[contains(@class, 's-result-item')]")));
			} catch (TimeoutException e) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException ie) {
				}
			}
		}

		/*
		 * PRICE SELECTION
		 * 
		 */
		protected String settedPrice = "";

		public ResultsPage selectPriceRange(int minPrice, int maxPrice) {
			try {
				Gson gson = new Gson();

				WebElement priceSlider2 = wait.until(ExpectedConditions.visibilityOfElementLocated(
						By.xpath("//form[contains(@data-slider-id, 'p_36/range-slider')]")));
				String sliderProps = priceSlider2.getDomAttribute("data-slider-props");
				SliderProperties sliderConfig = gson.fromJson(sliderProps, SliderProperties.class);

				int minStep = findClosestStepIndex(minPrice, sliderConfig.getStepValues());
				int maxStep = findClosestStepIndex(maxPrice, sliderConfig.getStepValues());

				String actualMinPrice = sliderConfig.getStepLabels()[minStep];
				String actualMaxPrice = sliderConfig.getStepLabels()[maxStep];

				setSliderValue(By.id("p_36/range-slider_slider-item_lower-bound-slider"), minStep, actualMinPrice);
				setSliderValue(By.id("p_36/range-slider_slider-item_upper-bound-slider"), maxStep, actualMaxPrice);

				settedPrice = actualMinPrice + " - " + actualMaxPrice;

				submitPriceFilter();
				waitForPriceFilterResults();

				return this;
			} catch (JsonSyntaxException e) {
				takeScreenshot("price_slider_parse_error");
				throw new RuntimeException("Failed to parse price slider properties", e);
			} catch (Exception e) {
				takeScreenshot("failed_price_range_selection");
				throw new RuntimeException(String.format("Failed to select price range: $%d - $%d", minPrice, maxPrice),
						e);
			}
		}

		private int findClosestStepIndex(int targetPrice, Integer[] stepValues) {
			int closestIndex = 0;
			int smallestDiff = Integer.MAX_VALUE;

			for (int i = 0; i < stepValues.length; i++) {
				if (stepValues[i] != null) {
					int diff = Math.abs(stepValues[i] - targetPrice);
					if (diff < smallestDiff) {
						smallestDiff = diff;
						closestIndex = i;
					}
				}
			}
			return closestIndex;
		}

		private void setSliderValue(By locator, int stepIndex, String expectedPriceText) {
			WebElement slider = wait.until(ExpectedConditions.presenceOfElementLocated(locator));

			((JavascriptExecutor) driver).executeScript("arguments[0].value = arguments[1]; "
					+ "arguments[0].setAttribute('aria-valuetext', arguments[2]); "
					+ "arguments[0].setAttribute('aria-valuenow', arguments[1]); "
					+ "const event = new Event('change', { bubbles: true }); " + "arguments[0].dispatchEvent(event);",
					slider, stepIndex, expectedPriceText);

			wait.until(ExpectedConditions.attributeContains(locator, "aria-valuetext", expectedPriceText));
		}

		private void submitPriceFilter() {
			WebElement submitButton = wait.until(ExpectedConditions
					.elementToBeClickable(By.xpath("//input[@aria-label='Go - Submit price range']")));
			((JavascriptExecutor) driver).executeScript("arguments[0].click();", submitButton);
		}

		private void waitForPriceFilterResults() {
			try {
				wait.until(ExpectedConditions
						.invisibilityOfElementLocated(By.xpath("//div[contains(@class, 'a-spinner-wrapper')]")));

				wait.until(ExpectedConditions
						.presenceOfElementLocated(By.xpath("//span[contains(@class, 'a-price-whole')]")));
			} catch (TimeoutException e) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException ie) {
				}
			}
		}

		public String getCurrentPriceRange() {
			try {
				List<WebElement> priceLabels = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.xpath(
						"//label[contains(@class, 'sf-range-slider-label')]//span[contains(@class, 'a-text-bold')]")));

				return priceLabels.get(0).getText() + " - " + priceLabels.get(1).getText();
			} catch (Exception e) {
				takeScreenshot("failed_get_price_range");
				return "Price range not available";
			}
		}

		/*
		 * 
		 * DISCOUNT TYPE
		 * 
		 * 
		 */

		public ResultsPage selectDiscountType(DiscountType discountType) {
			try {
				if (discountType == DiscountType.NONE) {
					return this;
				}
				wait.until(ExpectedConditions
						.visibilityOfElementLocated(By.xpath("//span[@data-action='sf-select-refinement-picker']")));


				String xpath = String.format(
						"//li[contains(@id, 'p_n_deal_type/')]//span[contains(@class, 'a-size-base') and normalize-space()='%s']/ancestor::a",
						discountType.getDisplayName());

				WebElement discountLink = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(xpath)));

				((JavascriptExecutor) driver).executeScript(
						"arguments[0].scrollIntoView({block: 'center', behavior: 'smooth'});", discountLink);

				((JavascriptExecutor) driver).executeScript("arguments[0].click();", discountLink);

				waitForDiscountFilterToApply();

				return this;
			} catch (Exception e) {
				takeScreenshot("failed_select_discount_" + discountType.name());
				throw new RuntimeException("Failed to select discount type: " + discountType.getDisplayName(), e);
			}
		}

		private void waitForDiscountFilterToApply() {
			try {
				wait.until(ExpectedConditions
						.invisibilityOfElementLocated(By.xpath("//div[contains(@class, 'a-spinner-wrapper')]")));

				wait.until(ExpectedConditions
						.presenceOfElementLocated(By.xpath("//div[contains(@class, 's-result-item')]")));
			} catch (TimeoutException e) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException ie) {
				}
			}
		}

		public boolean isDiscountTypeSelected(DiscountType discountType) {
			if (discountType == DiscountType.NONE) {
				return true;
			}
			try {
				String xpath = String.format(
						"//li[@id='p_n_deal_type/%s']//a[contains(@class, 's-navigation-item') and contains(@aria-current, 'true')]",
						discountType.getDealTypeId());
				return !driver.findElements(By.xpath(xpath)).isEmpty();
			} catch (Exception e) {
				takeScreenshot("failed_check_discount_selection");
				return false;
			}
		}

		public String getSelectedDiscountType() {
			try {
				WebElement selectedDiscount = wait.until(ExpectedConditions.presenceOfElementLocated(
						By.xpath("//li[contains(@id, 'p_n_deal_type/')]//a[contains(@aria-current, 'true')]//span")));
				return selectedDiscount.getText();
			} catch (Exception e) {
				takeScreenshot("failed_get_selected_discount");
				return "No discount filter applied";
			}
		}
	}

	public enum SortOption {
		FEATURED("Featured", "relevanceblender"), 
		PRICE_LOW_TO_HIGH("Price: Low to High", "price-asc-rank"),
		PRICE_HIGH_TO_LOW("Price: High to Low", "price-desc-rank"),
		CUSTOMER_REVIEW("Avg. Customer Review", "review-rank"), 
		NEWEST_ARRIVALS("Newest Arrivals", "date-desc-rank"),
		BEST_SELLERS("Best Sellers", "exact-aware-popularity-rank");

		private final String displayName;
		private final String value;

		SortOption(String displayName, String value) {
			this.displayName = displayName;
			this.value = value;
		}

		public String getDisplayName() {
			return displayName;
		}

		public String getValue() {
			return value;
		}
	}

	public enum DiscountType {
		ALL_DISCOUNTS("All Discounts", "23566065011"), TODAYS_DEALS("Today's Deals", "23566064011"), NONE("None", null);

		private final String displayName;
		private final String dealTypeId;

		DiscountType(String displayName, String dealTypeId) {
			this.displayName = displayName;
			this.dealTypeId = dealTypeId;
		}

		public String getDisplayName() {
			return displayName;
		}

		public String getDealTypeId() {
			return dealTypeId;
		}
	}

	public class SliderProperties {
		@SerializedName("lowerBoundParamName")
		private String lowerBoundParamName;

		@SerializedName("upperBoundParamName")
		private String upperBoundParamName;

		@SerializedName("stepValues")
		private Integer[] stepValues;

		@SerializedName("stepLabels")
		private String[] stepLabels;

		// Getters
		public Integer[] getStepValues() {
			return stepValues;
		}

		public String[] getStepLabels() {
			return stepLabels;
		}
	}
}