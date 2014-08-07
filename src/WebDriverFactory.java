
import java.util.HashMap;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

import Drivers.InternetExplorerDriverEx;


public class WebDriverFactory {

	private HashMap<String, Class<? extends WebDriver>> _possibleDrivers;
	
	public WebDriverFactory(){
		_possibleDrivers = new HashMap<String, Class<? extends WebDriver>>();
		_possibleDrivers.put("FF", FirefoxDriver.class);
		_possibleDrivers.put("IE", InternetExplorerDriverEx.class);
		_possibleDrivers.put("CH", ChromeDriver.class);
	}
	
	public boolean isDriverLegal(String driverName){
		return _possibleDrivers.containsKey(driverName);
	}
	
	public WebDriver createDriver(String driverName){
		if (isDriverLegal(driverName))
		{
			Class<? extends WebDriver> webDriverClass = _possibleDrivers.get(driverName);
			
			WebDriver driver = null;
			try {
				driver = webDriverClass.getConstructor().newInstance();
		
			} catch (Exception e) {
				// Initialization error
			}
			
			return driver;
		}
		
		return null;
	}
}
