package UserInterface;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.function.Consumer;

/**
 * Checks for application updates by querying the GitHub Releases API.
 * Performs network operations asynchronously to avoid blocking the UI.
 */
public class VersionChecker {

    private static final String RELEASES_API_URL =
        "https://api.github.com/repos/olcaytaner/Automata_Practice_and_Test/releases/latest";

    private static final int CONNECTION_TIMEOUT_MS = 3000;
    private static final int READ_TIMEOUT_MS = 3000;

    /**
     * Holds information about a release version.
     */
    public static class VersionInfo {
        public final String currentVersion;
        public final String latestVersion;
        public final String releaseUrl;
        public final String releaseNotes;
        public final String releaseName;

        public VersionInfo(String currentVersion, String latestVersion,
                          String releaseUrl, String releaseNotes, String releaseName) {
            this.currentVersion = currentVersion;
            this.latestVersion = latestVersion;
            this.releaseUrl = releaseUrl;
            this.releaseNotes = releaseNotes;
            this.releaseName = releaseName;
        }
    }

    /**
     * Asynchronously checks for updates and invokes the callback if a newer version is available.
     * The callback is NOT invoked on the EDT - caller must handle EDT dispatch.
     *
     * @param currentVersion The current application version (e.g., "1.3.1")
     * @param onUpdateAvailable Callback invoked with VersionInfo if update is available
     */
    public static void checkForUpdatesAsync(String currentVersion, Consumer<VersionInfo> onUpdateAvailable) {
        Thread checkThread = new Thread(() -> {
            try {
                if (!isNetworkAvailable()) {
                    return;
                }

                VersionInfo info = fetchLatestRelease(currentVersion);
                if (info != null && compareVersions(currentVersion, info.latestVersion) < 0) {
                    onUpdateAvailable.accept(info);
                }
            } catch (Exception e) {
                // Silent fail - don't interrupt application startup
            }
        }, "VersionChecker");
        checkThread.setDaemon(true);
        checkThread.start();
    }

    /**
     * Checks if network is available by attempting a connection to GitHub.
     */
    private static boolean isNetworkAvailable() {
        try {
            URL url = new URL("https://api.github.com");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(CONNECTION_TIMEOUT_MS);
            conn.setReadTimeout(READ_TIMEOUT_MS);
            conn.setRequestMethod("HEAD");
            conn.setRequestProperty("User-Agent", "CS410-Exam-VersionChecker");
            int responseCode = conn.getResponseCode();
            conn.disconnect();
            return (200 <= responseCode && responseCode < 400);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Fetches the latest release information from GitHub Releases API.
     */
    private static VersionInfo fetchLatestRelease(String currentVersion) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(RELEASES_API_URL);
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(CONNECTION_TIMEOUT_MS);
            conn.setReadTimeout(READ_TIMEOUT_MS);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "CS410-Exam-VersionChecker");
            conn.setRequestProperty("Accept", "application/vnd.github.v3+json");

            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                return null;
            }

            // Read JSON response
            StringBuilder response = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
            }

            // Parse JSON manually (to avoid external dependencies)
            String json = response.toString();
            String tagName = extractJsonString(json, "tag_name");
            String htmlUrl = extractJsonString(json, "html_url");
            String body = extractJsonString(json, "body");
            String name = extractJsonString(json, "name");

            if (tagName == null) {
                return null;
            }

            // Strip "v" prefix from tag if present (e.g., "v1.3.1" -> "1.3.1")
            String latestVersion = tagName.startsWith("v") ? tagName.substring(1) : tagName;

            // Clean up release notes (unescape newlines)
            if (body != null) {
                body = body.replace("\\n", "\n").replace("\\r", "");
            }

            return new VersionInfo(currentVersion, latestVersion, htmlUrl, body, name);

        } catch (Exception e) {
            return null;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    /**
     * Simple JSON string extractor (avoids external JSON library dependency).
     * Extracts the value for a given key from a JSON string.
     */
    private static String extractJsonString(String json, String key) {
        String searchKey = "\"" + key + "\"";
        int keyIndex = json.indexOf(searchKey);
        if (keyIndex < 0) {
            return null;
        }

        // Find the colon after the key
        int colonIndex = json.indexOf(':', keyIndex + searchKey.length());
        if (colonIndex < 0) {
            return null;
        }

        // Skip whitespace and find the opening quote
        int valueStart = colonIndex + 1;
        while (valueStart < json.length() && Character.isWhitespace(json.charAt(valueStart))) {
            valueStart++;
        }

        if (valueStart >= json.length()) {
            return null;
        }

        // Check for null value
        if (json.substring(valueStart).startsWith("null")) {
            return null;
        }

        // Must be a quoted string
        if (json.charAt(valueStart) != '"') {
            return null;
        }

        // Find the closing quote (handling escaped quotes)
        StringBuilder value = new StringBuilder();
        int i = valueStart + 1;
        while (i < json.length()) {
            char c = json.charAt(i);
            if (c == '\\' && i + 1 < json.length()) {
                char next = json.charAt(i + 1);
                if (next == '"' || next == '\\' || next == 'n' || next == 'r' || next == 't') {
                    if (next == 'n') {
                        value.append('\n');
                    } else if (next == 'r') {
                        value.append('\r');
                    } else if (next == 't') {
                        value.append('\t');
                    } else {
                        value.append(next);
                    }
                    i += 2;
                    continue;
                }
            }
            if (c == '"') {
                break;
            }
            value.append(c);
            i++;
        }

        return value.toString();
    }

    /**
     * Compares two semantic versions.
     *
     * @param v1 First version (e.g., "1.3.1")
     * @param v2 Second version (e.g., "1.3.2")
     * @return negative if v1 < v2, 0 if equal, positive if v1 > v2
     */
    static int compareVersions(String v1, String v2) {
        if (v1 == null || v2 == null) {
            return 0;
        }

        // Remove any non-numeric prefix (e.g., "v1.3.1" -> "1.3.1")
        v1 = v1.replaceFirst("^[^0-9]*", "");
        v2 = v2.replaceFirst("^[^0-9]*", "");

        String[] parts1 = v1.split("\\.");
        String[] parts2 = v2.split("\\.");

        int length = Math.max(parts1.length, parts2.length);
        for (int i = 0; i < length; i++) {
            int p1 = 0;
            int p2 = 0;

            if (i < parts1.length) {
                try {
                    // Handle versions like "1.3.1-beta" by taking only numeric part
                    String part = parts1[i].replaceAll("[^0-9].*", "");
                    p1 = part.isEmpty() ? 0 : Integer.parseInt(part);
                } catch (NumberFormatException e) {
                    p1 = 0;
                }
            }

            if (i < parts2.length) {
                try {
                    String part = parts2[i].replaceAll("[^0-9].*", "");
                    p2 = part.isEmpty() ? 0 : Integer.parseInt(part);
                } catch (NumberFormatException e) {
                    p2 = 0;
                }
            }

            if (p1 != p2) {
                return p1 - p2;
            }
        }
        return 0;
    }
}
