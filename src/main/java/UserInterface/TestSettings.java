package UserInterface;

import java.util.ArrayList;
import java.util.List;

/**
 * Global test configuration settings with persistence.
 * Singleton pattern ensures consistent settings across all panels.
 *
 * Settings are automatically persisted via PreferencesManager when changed.
 */
public class TestSettings {

    private static TestSettings instance;
    private static PreferencesManager preferencesManager;

    // ═══════════════════════════════════════════════════════════════════
    // CONSTANTS
    // ═══════════════════════════════════════════════════════════════════

    public static final int DEFAULT_MIN_POINTS = 4;
    public static final int DEFAULT_MAX_POINTS = 15;
    public static final int DEFAULT_TIMEOUT_SECONDS = 20;

    // ═══════════════════════════════════════════════════════════════════
    // SETTINGS FIELDS
    // ═══════════════════════════════════════════════════════════════════

    private int minPoints;
    private int maxPoints;
    private int timeoutSeconds;
    private Integer maxRules;        // CFG only, null = no limit
    private Integer maxTransitions;  // PDA only, null = no limit
    private Integer maxRegexLength;  // REX only, null = no limit

    // Change listeners
    private final List<SettingsChangeListener> listeners = new ArrayList<>();

    // ═══════════════════════════════════════════════════════════════════
    // SINGLETON ACCESS
    // ═══════════════════════════════════════════════════════════════════

    private TestSettings() {
        // Initialize with defaults
        this.minPoints = DEFAULT_MIN_POINTS;
        this.maxPoints = DEFAULT_MAX_POINTS;
        this.timeoutSeconds = DEFAULT_TIMEOUT_SECONDS;
        this.maxRules = null;
        this.maxTransitions = null;
        this.maxRegexLength = null;
    }

    /**
     * Gets the singleton instance of TestSettings.
     * Initializes from preferences on first access.
     */
    public static synchronized TestSettings getInstance() {
        if (instance == null) {
            instance = new TestSettings();
            // Load from preferences if available
            if (preferencesManager != null) {
                instance.loadFromPreferences(preferencesManager);
            }
        }
        return instance;
    }

    /**
     * Sets the PreferencesManager for persistence.
     * Should be called early in application startup.
     */
    public static void setPreferencesManager(PreferencesManager prefs) {
        preferencesManager = prefs;
        if (instance != null) {
            instance.loadFromPreferences(prefs);
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // GETTERS
    // ═══════════════════════════════════════════════════════════════════

    public int getMinPoints() {
        return minPoints;
    }

    public int getMaxPoints() {
        return maxPoints;
    }

    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }

    /**
     * Returns timeout in milliseconds (convenience method).
     */
    public long getTimeoutMs() {
        return timeoutSeconds * 1000L;
    }

    /**
     * Returns max rules limit for CFG, or null if no limit.
     */
    public Integer getMaxRules() {
        return maxRules;
    }

    /**
     * Returns max transitions limit for PDA, or null if no limit.
     */
    public Integer getMaxTransitions() {
        return maxTransitions;
    }

    /**
     * Returns max regex length limit for REX, or null if no limit.
     */
    public Integer getMaxRegexLength() {
        return maxRegexLength;
    }

    // ═══════════════════════════════════════════════════════════════════
    // SETTERS (auto-save on change)
    // ═══════════════════════════════════════════════════════════════════

    public void setMinPoints(int value) {
        if (value < 0) value = 0;
        if (value != this.minPoints) {
            this.minPoints = value;
            saveAndNotify();
        }
    }

    public void setMaxPoints(int value) {
        if (value < 1) value = 1;
        if (value != this.maxPoints) {
            this.maxPoints = value;
            saveAndNotify();
        }
    }

    public void setTimeoutSeconds(int value) {
        if (value < 1) value = 1;
        if (value != this.timeoutSeconds) {
            this.timeoutSeconds = value;
            saveAndNotify();
        }
    }

    /**
     * Sets max rules limit for CFG. Pass null to remove limit.
     */
    public void setMaxRules(Integer value) {
        if (value != null && value < 1) value = null;
        if (!equals(value, this.maxRules)) {
            this.maxRules = value;
            saveAndNotify();
        }
    }

    /**
     * Sets max transitions limit for PDA. Pass null to remove limit.
     */
    public void setMaxTransitions(Integer value) {
        if (value != null && value < 1) value = null;
        if (!equals(value, this.maxTransitions)) {
            this.maxTransitions = value;
            saveAndNotify();
        }
    }

    /**
     * Sets max regex length limit for REX. Pass null to remove limit.
     */
    public void setMaxRegexLength(Integer value) {
        if (value != null && value < 1) value = null;
        if (!equals(value, this.maxRegexLength)) {
            this.maxRegexLength = value;
            saveAndNotify();
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // UTILITY METHODS
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Resets all settings to their default values.
     */
    public void resetToDefaults() {
        this.minPoints = DEFAULT_MIN_POINTS;
        this.maxPoints = DEFAULT_MAX_POINTS;
        this.timeoutSeconds = DEFAULT_TIMEOUT_SECONDS;
        this.maxRules = null;
        this.maxTransitions = null;
        this.maxRegexLength = null;
        saveAndNotify();
    }

    /**
     * Loads settings from PreferencesManager.
     */
    public void loadFromPreferences(PreferencesManager prefs) {
        if (prefs == null) return;

        this.minPoints = prefs.getTestMinPoints();
        this.maxPoints = prefs.getTestMaxPoints();
        this.timeoutSeconds = prefs.getTestTimeout();
        this.maxRules = prefs.getTestMaxRules();
        this.maxTransitions = prefs.getTestMaxTransitions();
        this.maxRegexLength = prefs.getTestMaxRegexLength();
    }

    /**
     * Saves settings to PreferencesManager.
     */
    public void saveToPreferences(PreferencesManager prefs) {
        if (prefs == null) return;

        prefs.setTestMinPoints(minPoints);
        prefs.setTestMaxPoints(maxPoints);
        prefs.setTestTimeout(timeoutSeconds);
        prefs.setTestMaxRules(maxRules);
        prefs.setTestMaxTransitions(maxTransitions);
        prefs.setTestMaxRegexLength(maxRegexLength);
    }

    private void saveAndNotify() {
        if (preferencesManager != null) {
            saveToPreferences(preferencesManager);
        }
        notifyListeners();
    }

    // ═══════════════════════════════════════════════════════════════════
    // CHANGE LISTENERS
    // ═══════════════════════════════════════════════════════════════════

    public void addChangeListener(SettingsChangeListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeChangeListener(SettingsChangeListener listener) {
        listeners.remove(listener);
    }

    private void notifyListeners() {
        for (SettingsChangeListener listener : listeners) {
            listener.onSettingsChanged(this);
        }
    }

    /**
     * Listener interface for settings changes.
     */
    public interface SettingsChangeListener {
        void onSettingsChanged(TestSettings settings);
    }

    // ═══════════════════════════════════════════════════════════════════
    // HELPER
    // ═══════════════════════════════════════════════════════════════════

    private static boolean equals(Integer a, Integer b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.equals(b);
    }
}
