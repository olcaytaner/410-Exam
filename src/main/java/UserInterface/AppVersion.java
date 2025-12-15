package UserInterface;

import java.io.InputStream;
import java.util.Properties;

/**
 * Utility class for retrieving the application version.
 * Caches the version after first retrieval for efficiency.
 */
public final class AppVersion {
    private static String cachedVersion;

    /**
     * Returns the application version string.
     * Tries multiple sources in order:
     * 1. version.properties (Maven filtered, works with mvn compile)
     * 2. Package implementation version (works from JAR)
     * 3. Maven pom.properties (works after mvn package)
     * 4. Falls back to "dev" if all else fails
     */
    public static String getVersion() {
        if (cachedVersion != null) {
            return cachedVersion;
        }

        String version = null;

        // First try: Read from filtered version.properties (works with mvn compile)
        try {
            InputStream is = AppVersion.class.getResourceAsStream("/version.properties");
            if (is != null) {
                Properties props = new Properties();
                props.load(is);
                version = props.getProperty("version");
                is.close();
            }
        } catch (Exception e) {
            // Ignore and try other methods
        }

        // Second try: Get version from package (works when running from JAR)
        if (version == null) {
            Package pkg = AppVersion.class.getPackage();
            if (pkg != null) {
                version = pkg.getImplementationVersion();
            }
        }

        // Third try: Read from Maven pom.properties (works after mvn package)
        if (version == null) {
            try {
                InputStream is = AppVersion.class.getResourceAsStream(
                    "/META-INF/maven/org.example/CS410-Exam/pom.properties");
                if (is != null) {
                    Properties props = new Properties();
                    props.load(is);
                    version = props.getProperty("version");
                    is.close();
                }
            } catch (Exception e) {
                // Ignore and use fallback
            }
        }

        cachedVersion = (version != null) ? version : "dev";
        return cachedVersion;
    }

    private AppVersion() {
        // Prevent instantiation
    }
}
