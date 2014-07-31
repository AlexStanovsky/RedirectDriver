import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.Queue;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class ClickoutDriverWorker extends DriverWorkerBase {

	public final static String[] TESTS_TO_RUN = { "clickout" };

	public ClickoutDriverWorker(Queue<String> sites, Queue<WebDriver> drivers,
			EnvironmentPrefix environment, Writer writer) {
		super(sites, drivers, environment, writer);
	}

	public void executeTest(WebDriver driver, String site) throws IOException {

		driver.manage().deleteAllCookies();
		
		Collection<String> siteRedirectLinks = gatherLinksForSite(driver,
				buildLink(site, _environment));

		for (String redirectLink : siteRedirectLinks) {

			String result = redirectLink;

			String currentUrl = "Error in get Url";

			try {

				// Redirect to link
				driver.get(redirectLink);

				WebDriverWait wait = new WebDriverWait(driver, 20);
				wait.until(ExpectedConditions.not(ExpectedConditions
						.titleIs("")));

			} catch (Throwable err) {
				System.out.println("Error in navigation to " + redirectLink);
			}

			try {
				currentUrl = driver.getCurrentUrl();
			} catch (Throwable tr) {
				System.out.println("Couldn't get current url for redirect :"
						+ redirectLink);
			}

			result += SiteDriver.DELIMITER + currentUrl;

			synchronized (_writer) {
				_writer.write(result + "\n");
			}
		}

		synchronized (_writer) {
			_writer.flush();
		}
	}
}
