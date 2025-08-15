package UserInterface;

import common.Automaton;
import java.io.File;

/**
 * Interface for automaton panels to provide common actions functionality
 */
public interface AutomatonPanel {
    
    /**
     * Runs the automaton with current text input and displays the graph
     */
    void runAutomaton();
    
    /**
     * Compiles/validates the automaton and displays warnings
     */
    void compileAutomaton();
    
    /**
     * Saves the current content to a file (quick save if file exists, save as if new)
     */
    void saveAutomaton();
    
    /**
     * Always shows save dialog (Save As functionality)
     */
    default void saveAsAutomaton() {
        // Default implementation for backward compatibility
        saveAutomaton();
    }
    
    /**
     * Tests the automaton with matching .test file
     */
    default void testAutomaton() {
        // Default implementation - no operation
    }
    
    /**
     * Tests the automaton with user-selected test file
     */
    default void testAutomatonWithFile() {
        // Default implementation - no operation
    }
    
    /**
     * Gets the current text from the text editor
     */
    String getTextAreaContent();
    
    /**
     * Gets the automaton associated with this panel
     */
    Automaton getAutomaton();
    
    /**
     * Gets the current file if any
     */
    File getCurrentFile();
    
    /**
     * Sets the current file
     */
    void setCurrentFile(File file);
    
    /**
     * Adds a document listener to track text changes
     */
    void addTextChangeListener(Runnable onChange);
}
