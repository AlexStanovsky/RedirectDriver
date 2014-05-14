import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class DriverWorker implements Runnable {

	private Queue<String> _sites;
	private Queue<WebDriver> _drivers;
	private Writer _writer;
	private EnvironmentPrefix _environment;
	private String[] _testsToRun;

	public DriverWorker(Queue<String> sites, Queue<WebDriver> drivers,
			EnvironmentPrefix environment, Writer writer, String[] testsToRun) {
		_sites = sites;
		_drivers = drivers;
		_writer = writer;
		_environment = environment;
		_testsToRun = testsToRun;
	}

	@Override
	public void run() {

		String site = null;
		synchronized (_sites) {
			if (!_sites.isEmpty()) {
				System.out.println("Sites left: " + _sites.size());
				site = _sites.poll();
			}
		}

		if (site != null) {
			WebDriver driver = _drivers.poll();

			try {
				executeTest(driver, site);

			} catch (IOException e) {
				e.printStackTrace();
			}

			_drivers.offer(driver);
		}
	}

	public void executeTest(WebDriver driver, String site) throws IOException {

		Collection<String> siteRedirectLinks = gatherLinksForSite(driver,
				buildLink(site, _environment));

		
		
		for (String redirectLink : siteRedirectLinks) {

			String result = redirectLink;

			for (String testName : _testsToRun) {
				String linkWithTest = String.format("%s&test=%s", redirectLink,
						testName);

				String currentUrl = "Error in get Url";

				try {

					// Redirect to link
					driver.get(linkWithTest);
					
					WebDriverWait wait = new WebDriverWait(driver, 20);
					wait.until(ExpectedConditions.not(ExpectedConditions
							.titleIs("")));
					

				} catch (Throwable err) {
					System.out
							.println("Error in navigation to " + linkWithTest);
				}

				try {
					currentUrl = driver.getCurrentUrl();
				} catch (Throwable tr) {
					System.out
							.println("Couldn't get current url for redirect :"
									+ redirectLink);
				}

				result += RedirectDriver.DELIMITER + currentUrl;
			}

			synchronized (_writer) {
				_writer.write(result + "\n");
			}
		}

		synchronized (_writer) {
			_writer.flush();
		}
	}

	private Collection<String> gatherLinksForSite(WebDriver driver, String site) {
		Collection<String> links = new HashSet<String>();
		try {
			driver.get(site);

			// Get all the links in the page
			List<WebElement> webElemetsLinks = driver.findElements(By
					.partialLinkText(""));

			// Filter visit php links
			for (WebElement link : webElemetsLinks) {
				if (link.getAttribute("href").contains("visit.php")) {
					links.add(link.getAttribute("href"));
				}
			}

		} catch (Throwable er) {
			System.out.println("Error loading in " + site);
		}

		if (links.size() > 0) {
			System.out.println("Loaded successfully in " + site
					+ " with links " + links.size());
		}

		return links;

	}

	public String buildLink(String site, EnvironmentPrefix environment) {
		// Check if mobile
		if (site.startsWith("m.")){
			return "http://" + site.replace("m.", "m." +environment.toString() + ".");
		} else{
			return "http://" + site.replace("www", environment.toString());
		}
	}
}
