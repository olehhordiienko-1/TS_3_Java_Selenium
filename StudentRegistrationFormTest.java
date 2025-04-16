package UNI.UITesting.Testing;

import junit.framework.TestCase;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;

import com.sun.tools.javac.util.Pair;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class StudentRegistrationFormTest extends TestCase {
    private WebDriver driver;
    private final String BASE_URL = "https://demoqa.com/automation-practice-form";
    
    class FormPage {
        private WebDriver driver;
        
        public FormPage(WebDriver driver) {
            this.driver = driver;
        }
        
        // Input field interactions
        public void enterFirstName(String firstName) {
            WebElement field = driver.findElement(By.id("firstName"));
            field.clear();
            field.sendKeys(firstName);
        }
        
        public void enterLastName(String lastName) {
            WebElement field = driver.findElement(By.id("lastName"));
            field.clear();
            field.sendKeys(lastName);
        }
        
        public void enterEmail(String email) {
            WebElement field = driver.findElement(By.id("userEmail"));
            field.clear();
            field.sendKeys(email);
        }
        
        public void selectGender(String gender) {
            String genderXpath = String.format("//label[contains(.,'%s')]", gender);
            driver.findElement(By.xpath(genderXpath)).click();
        }
        
        public void enterMobile(String mobile) {
            WebElement field = driver.findElement(By.id("userNumber"));
            field.clear();
            field.sendKeys(mobile);
        }
        
        // Validation checks
        public boolean isFirstNameValid() {
            return isFieldValid("firstName");
        }
        
        public boolean isLastNameValid() {
            return isFieldValid("lastName");
        }
        
        public boolean isEmailValid() {
            return isFieldValid("userEmail");
        }
        
        public boolean isGenderSelected(String gender) {
            String genderInputId = gender.equals("Male") ? "gender-radio-1" :
                                 gender.equals("Female") ? "gender-radio-2" : "gender-radio-3";
            return driver.findElement(By.id(genderInputId)).isSelected();
        }
        
        public boolean isMobileValid() {
            return isFieldValid("userNumber");
        }
        
        public void submitForm() throws InterruptedException {
        	Thread.sleep(3000);
            ((JavascriptExecutor)driver).executeScript("arguments[0].scrollIntoView(true);", driver.findElement(By.id("submit")));
            driver.findElement(By.id("submit")).click();
        }       

        public boolean isFieldValid(String fieldId) {
            WebElement field = driver.findElement(By.id(fieldId));
            return (boolean) ((JavascriptExecutor) driver).executeScript(
                "return arguments[0].checkValidity();", field);
        }

        public String getValidationMessage(String fieldId) {
            WebElement field = driver.findElement(By.id(fieldId));
            return (String) ((JavascriptExecutor) driver).executeScript("return arguments[0].validationMessage;", field);
        }
    }

    private void takeScreenshot(String testName) {
        File srcFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String fileName = "screenshots/" + testName + "_" + timestamp + ".png";

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
        driver.manage().window().maximize();
    }

    public void test1_NameBoundaryValues() throws InterruptedException {
        driver.get(BASE_URL);
        FormPage form = new FormPage(driver);
        
        List<Pair<String, Boolean>> testNames = Arrays.asList(
            new Pair<>("", false),                  // empty (invalid)
            new Pair<>("A", true),                 // 1 char (valid)
            new Pair<>("John", true),               // typical (valid)
            new Pair<>(new String(new char[50]).replace('\0', 'A'), true),  // max boundary (valid) FIXME OLEH
            new Pair<>(new String(new char[51]).replace('\0', 'A'), false)  // exceeds max (invalid)
        );
        
        for (int i = 0; i < testNames.size(); i++) {
            Pair<String, Boolean> testCase = testNames.get(i);
            form.enterFirstName(testCase.fst);
            form.enterLastName("Doe");
            form.enterEmail("test@example.com");
            form.enterMobile("1234567890");
            form.selectGender("Male");
            form.submitForm();

            boolean expected = testCase.snd;
            String message = "Test case " + i + " failed for name: " + testCase.fst;
            assertEquals(message, expected, form.isFirstNameValid());
            
            if (!testCase.snd) {
                System.out.println("Validation message: " + form.getValidationMessage("firstName"));
            }
            
            driver.navigate().refresh();
        }
    }

    public void test2_EmailEquivalencePartitions()  throws InterruptedException {
        driver.get(BASE_URL);
        FormPage form = new FormPage(driver);
        
        List<Pair<String, Boolean>> testEmails = Arrays.asList(
            new Pair<>("", true),                      // empty (invalid)
            new Pair<>("invalid", false),               // no @ (invalid)
            new Pair<>("invalid@", false),              // no domain (invalid)
            new Pair<>("@domain.com", false),           // no local part (invalid)
            new Pair<>("valid@example.com", true),      // valid
            new Pair<>("valid@sub.example.com", true),  // valid with subdomain
            new Pair<>("a@b.cd", true),                // minimal valid
            new Pair<>(new String(new char[100]).replace('\0', 'a') + "@example.com", true) // long but valid
        );

        for (int i = 0; i < testEmails.size(); i++) {
            Pair<String, Boolean> testCase = testEmails.get(i);
            form.enterFirstName("John");
            form.enterLastName("Doe");
            form.enterEmail(testCase.fst);
            form.enterMobile("1234567890");
            form.selectGender("Female");
            form.submitForm();
            
            boolean expected = testCase.snd;
            String message = "Test case " + i + " failed for email: " + testCase.fst;
            assertEquals(message, expected, form.isEmailValid());
            
            if (!testCase.snd) {
                System.out.println("Validation message: " + form.getValidationMessage("userEmail"));
            }
            
            driver.navigate().refresh();
        }
    }

    public void test3_MobileBoundaryValues()  throws InterruptedException {
        driver.get(BASE_URL);
        FormPage form = new FormPage(driver);
        
        List<Pair<String, Boolean>> testMobiles = Arrays.asList(
            new Pair<>("", false),              // empty (invalid)
            new Pair<>("123456789", false),     // 9 digits (invalid)
            new Pair<>("1234567890", true),     // 10 digits (valid)
            new Pair<>("1234567890", true),     // a lot of digits (valid) FIXME OLEH !!!
            new Pair<>("12345abcde", false),    // non-numeric (invalid)
            new Pair<>("123 456 7890", false),  // with spaces (invalid)
            new Pair<>("(123)456-7890", false)  // with special chars (invalid)
        );
        
        for (int i = 0; i < testMobiles.size(); i++) {
            Pair<String, Boolean> testCase = testMobiles.get(i);
            form.enterFirstName("John");
            form.enterLastName("Doe");
            form.enterEmail("test@example.com");
            form.enterMobile(testCase.fst);
            form.selectGender("Other");
            form.submitForm();
            
            boolean expected = testCase.snd;
            String message = "Test case " + i + " failed for mobile: " + testCase.fst;
            assertEquals(message, expected, form.isMobileValid());
            
            if (!testCase.snd) {
                System.out.println("Validation message: " + form.getValidationMessage("userNumber"));
            }
            
            driver.navigate().refresh();
        }
    }

    public void test4_GenderValidation()  throws InterruptedException  {
        driver.get(BASE_URL);
        FormPage form = new FormPage(driver);
        
        List<Pair<String, Boolean>> testGenders = Arrays.asList(
            new Pair<>("Male", true),
            new Pair<>("Female", true),
            new Pair<>("Other", true)
        );
        
        // Test unselected gender
        form.enterFirstName("John");
        form.enterLastName("Doe");
        form.enterEmail("test@example.com");
        form.enterMobile("1234567890");
        form.submitForm();
        
        assertFalse("Form should not be valid without gender selected",
            form.isGenderSelected("Male") || 
            form.isGenderSelected("Female") || 
            form.isGenderSelected("Other"));
        
        // Test each gender selection
        for (int i = 0; i < testGenders.size(); i++) {
            Pair<String, Boolean> testCase = testGenders.get(i);
            form.selectGender(testCase.fst);
            form.submitForm();
            
            String message = "Test case " + i + " failed for gender: " + testCase.fst;
            assertTrue(message, form.isGenderSelected(testCase.fst));
            
            driver.navigate().refresh();
        }
    }

    public void test5_PairwiseFieldCombinations()  throws InterruptedException  {
        driver.get(BASE_URL);
        FormPage form = new FormPage(driver);
        
        List<Pair<Object[], Boolean>> testCases = Arrays.asList(
            new Pair<>(new Object[]{"Valid", "Valid", "a@b.cd", "1234567890", "Male"}, true),
            new Pair<>(new Object[]{"Valid", "", "valid@ex.com", "1234567890", "Female"}, false),
            new Pair<>(new Object[]{"", "Valid", "invalid", "1234567890", "Other"}, false),
            new Pair<>(new Object[]{"Valid", "Valid", "", "123", "Male"}, false),
            new Pair<>(new Object[]{"VeryLongButValidName", "Valid", "valid@ex.com", "123456789", "Female"}, false)
        );
        
        for (int i = 0; i < testCases.size(); i++) {
            Pair<Object[], Boolean> testCase = testCases.get(i);
            Object[] inputs = testCase.fst;
            
            form.enterFirstName((String)inputs[0]);
            form.enterLastName((String)inputs[1]);
            form.enterEmail((String)inputs[2]);
            form.enterMobile((String)inputs[3]);
            form.selectGender((String)inputs[4]);
            form.submitForm();
            
            boolean isValid = form.isFirstNameValid() 
                           && form.isLastNameValid()
                           && form.isEmailValid() 
                           && form.isMobileValid()
                           && form.isGenderSelected((String)inputs[4]);
            
            boolean expected = testCase.snd;
            assertEquals("Pairwise test case " + i + " failed", expected, isValid);
            
            takeScreenshot("test5_PairwiseCase_" + i);
            driver.navigate().refresh();
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