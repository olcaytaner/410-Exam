package UserInterface;

/**
 * Test utility class that provides methods to create MainPanel instances
 * safe for testing without triggering UI dialogs.
 */
public class TestMainPanelFactory {
    
    /**
     * Creates a MainPanel instance for testing that prevents file dialogs.
     * This method does NOT enable headless mode, but overrides dialog methods.
     */
    public static MainPanel createForTesting() {
        MainPanel panel = new MainPanel();
        
        // Override the file manager with a mock version that doesn't show dialogs
        panel.fileManager = new TestFileManager(panel);
        
        return panel;
    }
    
    /**
     * Creates a MainPanel instance for headless testing (no UI components).
     * This is useful for tests that don't need actual UI interaction.
     */
    public static MainPanel createForHeadlessTesting() {
        // Enable headless mode to prevent UI dialogs
        System.setProperty("java.awt.headless", "true");
        
        MainPanel panel = new MainPanel();
        
        // Override the file manager with a mock version that doesn't show dialogs
        panel.fileManager = new TestFileManager(panel);
        
        return panel;
    }
    
    /**
     * Test-specific FileManager that prevents actual dialogs from showing
     */
    private static class TestFileManager extends MainPanel.FileManager {
        private MainPanel parent;
        
        TestFileManager(MainPanel parent) {
            parent.super();
            this.parent = parent;
        }
        
        @Override
        public void showOpenDialog() {
            // Do nothing - prevent actual file dialog from showing
            System.out.println("TestFileManager: showOpenDialog() called - no actual dialog shown");
        }
        
        @Override
        public java.io.File showSaveDialog(common.Automaton automaton, String currentFileName) {
            // Return null to simulate user canceling the dialog
            System.out.println("TestFileManager: showSaveDialog() called - returning null (canceled)");
            return null;
        }
    }
}