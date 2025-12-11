
package Controllers;

import static interfaces.Log.LOG;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

@Named("extenderService")
@ApplicationScoped
public class ScheduleExtenderService {

public Map<String, ExtenderExample> createExtenderExamples() {
	final Properties properties = new Properties();
           LOG.debug("entering createExtenderExamples");
try{ 
  //      ClassLoader clo = Thread.currentThread().getContextClassLoader();
       String settings = "schedule-extender-examples.properties";
       InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(settings);
  //     utils.LCUtil.printProperties(settings);
       properties.load(is);
       is.close();
       LOG.debug("after createExtenderExamples");
} catch (IOException e) {
            String msg = "IO exception error inStream " + e;
	    LOG.error(msg);
} catch (Exception e) {
            String msg = "Exception error inStream " + e;
	    LOG.error(msg);
	}
	final Map<String, ExtenderExample> extenderExamples = new HashMap<>();

	for (final String key : properties.stringPropertyNames()) {
			if (key != null && key.endsWith(".name")) {
				final String baseKey = key.substring(0, key.length() - 5);
				final ExtenderExample example = new ExtenderExample(baseKey, properties);
				if (example.getName() != null && example.getValue() != null && !example.getName().trim().isEmpty()
						&& !example.getValue().trim().isEmpty()) {
					extenderExamples.put(baseKey, example);
				}
			}
	}
		return extenderExamples;
	}

	public static class ExtenderExample {
		private final String details;
		private final String html;
		private final String key;
		private final String link;
		private final String name;
		private final String value;

	public ExtenderExample(String key, Properties properties) {
		this.key = key;
		this.details = properties.getProperty(key + ".details");
		this.html = properties.getProperty(key + ".html");
		this.link = properties.getProperty(key + ".link");
		this.name = properties.getProperty(key + ".name");
		this.value = properties.getProperty(key + ".value");
	}

	public String getDetails() {
			return details;
	}

	public String getHtml() {
			return html;
	}

	public String getKey() {
			return key;
	}

	public String getLink() {
			return link;
	}

	public String getName() {
			return name;
	}

	public String getValue() {
			return value;
	}
	}
} //end Class