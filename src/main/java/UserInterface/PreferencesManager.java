package UserInterface;

import java.io.*;
import java.util.Properties;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Manages application preferences including persistent storage
 */
public class PreferencesManager {
    private static final String PREFERENCES_FILE = ".cs410_preferences.properties";
    private static final String LAST_DIRECTORY_KEY = "lastUsedDirectory";
    private static final String RECENT_FILES_KEY = "recentFiles";
    private static final String LAST_OPENED_FILES_KEY = "lastOpenedFiles";
    private static final String FILE_SEPARATOR = "|";
    private static final int MAX_RECENT_FILES = 10;

    // Test settings keys
    private static final String TEST_MIN_POINTS = "test.minPoints";
    private static final String TEST_MAX_POINTS = "test.maxPoints";
    private static final String TEST_TIMEOUT = "test.timeoutSeconds";
    private static final String TEST_MAX_RULES = "test.maxRules";
    private static final String TEST_MAX_TRANSITIONS = "test.maxTransitions";
    private static final String TEST_MAX_REGEX_LENGTH = "test.maxRegexLength";
    
    private Properties properties;
    private File preferencesFile;
    
    public PreferencesManager() {
        properties = new Properties();
        String userHome = System.getProperty("user.home");
        preferencesFile = new File(userHome, PREFERENCES_FILE);
        loadPreferences();
    }
    
    /**
     * Loads preferences from the properties file
     */
    private void loadPreferences() {
        if (preferencesFile.exists()) {
            try (FileInputStream fis = new FileInputStream(preferencesFile)) {
                properties.load(fis);
            } catch (IOException e) {
                System.err.println("Error loading preferences: " + e.getMessage());
            }
        }
    }
    
    /**
     * Saves preferences to the properties file
     */
    public void savePreferences() {
        try (FileOutputStream fos = new FileOutputStream(preferencesFile)) {
            properties.store(fos, "CS.410 Graph System Preferences");
        } catch (IOException e) {
            System.err.println("Error saving preferences: " + e.getMessage());
        }
    }
    
    /**
     * Gets the last used directory path
     */
    public String getLastDirectory() {
        return properties.getProperty(LAST_DIRECTORY_KEY, null);
    }
    
    /**
     * Sets the last used directory path
     */
    public void setLastDirectory(String directoryPath) {
        if (directoryPath != null) {
            properties.setProperty(LAST_DIRECTORY_KEY, directoryPath);
            savePreferences();
        }
    }
    
    /**
     * Gets the last used directory as a File object, returns null if invalid
     */
    public File getLastDirectoryAsFile() {
        String path = getLastDirectory();
        if (path != null) {
            File dir = new File(path);
            if (dir.exists() && dir.isDirectory()) {
                return dir;
            }
        }
        return null;
    }
    
    /**
     * Gets the list of recent files
     */
    public List<String> getRecentFiles() {
        String recentFilesStr = properties.getProperty(RECENT_FILES_KEY, "");
        if (recentFilesStr.isEmpty()) {
            return new ArrayList<>();
        }
        return new ArrayList<>(Arrays.asList(recentFilesStr.split("\\" + FILE_SEPARATOR)));
    }
    
    /**
     * Adds a file to the recent files list
     */
    public void addRecentFile(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            return;
        }
        
        List<String> recentFiles = getRecentFiles();
        
        // Remove if already exists to move to top
        recentFiles.remove(filePath);
        
        // Add to beginning
        recentFiles.add(0, filePath);
        
        // Limit to MAX_RECENT_FILES
        if (recentFiles.size() > MAX_RECENT_FILES) {
            recentFiles = recentFiles.subList(0, MAX_RECENT_FILES);
        }
        
        // Save back to properties
        String recentFilesStr = String.join(FILE_SEPARATOR, recentFiles);
        properties.setProperty(RECENT_FILES_KEY, recentFilesStr);
        savePreferences();
    }
    
    /**
     * Clears the recent files list
     */
    public void clearRecentFiles() {
        properties.remove(RECENT_FILES_KEY);
        savePreferences();
    }
    
    /**
     * Gets the list of last opened files (files that were open when app closed)
     */
    public List<String> getLastOpenedFiles() {
        String lastOpenedStr = properties.getProperty(LAST_OPENED_FILES_KEY, "");
        if (lastOpenedStr.isEmpty()) {
            return new ArrayList<>();
        }
        return new ArrayList<>(Arrays.asList(lastOpenedStr.split("\\" + FILE_SEPARATOR)));
    }
    
    /**
     * Sets the list of last opened files (to restore when app starts)
     */
    public void setLastOpenedFiles(List<String> filePaths) {
        if (filePaths == null || filePaths.isEmpty()) {
            properties.remove(LAST_OPENED_FILES_KEY);
        } else {
            // Filter out null/empty paths
            List<String> validPaths = new ArrayList<>();
            for (String path : filePaths) {
                if (path != null && !path.trim().isEmpty()) {
                    validPaths.add(path);
                }
            }
            
            if (!validPaths.isEmpty()) {
                String lastOpenedStr = String.join(FILE_SEPARATOR, validPaths);
                properties.setProperty(LAST_OPENED_FILES_KEY, lastOpenedStr);
            } else {
                properties.remove(LAST_OPENED_FILES_KEY);
            }
        }
        savePreferences();
    }
    
    /**
     * Removes a file from recent files (useful when file no longer exists)
     */
    public void removeRecentFile(String filePath) {
        List<String> recentFiles = getRecentFiles();
        if (recentFiles.remove(filePath)) {
            String recentFilesStr = String.join(FILE_SEPARATOR, recentFiles);
            properties.setProperty(RECENT_FILES_KEY, recentFilesStr);
            savePreferences();
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // TEST SETTINGS
    // ═══════════════════════════════════════════════════════════════════

    public int getTestMinPoints() {
        return getIntProperty(TEST_MIN_POINTS, TestSettings.DEFAULT_MIN_POINTS);
    }

    public void setTestMinPoints(int value) {
        setIntProperty(TEST_MIN_POINTS, value);
    }

    public int getTestMaxPoints() {
        return getIntProperty(TEST_MAX_POINTS, TestSettings.DEFAULT_MAX_POINTS);
    }

    public void setTestMaxPoints(int value) {
        setIntProperty(TEST_MAX_POINTS, value);
    }

    public int getTestTimeout() {
        return getIntProperty(TEST_TIMEOUT, TestSettings.DEFAULT_TIMEOUT_SECONDS);
    }

    public void setTestTimeout(int value) {
        setIntProperty(TEST_TIMEOUT, value);
    }

    public Integer getTestMaxRules() {
        return getOptionalIntProperty(TEST_MAX_RULES);
    }

    public void setTestMaxRules(Integer value) {
        setOptionalIntProperty(TEST_MAX_RULES, value);
    }

    public Integer getTestMaxTransitions() {
        return getOptionalIntProperty(TEST_MAX_TRANSITIONS);
    }

    public void setTestMaxTransitions(Integer value) {
        setOptionalIntProperty(TEST_MAX_TRANSITIONS, value);
    }

    public Integer getTestMaxRegexLength() {
        return getOptionalIntProperty(TEST_MAX_REGEX_LENGTH);
    }

    public void setTestMaxRegexLength(Integer value) {
        setOptionalIntProperty(TEST_MAX_REGEX_LENGTH, value);
    }

    // ═══════════════════════════════════════════════════════════════════
    // HELPER METHODS
    // ═══════════════════════════════════════════════════════════════════

    private int getIntProperty(String key, int defaultValue) {
        String value = properties.getProperty(key);
        if (value != null) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                // Fall through to default
            }
        }
        return defaultValue;
    }

    private void setIntProperty(String key, int value) {
        properties.setProperty(key, String.valueOf(value));
        savePreferences();
    }

    private Integer getOptionalIntProperty(String key) {
        String value = properties.getProperty(key);
        if (value != null && !value.isEmpty()) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                // Fall through to null
            }
        }
        return null;
    }

    private void setOptionalIntProperty(String key, Integer value) {
        if (value == null) {
            properties.remove(key);
        } else {
            properties.setProperty(key, String.valueOf(value));
        }
        savePreferences();
    }
}