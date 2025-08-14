package UserInterface;

import java.io.*;
import java.util.Properties;

/**
 * Manages application preferences including persistent storage
 */
public class PreferencesManager {
    private static final String PREFERENCES_FILE = ".cs410_preferences.properties";
    private static final String LAST_DIRECTORY_KEY = "lastUsedDirectory";
    
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
}