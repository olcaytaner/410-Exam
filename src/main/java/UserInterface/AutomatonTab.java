package UserInterface;

import java.io.File;

/**
 * Represents a tab containing an automaton panel
 */
public class AutomatonTab {
    private String title;
    private AutomatonPanel panel;
    private File file;
    private boolean hasUnsavedChanges;
    private String originalContent;
    
    public AutomatonTab(String title, AutomatonPanel panel, File file) {
        this.title = title;
        this.panel = panel;
        this.file = file;
        this.hasUnsavedChanges = false;
        this.originalContent = panel.getTextAreaContent();
    }
    
    /**
     * Updates the tab's unsaved changes status by comparing current content with original
     */
    public void updateUnsavedStatus() {
        String currentContent = panel.getTextAreaContent();
        this.hasUnsavedChanges = !currentContent.equals(originalContent);
    }
    
    /**
     * Marks the tab as saved and updates the original content
     */
    public void markAsSaved() {
        this.hasUnsavedChanges = false;
        this.originalContent = panel.getTextAreaContent();
    }
    
    /**
     * Gets the display title for the tab (includes * if unsaved)
     */
    public String getDisplayTitle() {
        return hasUnsavedChanges ? title + " *" : title;
    }
    
    // Getters and setters
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public AutomatonPanel getPanel() {
        return panel;
    }
    
    public File getFile() {
        return file;
    }
    
    public void setFile(File file) {
        this.file = file;
        if (file != null) {
            this.title = file.getName();
        }
    }
    
    public boolean hasUnsavedChanges() {
        return hasUnsavedChanges;
    }
    
    public void setHasUnsavedChanges(boolean hasUnsavedChanges) {
        this.hasUnsavedChanges = hasUnsavedChanges;
    }
    
    public String getOriginalContent() {
        return originalContent;
    }
    
    public void setOriginalContent(String originalContent) {
        this.originalContent = originalContent;
    }
}
