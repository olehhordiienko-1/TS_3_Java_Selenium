package UNI.UITesting.Testing.elmirua;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

public class Util {
	public static void takeScreenshot(WebDriver driver, String catalogName, String testName) {
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
}
