import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Queue;

public class HostWriter {

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		Queue<String> sites = SiteDriver.getSitesFromFile("sites_file");

		BufferedWriter writer = new BufferedWriter(new FileWriter(new File(
				"hosts")));

		for (String domain : sites) {

			String hostLine = String.format("%s\t%s\n%s\t%s\n",
					"107.21.7.245", domain.replace("www.", "www.qa."),
					"107.21.7.245", domain.replace("www.", "cdn.qa."));
			
			writer.write(hostLine);
			System.out.println(hostLine);
		}

		// Close writer
		writer.flush();
		writer.close();
		
	}

}
