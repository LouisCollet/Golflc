
package lc.golfnew;

/**
 * {@code Singleton enum} containing the current system information such as the
 * operational system name and the java version.
 * 
 * @author Bruno Gasparotto
 *
 */
public enum SystemInfoEnum {
	INSTANCE;

	private final String systemName = System.getProperty("os.name");
	private final String javaVersion = System.getProperty("java.version");

	public String getSystemName() {
		return systemName;
	}

	public String getJavaVersion() {
		return javaVersion;
	}
}