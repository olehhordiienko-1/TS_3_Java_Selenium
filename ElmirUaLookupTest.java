package UNI.UITesting.Testing.elmirua;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import junit.framework.TestCase;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ElmirUaLookupTest extends TestCase {
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

	protected final String BASE_URL = "https://elmir.ua/ua/";

	public void testProductSearch() {
		try {
			HomePage homePage = new HomePage(driver, BASE_URL, 5);
			ResultsPage resultsPage = homePage.openHomePage().enterSearchRequest("acer")
					.clickCategoryByName("Ноутбуки");

			takeScreenshot("testProductSearch");

			
			assertTrue("No products found", resultsPage.getProductsCount() > 0);

			assertTrue(resultsPage.doesElementContainText("Ноутбуки"));

			driver.quit();
		} catch (Exception e) {
			e.printStackTrace();
			takeScreenshot("testProductSearch_error");
			fail("Test failed: " + e.getMessage());
		}
	}

	public static final String[] ORDERING_METHODS = new String[] { "Від дешевих", "Від дорогих", "За оцінкою покупців",
			"За к-стю відгуків", "За найменуванням" };

	public void testSortingOptions() {
		try {
			ResultsPage resultsPage = new HomePage(driver, BASE_URL, 5).openHomePage().enterSearchRequest("Ноутбуки");

			for (String orderingMethod : ORDERING_METHODS) {
				resultsPage.selectOrderingMethod(orderingMethod);
				Thread.sleep(2000);
				String applied = resultsPage.getSelectedOrderingMethod();
				assertEquals(orderingMethod, applied);

			}

			driver.quit();

		} catch (Exception e) {
			e.printStackTrace();
			fail("Test failed: " + e.getMessage());
			takeScreenshot("testSortingOptions");
		}
	}

	public void testCombinedFiltersPairwise() {
	    List<String> errors = new ArrayList<>();

	    
		String[] categories = { ResultsPage.CONST_CATEGORY_LAPTOPS, ResultsPage.CONST_CATEGORY_MONITORS,
				ResultsPage.CONST_CATEGORY_SMARTPHONES };
		String[] brands = { "Xiaomi", "Samsung", "Asus" };
		String[] orderings = { "Від дешевих", "Від дорогих", "За оцінкою покупців" };

		
		for (String category : categories) {
			for (String brand : brands) {
				try {
					ResultsPage resultsPage = new HomePage(driver, BASE_URL, 5).openHomePage()
							.enterSearchRequest(category).selectBrand(brand);

					if (!resultsPage.getSelectedBrand(brand).equals("true")) {
						errors.add("Brand filter not applied for category: " + category + ", brand: " + brand);
					}
					takeScreenshot("pairwise_category_" + category + "_brand_" + brand);
				} catch (Exception e) {
					errors.add("Error for category: " + category + ", brand: " + brand + ": " + e.getMessage());
					takeScreenshot("pairwise_category_" + category + "_brand_" + brand + "_error");
				}
			}

			for (String ordering : orderings) {
				try {
					ResultsPage resultsPage = new HomePage(driver, BASE_URL, 5).openHomePage()
							.enterSearchRequest(category).selectOrderingMethod(ordering);

					if (!resultsPage.getSelectedOrderingMethod().equals(ordering)) {
						errors.add("Ordering filter not applied for category: " + category + ", ordering: " + ordering);
					}
					takeScreenshot("pairwise_category_" + category + "_ordering_" + ordering);
				} catch (Exception e) {
					errors.add("Error for category: " + category + ", ordering: " + ordering + ": " + e.getMessage());
					takeScreenshot("pairwise_category_" + category + "_ordering_" + ordering + "_error");
				}
			}
		}

		String fixedCategory = ResultsPage.CONST_CATEGORY_LAPTOPS;
		for (String brand : brands) {
			for (String ordering : orderings) {
				try {
					ResultsPage resultsPage = new HomePage(driver, BASE_URL, 5).openHomePage()
							.enterSearchRequest(fixedCategory).selectBrand(brand).selectOrderingMethod(ordering);

					if (!resultsPage.getSelectedBrand(brand).equals("true")) {
						errors.add("Brand filter not applied for brand: " + brand + ", ordering: " + ordering);
					}
					if (!resultsPage.getSelectedOrderingMethod().equals(ordering)) {
						errors.add("Ordering filter not applied for brand: " + brand + ", ordering: " + ordering);
					}
					takeScreenshot("pairwise_brand_" + brand + "_ordering_" + ordering);
				} catch (Exception e) {
					errors.add("Error for brand: " + brand + ", ordering: " + ordering + ": " + e.getMessage());
					takeScreenshot("pairwise_brand_" + brand + "_ordering_" + ordering + "_error");
				}
			}
		}

		if (!errors.isEmpty()) {
			fail("Test failed with errors:\n" + String.join("\n", errors));
		}

	}

	public void testCombinedFiltersDecisionTable() {
		try {
			Object[][] testCases = {
					{ ResultsPage.CONST_CATEGORY_LAPTOPS, "Acer", "Від дешевих", "15,6", "10000", "50000" },
					{ ResultsPage.CONST_CATEGORY_MONITORS, "Asus", "Від дорогих", "24", "5000", "20000" },
					{ ResultsPage.CONST_CATEGORY_SMARTPHONES, "Apple", "За оцінкою покупців", "6,1", "20000","100000" },
					{ ResultsPage.CONST_CATEGORY_TVS, "LG", "За к-стю відгуків", "55", "30000", "150000" } };

			for (Object[] testCase : testCases) {
				String category = (String) testCase[0];
				String brand = (String) testCase[1];
				String ordering = (String) testCase[2];
				String screensize = (String) testCase[3];
				String minPrice = (String) testCase[4];
				String maxPrice = (String) testCase[5];

				ResultsPage resultsPage = new HomePage(driver, BASE_URL, 5).openHomePage().enterSearchRequest(category)
						.selectBrand(brand).selectOrderingMethod(ordering).selectScreensize(category, screensize)
						.setPriceRange(minPrice, maxPrice);

				assertTrue(resultsPage.getSelectedBrand(brand).equals("true"));
				assertTrue(resultsPage.getSelectedOrderingMethod().equals(ordering));
				assertTrue(resultsPage.getScreensize(category, screensize).equals("true"));

				Map<String, String> appliedPrices = resultsPage.getAppliedPriceRange();
				assertTrue(appliedPrices.get("min").contains(minPrice));
				assertTrue(appliedPrices.get("max").contains(maxPrice));
				
				assertTrue(resultsPage.doesElementContainText(category) && resultsPage.doesElementContainText(brand));

				takeScreenshot("testCombinedFiltersDecisionTable_" + category + "_" + brand);
			}

			driver.quit();
		} catch (Exception e) {
			e.printStackTrace();
			fail("Test failed: " + e.getMessage());
			takeScreenshot("testCombinedFiltersDecisionTable_error");
		}
	}

	public void testSearchQueryEquivalencePartitioning() {
		try {
			Object[][] testCases = {
					
					{ "ноутбук", false }, { "acer aspire", false },
					{ "a", true }, { "дуже довгий пошуковий запит який перевищує звичайні межі введення", true },
					{ "", true }, 
					{ "12345", false }, 
					{ "!@#$%", false } 
			};

			for (Object[] testCase : testCases) {
				String query = (String) testCase[0];
				ResultsPage resultsPage = new HomePage(driver, BASE_URL, 5).openHomePage().enterSearchRequest(query);

				assertEquals((boolean) testCase[1], resultsPage.getProductsCount() == 0);
				takeScreenshot("testSearchQueryEquivalencePartitioningExtended_"
						+ query.substring(0, Math.min(10, query.length())));
			}

			driver.quit();
		} catch (Exception e) {
			e.printStackTrace();
			fail("Test failed: " + e.getMessage());
		}
	}

	public void testPriceBoundaryValuesExtended() {
		try {
			ResultsPage resultsPage = new HomePage(driver, BASE_URL, 5).openHomePage().enterSearchRequest("Ноутбуки");

			Map<String, Integer> defaultPrices = resultsPage.getDefaultPriceRange();
			int minPrice = defaultPrices.get("min");
			int maxPrice = defaultPrices.get("max");

			
			Object[][] testCases = { { minPrice - 1, maxPrice, true }, 
					{ minPrice, maxPrice + 1, true }, 
					{ minPrice, maxPrice, true }, 
					{ minPrice + 1, maxPrice - 1, true }, 
					{ maxPrice, minPrice, false }, 
					{ 0, 0, false }, 
					{ -1, -1, false } 
			};

			for (Object[] testCase : testCases) {
				int min = (int) testCase[0];
				int max = (int) testCase[1];
				boolean expectedResult = (boolean) testCase[2];

				resultsPage.setPriceRange(Integer.toString(min), Integer.toString(max));

				Map<String, String> map2 = resultsPage.getAppliedPriceRange();
				assertEquals(expectedResult, map2.get("min").contains(Integer.toString(min))
						&& map2.get("max").contains(Integer.toString(max)));

				takeScreenshot("testPriceBoundaryValuesExtended_" + min + "_" + max);
			}

			driver.quit();
		} catch (Exception e) {
			e.printStackTrace();
			fail("Test failed: " + e.getMessage());
		}
	}

}
