package nettest;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;

public class Browser {

	private static WebDriver driver = createFirefoxDriver();

	private static WebDriver createFirefoxDriver() {
		FirefoxProfile profile = new FirefoxProfile();
		profile.setPreference("network.proxy.type", 1);
		profile.setPreference("network.proxy.http", "127.0.0.1");
		profile.setPreference("network.proxy.http_port", 9999);
		return new FirefoxDriver(profile);
	}

	public static void open(String url) {

		driver.get(url);
	}

	public static void close() {
		driver.quit();
	}
}
