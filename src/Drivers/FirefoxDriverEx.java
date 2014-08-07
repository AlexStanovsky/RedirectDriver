package Drivers;

import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;

public class FirefoxDriverEx extends FirefoxDriver{

	public FirefoxDriverEx(){
		super(createProfile());
	}
	
	private static FirefoxProfile createProfile(){
		FirefoxProfile profile = new FirefoxProfile();
		profile.setPreference("network.http.redirection-limit", 1);
		return profile;
	}
}
