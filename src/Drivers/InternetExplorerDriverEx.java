package Drivers;

import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;

public class InternetExplorerDriverEx extends InternetExplorerDriver {
	public InternetExplorerDriverEx() {
		super(createCapabilities());
	}

	private static DesiredCapabilities createCapabilities() {
		DesiredCapabilities caps = DesiredCapabilities.internetExplorer();
		caps.setCapability(
				CapabilityType.ForSeleniumServer.ENSURING_CLEAN_SESSION, true);
		return caps;
	}
}
