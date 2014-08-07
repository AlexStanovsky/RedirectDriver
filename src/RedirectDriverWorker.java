import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.Queue;

import org.openqa.selenium.WebDriver;

public class RedirectDriverWorker extends DriverWorkerBase {

	public final static String[] TESTS_TO_RUN = { "new", "old" };
	
	public RedirectDriverWorker(Queue<String> sites, Queue<WebDriver> drivers,
			EnvironmentPrefix environment, Writer writer) {
		super(sites, drivers, environment, writer);
	}

	public void executeTest(WebDriver driver, String site) throws IOException {

		Collection<String> siteRedirectLinks = gatherLinksForSite(driver,
				buildLink(site, _environment));
		
		for (String redirectLink : siteRedirectLinks) {

			String result = redirectLink;

			for (String testName : TESTS_TO_RUN) {
				String linkWithTest = String.format("%s&test=%s", redirectLink,
						testName);

				String currentUrl = redirectToLink(driver, linkWithTest);

				result += SiteDriver.DELIMITER + currentUrl;
			}

			synchronized (_writer) {
				_writer.write(result + "\n");
			}
		}

		synchronized (_writer) {
			_writer.flush();
		}
	}
}
