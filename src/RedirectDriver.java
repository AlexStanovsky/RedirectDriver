import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;

public class RedirectDriver {
	public static final boolean DEBUG_MODE = false;
	public static final String DELIMITER = "^";
	private Queue<String> _sites;
	private static int _numOfThreads = 15;
	private EnvironmentPrefix _env;
	private final static String[] TESTS_TO_RUN = { "new", "old" };
	private final static String[][] MANDATORY_PARAMS = {
			{ "sites_file", "threads", "env" },
			{ "db_host", "db_user", "db_pass", "threads", "env" } };

	public static void main(String[] args) throws IOException {

		Map<String, String> params = null;
		int numOfThreads = _numOfThreads;
		EnvironmentPrefix env = EnvironmentPrefix.PRODUCTION;

		try {
			params = parseParams(args);

			try {
				numOfThreads = Integer.parseInt(params.get("threads"));
			} catch (Exception ex) {
			}

			try {
				env = EnvironmentPrefix.valueOf(params.get("env"));
			} catch (Exception ex) {
			}

		} catch (Exception e) {
			System.out.println("Missing param: " + e.getMessage());
			System.out.println("Usage:");
			System.out
					.println("	java -jar "
							+ RedirectDriver.class.getName()
							+ ".jar db_host=[host] db_name=[name] db_user=[user] db_pass=[pass] theads=[num] env=[LOCAL/QA/STAGING/PRODUCTION]");
			System.out.println("or:");
			System.out
					.println("	java -jar "
							+ RedirectDriver.class.getName()
							+ ".jar sites_file=[pass] theads=[num] env=[LOCAL/QA/STAGING/PRODUCTION]");
			System.exit(1);
		}

		Queue<String> _sites = null;

		if (params.containsKey("sites_file")) {
			_sites = getSitesFromFile(params.get("sites_file"));
		} else {
			_sites = getSitesFromDataBase(DEBUG_MODE, params.get("db_host"),
					params.get("db_name"), params.get("db_user"),
					params.get("db_pass"));
		}

		new RedirectDriver(_sites, numOfThreads, env).executeTests();
	}

	public static Map<String, String> parseParams(String[] args)
			throws Exception {
		Map<String, String> params = new HashMap<String, String>();

		for (String param : args) {
			String[] values = param.split("=");
			if (values.length == 2) {
				params.put(values[0], values[1]);
			}
		}

		checkMadatoryParams(params, 0);

		return params;
	}

	private static void checkMadatoryParams(Map<String, String> params,
			int madatoryParamsSetId) throws Exception {
		for (String param : MANDATORY_PARAMS[madatoryParamsSetId]) {
			if (!params.containsKey(param)) {

				if (madatoryParamsSetId == MANDATORY_PARAMS.length - 1) {
					throw new Exception(param);
				} else {
					checkMadatoryParams(params, ++madatoryParamsSetId);
				}
			}
		}
	}

	public RedirectDriver(Queue<String> sites, int threads,
			EnvironmentPrefix env) {
		_numOfThreads = threads;
		_env = env;
		_sites = sites;
	}

	private void executeTests() throws IOException {

		int numOfSites = _sites.size();

		BufferedWriter writer = new BufferedWriter(new FileWriter(new File(
				"result.csv")));

		printHeaders(TESTS_TO_RUN, writer);

		Queue<WebDriver> drivers = new ArrayBlockingQueue<WebDriver>(
				_numOfThreads);

		for (int i = 0; i < Math.min(_numOfThreads, numOfSites); i++) {
			// The Firefox driver supports javascript
			FirefoxProfile profile = new FirefoxProfile();
			profile.setPreference("network.http.redirection-limit", 1);
			WebDriver fireFoxDriver = new FirefoxDriver(profile);
			drivers.offer(fireFoxDriver);
		}

		ExecutorService executor = Executors.newFixedThreadPool(_numOfThreads);

		for (int i = 0; i < numOfSites; i++) {
			executor.execute(new DriverWorker(_sites, drivers, _env, writer,
					TESTS_TO_RUN));
		}

		executor.shutdown();
		while (!executor.isTerminated()) {
		}

		// Close writer
		writer.flush();
		writer.close();

		while (!drivers.isEmpty()) {
			try {
				drivers.poll().quit();
			} catch (Throwable ex) {

			}
		}

	}

	private static Queue<String> getSitesFromFile(String fileName) {
		Queue<String> resultList = new LinkedList<String>();

		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(fileName));
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		try {

			String line = null;

			while ((line = br.readLine()) != null) {
				resultList.add(line);
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return resultList;
	}

	private void printHeaders(String[] testsToRun, BufferedWriter writer)
			throws IOException {
		writer.write("link");
		for (String testName : testsToRun) {
			writer.write(DELIMITER + testName);
		}
		writer.write("\n");
	}

	public static void start(List<String> sites, EnvironmentPrefix env,
			String[] testsToRun, WebDriver driver, Writer writer)
			throws IOException {

		driver.quit();
	}

	public static Queue<String> getSitesFromDataBase(boolean debug,
			String host, String dbName, String user, String pass) {

		Queue<String> resultList = new LinkedList<String>();

		String sql = "select distinct url from sites";

		if (debug) {
			sql += " limit 1";
		}

		try {
			Class.forName("com.mysql.jdbc.Driver");

			String connectionString = String.format(
					"jdbc:mysql://%s/%s?user=%s&password=%s", host, dbName,
					user, pass);

			Connection connect = DriverManager.getConnection(connectionString);

			Statement statement = connect.createStatement();

			ResultSet resultSet = statement.executeQuery(sql);

			while (resultSet.next()) {
				resultList.add(resultSet.getString("url"));
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
		}

		return resultList;
	}
}
