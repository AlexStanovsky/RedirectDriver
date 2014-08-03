import java.util.HashMap;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;


public class WebDriverFactory {

	private HashMap<String, Class> _possibleDrivers;
	
	public WebDriverFactory(){
		_possibleDrivers = new HashMap<String, Class>();
		_possibleDrivers.put("FF", FirefoxDriver.class);
		_possibleDrivers.put("IE", InternetExplorerDriver.class);
		_possibleDrivers.put("CH", ChromeDriver.class);
	}
	
	public boolean isDriverLegal(String driverName){
		return _possibleDrivers.containsKey(driverName);
	}
	
	public WebDriver createDriver(String driverName){
		if (isDriverLegal(driverName))
		{
			Class webDriverClass = _possibleDrivers.get(driverName);
			
			WebDriver driver = null;
			try {
				driver = (WebDriver) webDriverClass.getConstructor().newInstance();
		
			} catch (Exception e) {
				// Initialization error
			}
			
			return driver;
		}
		
		return null;
	}
}
